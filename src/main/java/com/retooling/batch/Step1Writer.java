package com.retooling.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.retooling.batch.entity.Chicken;

public class Step1Writer implements ItemWriter<Chicken>{
	
	private static final Logger logger = LoggerFactory.getLogger(Step1Writer.class);
	
	@Autowired
	private ApiCall apiCall;
	
	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;
	
	@Override
	public void write(Chunk<? extends Chicken> chunk) throws Exception {

		if (chunk.isEmpty()) {
			logger.info("No hay gallina para analizar.");
		} else {
			Chicken chicken = chunk.getItems().get(0);
			ChickenState chickenDead = ChickenState.Dead;
			apiCall.updateChicken(chicken);
			logger.info("El pollo con id=[" + chicken.getChickenId() + "] se ha actualizado con estado '" + chickenDead.getState() + "' (Muerto)");
		}			
	}
}
