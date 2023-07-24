package com.retooling.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class BatchProcessApplication {

	private static final Logger logger = LoggerFactory.getLogger(BatchProcessApplication.class);
	
	public static void main(String[] args) {
		logger.info("Iniciando BatchProcessApplication...");
		SpringApplication.run(BatchProcessApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(); 
	}
	
}
