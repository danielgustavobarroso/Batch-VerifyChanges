package com.retooling.batch;

import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import com.retooling.batch.entity.Chicken;

public class Step2Writer implements ItemWriter<Chicken>{
	
	private static final Logger logger = LoggerFactory.getLogger(Step2Writer.class);
	
	@Autowired
	private ApiCall apiCall;
	
	@Override
	public void write(Chunk<? extends Chicken> chunk) throws Exception {

		if (chunk.isEmpty()) {
			logger.info("No hay gallina para modificar.");
		} else {
			Chicken chicken = chunk.getItems().get(0);
			apiCall.updateChicken(chicken);
			logger.info("Se actualizó la fecha de última puesta [lastEggDate='" + (new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(chicken.getLastEggDate())) + "'" + "'] de la gallina con id=[" + chicken.getChickenId() + "]");
		}			
	}
}
