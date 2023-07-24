package com.retooling.batch;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.retooling.batch.entity.Chicken;

public class Step1Processor implements ItemProcessor<Chicken, Chicken>{

	private static final Logger logger = LoggerFactory.getLogger(Step1Processor.class);

	@Autowired
	private ApiCall apiCall;
	
	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;

	@Value("${batch.config.chicken-dead-days}")
	private int chickenDeadDays;

	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Chicken process(Chicken item) throws Exception {
		logger.info("Procesando gallina...");
		
		int chickensDead = this.jobExecutionContext.getInt("CHICKENS_DEAD");
		
		Date currentDate;
		if (useDateSimulator) {
			currentDate = apiCall.getDate();
		} else {
			currentDate = new Date();
		}
		
		//marcar pollo como muerto si supera la cantidad de dias configurada
		int diffDays = (int)((currentDate.getTime() - item.getCreationDate().getTime()) / 86400000);
			
		if (diffDays >= chickenDeadDays) {
			
			ChickenState chickenDead = ChickenState.Dead;
			item.setState(chickenDead.getState());
			item.setLastStateChangeDate(currentDate);
			chickensDead++;
			this.jobExecutionContext.putInt("CHICKENS_DEAD", chickensDead);
			return item;
		} else {
			return null;	
		}
	}
	
}
