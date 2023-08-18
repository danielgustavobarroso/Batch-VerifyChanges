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

public class Step2Processor implements ItemProcessor<Chicken, Chicken>{

	private static final Logger logger = LoggerFactory.getLogger(Step2Processor.class);

	int chickensPutEggs;
	int newEggsByChicken;
	int eggsDiscarted;
	String isEggsLimit;
	
	@Autowired
	private ApiCall apiCall;

	@Value("${batch.config.days-amount-eggs}")
	private int daysAmountEggs;	
	
	@Value("${batch.config.eggs-amount-by-chicken}")
	private int eggsAmountByChicken;
	
	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Chicken process(Chicken item) throws Exception {
		logger.info("Procesando gallina...");
		
		this.getJobExecutionContextValues();
		
		Date currentDate = apiCall.getDate();
		
		if (this.getDifferenceInDays(currentDate, item.getLastEggDate()) >= daysAmountEggs) {
			
			Farm farm = apiCall.getFarm(item.getFarmId());
	    	int eggsCount = apiCall.getEggsByFarm(item.getFarmId()).size();
			
			for (int i=0; i < eggsAmountByChicken;i++) {
				insertEgg (item.getFarmId(), eggsCount+1, farm.getEggLimit(), currentDate);
				
				setEggLimitFlagIfApplied(eggsCount+1, farm.getEggLimit());
				
				this.newEggsByChicken++;
				eggsCount++;
			}
			item.setLastEggDate(currentDate);
			this.chickensPutEggs++;

			setJobExecutionContextValues();
			
			return item;
		} else {
			return null;
		}
	}
	
	private void getJobExecutionContextValues() {
		this.chickensPutEggs = this.jobExecutionContext.getInt("CHICKENS_PUT_EGGS");
		this.newEggsByChicken = this.jobExecutionContext.getInt("NEW_EGGS_BY_CHICKEN");
		this.eggsDiscarted = this.jobExecutionContext.getInt("EGGS_DISCARTED");
		this.isEggsLimit = this.jobExecutionContext.getString("IS_EGGS_LIMIT");
	}
	
	private void setJobExecutionContextValues() {
		this.jobExecutionContext.putInt("CHICKENS_PUT_EGGS", chickensPutEggs);
		this.jobExecutionContext.putInt("NEW_EGGS_BY_CHICKEN", newEggsByChicken);
		this.jobExecutionContext.putInt("EGGS_DISCARTED", eggsDiscarted);
		this.jobExecutionContext.putString("IS_EGGS_LIMIT", isEggsLimit);
	}

	private int getDifferenceInDays(Date currentDate, Date itemDate) {
		return (int)((currentDate.getTime() - itemDate.getTime()) / 86400000);
	}
	
	private void discardOldEgg(String farmId) {
		Egg egg = apiCall.getOldEgg(farmId);
		EggState eggDiscarted = EggState.Discarded;
		egg.setState(eggDiscarted.getState());
		apiCall.updateEgg(egg);
		logger.info("El huevo con id=[" + egg.getEggId() + "] se ha actualizado con estado '" + eggDiscarted.getState() + "' (Descartado)");
	}

	private void insertEgg (String farmId, int eggsCount, long eggLimit, Date currentDate) {
		//si al insertar el huevo se excede el limite, entonces previamente descarto el huevo mas viejo antes de insertar
		if (eggsCount > eggLimit) {
			this.discardOldEgg(farmId);
			this.eggsDiscarted++;
		}
		Egg newEgg = new Egg();
		newEgg.setFarmId(farmId);
		EggState eggAvailable = EggState.Available;
		newEgg.setState(eggAvailable.getState());
		newEgg.setCreationDate(currentDate);
		EggOrigin eggOrigin = EggOrigin.Deposited;
		newEgg.setOrigin(eggOrigin.getOrigin());
		newEgg.setLastStateChangeDate(newEgg.getCreationDate());
		newEgg = apiCall.insertEgg(newEgg);
		logger.info("Se agrega huevo con id=[" + newEgg.getEggId() + "] con estado '" + eggAvailable.getState() + "' (Disponible)");
	}
	
	private void setEggLimitFlagIfApplied(int eggCount, long eggLimit) {
		//si se alcanza el limite de huevos, entonces seteo flag para generar el reporte
		if (eggCount == eggLimit) {
			logger.info("SE ALCANZO EL LIMITE DE HUEVOS!");
			this.isEggsLimit = "true";
		}
	}
	
}
