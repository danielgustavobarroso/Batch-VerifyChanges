package com.retooling.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.retooling.batch.entity.Chicken;
import com.retooling.batch.entity.Egg;
import com.retooling.batch.entity.Farm;

@Service
public class ApiCall {

	private static final Logger logger = LoggerFactory.getLogger(ApiCall.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${api.microservice.farm}")
	private String urlFarm;
	
	@Value("${api.microservice.egg}")
	private String urlEgg;
	
	@Value("${api.microservice.chicken}")
	private String urlChicken;

	@Value("${api.microservice.report}")
	private String urlReport;
	
	@Value("${api.microservice.date-simulator}")
	private String urlDateSimulator;

	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;
	
	public ApiCall() {
		super();
	}
	
	public Farm getFarm(String id) {
		logger.info("Service - Calling getFarm...");
		return restTemplate.getForObject(urlFarm+"/{id}", Farm.class, id);
	}
	
	public List<Egg> getEggsByFarm(String idFarm) {
		try {
			logger.info("Service - Calling getEggs...");
			EggState eggAvailable = EggState.Available;
			return Arrays.asList(restTemplate.getForObject(urlEgg+"/farms/{idFarm}", Egg[].class, idFarm))
				.stream().filter(c -> c.getState().equals(eggAvailable.getState())).collect(Collectors.toList());
		} catch (HttpClientErrorException.NotFound ex) {
			return new ArrayList<>();
		}
	}

	public List<Chicken> getChickensByFarm(String idFarm) {
		try {
			logger.info("Service - Calling getChickens...");
			ChickenState chickenAvailable = ChickenState.Available;
			return Arrays.asList(restTemplate.getForObject(urlChicken+"/farms/{idFarm}", Chicken[].class, idFarm))
				.stream().filter(c -> c.getState().equals(chickenAvailable.getState())).collect(Collectors.toList());
		} catch (HttpClientErrorException.NotFound ex) {
			return new ArrayList<>();
		}
	}

	public Chicken getOldChicken(String idFarm) {
		logger.info("Service - Calling getOldChicken...");
		ChickenState chickenAvailable = ChickenState.Available;
		return Arrays.asList(restTemplate.getForObject(urlChicken+"/farms/{idFarm}", Chicken[].class, idFarm))
				.stream().filter(c -> c.getState().equals(chickenAvailable.getState())).collect(Collectors.toList()).get(0);
	}

	public Egg getOldEgg(String idFarm) {
		logger.info("Service - Calling getOldEgg...");
		EggState eggAvailable = EggState.Available;
		return Arrays.asList(restTemplate.getForObject(urlEgg+"/farms/{idFarm}", Egg[].class, idFarm))
				.stream().filter(e -> e.getState().equals(eggAvailable.getState())).collect(Collectors.toList()).get(0);
	}
	
	public Egg insertEgg(Egg egg) {
		logger.info("Service - Calling insertEgg...");
		return restTemplate.postForObject(urlEgg, egg, Egg.class);
	}
	
	public void updateChicken(Chicken chicken) {
		logger.info("Service - Calling updateChicken...");
		restTemplate.put(urlChicken, chicken, Chicken.class);
	}

	public Chicken insertChicken(Chicken chicken) {
		logger.info("Service - Calling insertChicken...");
		return restTemplate.postForObject(urlChicken, chicken, Chicken.class);
	}
	
	public void updateEgg(Egg egg) {
		logger.info("Service - Calling updateEgg...");
		restTemplate.put(urlEgg, egg, Egg.class);
	}
	
	public void generateReport(String idFarm) {
		logger.info("Service - Calling generateReport...");
		restTemplate.postForObject(urlReport+"/currentStatusReport/generateFile", idFarm, String.class);
	}

	public Date getDate() throws ParseException {
		logger.info("Service - Calling getDate...");
		if (useDateSimulator) {
			String dateStr = restTemplate.getForObject(urlDateSimulator+"/get-date", String.class);
			return (new SimpleDateFormat("yyyyMMddHHmmss").parse(dateStr));
		} else {
			return new Date();
		}
	}
	
}
