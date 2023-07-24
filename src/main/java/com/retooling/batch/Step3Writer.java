package com.retooling.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import com.retooling.batch.entity.Egg;

public class Step3Writer implements ItemWriter<Egg>{
	
	private static final Logger logger = LoggerFactory.getLogger(Step3Writer.class);
	
	@Autowired
	private ApiCall apiCall;
	
	@Override
	public void write(Chunk<? extends Egg> chunk) throws Exception {

		if (chunk.isEmpty()) {
			logger.info("No hay huevo para modificar.");
		} else {
			Egg egg = chunk.getItems().get(0);
			EggState eggConvertToChicken = EggState.ConvertToChicken;
			egg.setState(eggConvertToChicken.getState());
			apiCall.updateEgg(egg);
			logger.info("El huevo con id=[" + egg.getEggId() + "] con estado '" + eggConvertToChicken.getState() + "' (Convertido en gallina)");
		}			
	}
}
