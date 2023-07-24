package com.retooling.batch;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import com.retooling.batch.entity.Egg;

public class Step3Reader implements ItemReader<Egg> {

	private static final Logger logger = LoggerFactory.getLogger(Step3Reader.class);

	private int nextEggIndex;
	private List<Egg> eggData;

	@Autowired
	ApiCall apiCall;
	
	public Step3Reader() {
		nextEggIndex = 0;
	}
	
	@Override
	public Egg read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		
		if (eggDataIsNotInitialized()) {
			logger.info("Leyendo la información de huevos...");
			try {
				eggData = fetchEggDatafromAPI();
			} catch (HttpClientErrorException.NotFound ex) {
				return null;
			}
		}
		
		Egg nextEgg = null;
		if (nextEggIndex < eggData.size()) {
			nextEgg = eggData.get(nextEggIndex);
			nextEggIndex++;
		} else {
			nextEggIndex = 0;
			eggData = null;
		}
				
		return nextEgg;
	}

	private boolean eggDataIsNotInitialized() {
		return this.eggData == null;
	}
	
	private List<Egg> fetchEggDatafromAPI() {
		return apiCall.getEggsByFarm("1");
	}
	
}
