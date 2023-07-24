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

public class Step2Processor implements ItemProcessor<Chicken, Chicken>{

	private static final Logger logger = LoggerFactory.getLogger(Step2Processor.class);

	private Farm farm; 
	
	@Autowired
	private ApiCall apiCall;
	
	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;

	@Value("${batch.config.days-amount-eggs}")
	private int daysAmountEggs;	
	
	@Value("${batch.config.eggs-amount-by-chicken}")
	private int eggsAmountByChicken;
	
	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Chicken process(Chicken item) throws Exception {
		logger.info("Procesando gallina...");
		
		int chickensPutEggs = this.jobExecutionContext.getInt("CHICKENS_PUT_EGGS");
		int newEggsByChicken = this.jobExecutionContext.getInt("NEW_EGGS_BY_CHICKEN");
		int eggsDiscarted = this.jobExecutionContext.getInt("EGGS_DISCARTED");
		String isEggsLimit = this.jobExecutionContext.getString("IS_EGGS_LIMIT");
		
		Date currentDate;
		if (useDateSimulator) {
			currentDate = apiCall.getDate();
		} else {
			currentDate = new Date();
		}
		
		int diffDays = (int)((currentDate.getTime() - item.getLastEggDate().getTime()) / 86400000);
		
		if (diffDays >= daysAmountEggs) {
			
			if (farm == null) {
				farm = apiCall.getFarm(item.getFarmId());
			}
			
	    	int eggsCount;
			try {
				eggsCount = apiCall.getEggsByFarm(item.getFarmId()).size();
			} catch (HttpClientErrorException.NotFound ex) {
				eggsCount = 0;
			}
			
			for (int i=0; i < eggsAmountByChicken;i++) {
				//si al insertar el huevo se excede el limite, entonces previamente descarto el huevo mas viejo antes de insertar
				if ((eggsCount+1) > farm.getEggLimit()) {
					//descarto huevo
					Egg egg = apiCall.getOldEgg(farm.getFarmId());
					EggState eggDiscarted = EggState.Discarded;
					egg.setState(eggDiscarted.getState());
					apiCall.updateEgg(egg);
					logger.info("El huevo con id=[" + egg.getEggId() + "] se ha actualizado con estado '" + eggDiscarted.getState() + "' (Descartado)");
					eggsDiscarted++;
				}
				
				//inserto huevo
				Egg newEgg = new Egg();
				newEgg.setFarmId(item.getFarmId());
				EggState eggAvailable = EggState.Available;
				newEgg.setState(eggAvailable.getState());
				newEgg.setCreationDate(currentDate);
				EggOrigin eggOrigin = EggOrigin.Deposited;
				newEgg.setOrigin(eggOrigin.getOrigin());
				newEgg.setLastStateChangeDate(currentDate);
				newEgg = apiCall.insertEgg(newEgg);
				logger.info("Se agrega huevo con id=[" + newEgg.getEggId() + "] con estado '" + eggAvailable.getState() + "' (Disponible)");
				
				//verifico si al sumar un huevo se alcanza el limite, y de ser asi, genero el reporte
				if ((eggsCount+1) == farm.getEggLimit()) {
					logger.info("SE ALCANZO EL LIMITE DE HUEVOS!");
					isEggsLimit = "true";
				}
				
				newEggsByChicken++;
				eggsCount++;
			}
			item.setLastEggDate(currentDate);
			chickensPutEggs++;

			this.jobExecutionContext.putInt("CHICKENS_PUT_EGGS", chickensPutEggs);
			this.jobExecutionContext.putInt("NEW_EGGS_BY_CHICKEN", newEggsByChicken);
			this.jobExecutionContext.putInt("EGGS_DISCARTED", eggsDiscarted);
			this.jobExecutionContext.putString("IS_EGGS_LIMIT", isEggsLimit);
			
			return item;
		} else {
			return null;
		}
	}
}
