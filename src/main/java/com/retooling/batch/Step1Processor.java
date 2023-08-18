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

	@Value("${batch.config.chicken-dead-days}")
	private int chickenDeadDays;

	@Value("#{jobExecution.executionContext}")
	private ExecutionContext jobExecutionContext;
	
	@Override
	public Chicken process(Chicken item) throws Exception {
		logger.info("Procesando gallina...");
		
		int chickensDead = this.jobExecutionContext.getInt("CHICKENS_DEAD");
		
		Date currentDate = apiCall.getDate();
		
		//marcar gallina como muerta si supera la cantidad de dias configurada
		if (this.getDifferenceInDays(currentDate, item.getCreationDate()) >= chickenDeadDays) {
			
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
	
	private int getDifferenceInDays(Date currentDate, Date itemDate) {
		return (int)((currentDate.getTime() - itemDate.getTime()) / 86400000);
	}
	
}
