package com.retooling.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJob {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledJob.class);
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	Job job;
	
	@Scheduled(fixedRateString = "${fixedDelay.in.milliseconds}")
	public void perform() throws Exception {
		logger.info("Se ejecuta el job...");
		JobParameters params = new JobParametersBuilder()
			.toJobParameters();
		jobLauncher.run(job, params);
	}
	
}
