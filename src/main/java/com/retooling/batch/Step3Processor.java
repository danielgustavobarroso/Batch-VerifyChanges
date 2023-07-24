package com.retooling.batch;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;
import com.retooling.batch.entity.Chicken;
import com.retooling.batch.entity.Egg;
import com.retooling.batch.entity.Farm;

public class Step3Processor implements ItemProcessor<Egg, Egg>{

	private static final Logger logger = LoggerFactory.getLogger(Step3Processor.class);

	private Farm farm; 
	
	@Autowired
	private ApiCall apiCall;
	
	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;

	@Value("${batch.config.egg-to-chicken-days}")
	private int eggToChickensDays;
	
	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Egg process(Egg item) throws Exception {
		logger.info("Procesando huevo...");
		
		int eggsConvertToChicken = this.jobExecutionContext.getInt("EGGS_CONVERT_TO_CHICKEN");
		int chickensDiscarted = this.jobExecutionContext.getInt("CHICKENS_DISCARTED");
		String isChickensLimit = this.jobExecutionContext.getString("IS_CHICKENS_LIMIT");
		String isEggsLimit = this.jobExecutionContext.getString("IS_EGGS_LIMIT");
		
		Date currentDate;
		if (useDateSimulator) {
			currentDate = apiCall.getDate();
		} else {
			currentDate = new Date();
		}
		
		int diffDays = (int)((currentDate.getTime() - item.getCreationDate().getTime()) / 86400000);
		
		if (diffDays >= eggToChickensDays) {
			
			if (farm == null) {
				farm = apiCall.getFarm(item.getFarmId());
			}
			
	    	int chickensCount;
			try {
				chickensCount = apiCall.getChickensByFarm(item.getFarmId()).size();
			} catch (HttpClientErrorException.NotFound ex) {
				chickensCount = 0;
			}
			
			//si al insertar la gallina se excede el limite, entonces previamente descarto la gallina mas vieja
			if ((chickensCount+1) > farm.getChickenLimit()) {
				//descarto gallina
				Chicken chicken = apiCall.getOldChicken(farm.getFarmId());
				ChickenState chickenDiscarted = ChickenState.Discarded;
				chicken.setState(chickenDiscarted.getState());
				apiCall.updateChicken(chicken);
				logger.info("El pollo con id=[" + chicken.getChickenId() + "] se ha actualizado con estado '" + chickenDiscarted.getState() + "' (Descartado)");
				chickensDiscarted++;
			}
			
			//inserto gallina
			Chicken newChicken = new Chicken();
			newChicken.setFarmId(farm.getFarmId());
			ChickenState chickenAvailable = ChickenState.Available;
			newChicken.setState(chickenAvailable.getState());
			newChicken.setCreationDate(currentDate);
			ChickenOrigin chickenOrigin = ChickenOrigin.Grown;
			newChicken.setOrigin(chickenOrigin.getOrigin());
			newChicken.setLastEggDate(newChicken.getCreationDate());
			newChicken.setLastStateChangeDate(newChicken.getCreationDate());
			newChicken = apiCall.insertChicken(newChicken);
			logger.info("Se agrega pollo con id=[" + newChicken.getChickenId() + "] con estado '" + chickenAvailable.getState() + "' (Disponible)");
			
			//verifico si al sumar un huevo se alcanza el limite, y de ser asi, genero el reporte
			if ((chickensCount+1) == farm.getChickenLimit()) {
				logger.info("SE ALCANZO EL LIMITE DE GALLINAS!");
				isChickensLimit = "true";
			}
			chickensCount++;

			//actualizo el estado del huevo indicando que se convirtió en gallina
			EggState eggConvertToChicken = EggState.ConvertToChicken;
			item.setState(eggConvertToChicken.getState());
			item.setLastStateChangeDate(currentDate);
			
			//como hubo conversion, eso significa que se descontara 1 huevo y, por lo tanto, debo
			//desactivar el flag que indica que se alcanzo el limite de huevos
			if (isEggsLimit.equals("true")) {
				isEggsLimit = "false";
			}
			
			this.jobExecutionContext.putInt("EGGS_CONVERT_TO_CHICKEN", eggsConvertToChicken);
			this.jobExecutionContext.putInt("CHICKENS_DISCARTED", chickensDiscarted);
			this.jobExecutionContext.putString("IS_CHICKENS_LIMIT", isChickensLimit);
			this.jobExecutionContext.putString("IS_EGGS_LIMIT", isEggsLimit);
			
			return item;
		} else {
			return null;
		}
	}
}
