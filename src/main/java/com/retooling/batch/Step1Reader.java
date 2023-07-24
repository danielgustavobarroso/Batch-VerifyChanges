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
import com.retooling.batch.entity.Chicken;

public class Step1Reader implements ItemReader<Chicken> {

	private static final Logger logger = LoggerFactory.getLogger(Step1Reader.class);

	private int nextChickenIndex;
	private List<Chicken> chickenData;

	@Autowired
	ApiCall apiCall;
	
	public Step1Reader() {
		nextChickenIndex = 0;
	}
	
	@Override
	public Chicken read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		
		if (chickenDataIsNotInitialized()) {
			logger.info("Leyendo la información de gallinas...");
			try {
				chickenData = fetchChickenDatafromAPI();
			} catch (HttpClientErrorException.NotFound ex) {
				return null;
			}
		}
		
		Chicken nextChicken = null;
		if (nextChickenIndex < chickenData.size()) {
			nextChicken = chickenData.get(nextChickenIndex);
			nextChickenIndex++;
		} else {
			nextChickenIndex = 0;
			chickenData = null;
		}
				
		return nextChicken;
	}

	private boolean chickenDataIsNotInitialized() {
		return this.chickenData == null;
	}
	
	private List<Chicken> fetchChickenDatafromAPI() {
		return apiCall.getChickensByFarm("1");
	}
	
}
