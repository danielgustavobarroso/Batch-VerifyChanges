package com.retooling.batch;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.retooling.batch.entity.Chicken;
import com.retooling.batch.entity.Egg;
import com.retooling.batch.entity.Farm;

public class Step3Processor implements ItemProcessor<Egg, Egg>{

	private static final Logger logger = LoggerFactory.getLogger(Step3Processor.class);

	int eggsConvertToChicken;
	int chickensDiscarted;
	String isChickensLimit;
	String isEggsLimit;
	
	@Autowired
	private ApiCall apiCall;

	@Value("${batch.config.egg-to-chicken-days}")
	private int eggToChickensDays;
	
	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Egg process(Egg item) throws Exception {
		logger.info("Procesando huevo...");
		
		getJobExecutionContextValues();
		
		Date currentDate = apiCall.getDate();
		
		if (this.getDifferenceInDays(currentDate, item.getCreationDate()) >= eggToChickensDays) {
			
			Farm farm = apiCall.getFarm(item.getFarmId());
			
	    	int chickensCount = apiCall.getChickensByFarm(item.getFarmId()).size();
			
			insertChicken (item.getFarmId(), chickensCount+1, farm.getChickenLimit(), currentDate);
			
			setChickenLimitFlagIfApplied(chickensCount+1, farm.getChickenLimit());
			
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
			
			setJobExecutionContextValues();
			
			return item;
		} else {
			return null;
		}
	}

	private int getDifferenceInDays(Date currentDate, Date itemDate) {
		return (int)((currentDate.getTime() - itemDate.getTime()) / 86400000);
	}
	
	private void getJobExecutionContextValues() {
		this.eggsConvertToChicken = this.jobExecutionContext.getInt("EGGS_CONVERT_TO_CHICKEN");
		this.chickensDiscarted = this.jobExecutionContext.getInt("CHICKENS_DISCARTED");
		this.isChickensLimit = this.jobExecutionContext.getString("IS_CHICKENS_LIMIT");
		this.isEggsLimit = this.jobExecutionContext.getString("IS_EGGS_LIMIT");
	}

	private void setJobExecutionContextValues() {
		this.jobExecutionContext.putInt("EGGS_CONVERT_TO_CHICKEN", eggsConvertToChicken);
		this.jobExecutionContext.putInt("CHICKENS_DISCARTED", chickensDiscarted);
		this.jobExecutionContext.putString("IS_CHICKENS_LIMIT", isChickensLimit);
		this.jobExecutionContext.putString("IS_EGGS_LIMIT", isEggsLimit);
	}

	private void discardOldChicken(String farmId) {
		Chicken chicken = apiCall.getOldChicken(farmId);
		ChickenState chickenDiscarted = ChickenState.Discarded;
		chicken.setState(chickenDiscarted.getState());
		apiCall.updateChicken(chicken);
		logger.info("El pollo con id=[" + chicken.getChickenId() + "] se ha actualizado con estado '" + chickenDiscarted.getState() + "' (Descartado)");		
	}

	private void insertChicken (String farmId, int chickensCount, long chickenLimit, Date currentDate) {
		//si al insertar la gallina se excede el limite, entonces previamente descarto la gallina mas vieja antes de insertar
		if (chickensCount > chickenLimit) {
			this.discardOldChicken(farmId);
			chickensDiscarted++;
		}
		Chicken newChicken = new Chicken();
		newChicken.setFarmId(farmId);
		ChickenState chickenAvailable = ChickenState.Available;
		newChicken.setState(chickenAvailable.getState());
		newChicken.setCreationDate(currentDate);
		ChickenOrigin chickenOrigin = ChickenOrigin.Grown;
		newChicken.setOrigin(chickenOrigin.getOrigin());
		newChicken.setLastEggDate(newChicken.getCreationDate());
		newChicken.setLastStateChangeDate(newChicken.getCreationDate());
		newChicken = apiCall.insertChicken(newChicken);
		logger.info("Se agrega pollo con id=[" + newChicken.getChickenId() + "] con estado '" + chickenAvailable.getState() + "' (Disponible)");
	}

	private void setChickenLimitFlagIfApplied(int chickenCount, long chickenLimit) {
		//si se alcanza el limite de gallinas, entonces seteo flag para generar el reporte
		if (chickenCount == chickenLimit) {
			logger.info("SE ALCANZO EL LIMITE DE GALLINAS!");
			isChickensLimit = "true";
		}
	}
	
}
