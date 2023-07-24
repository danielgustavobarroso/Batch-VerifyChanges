package com.retooling.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.retooling.batch.entity.Chicken;
import com.retooling.batch.entity.Egg;

@Configuration
public class BatchConfig {

	@Bean
	public Job verifyChanges(JobRepository jobRepository, JobCompletionNotificationListener listener,
			Step step1, Step step2, Step step3) {
	    return new JobBuilder("verifyChanges", jobRepository)
	      .incrementer(new RunIdIncrementer())
	      .listener(listener)
	      .start(step1)
	      .next(step2)
	      .next(step3)
	      .build();
	}

	//Marcar gallinas como muertas si aplica
	@Bean
	public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("step1", jobRepository)
		  .<Chicken, Chicken> chunk(1, transactionManager)
		  .allowStartIfComplete(true)
	      .reader(step1Reader())
	      .processor(step1Processor())
	      .writer(step1Writer())
	      .build();
	}

	@Bean
	public Step1Reader step1Reader() {
		return new Step1Reader();
	}
	
	@JobScope
	@Bean
	public Step1Processor step1Processor() {
		return new Step1Processor();
	}
	
	@Bean
	public Step1Writer step1Writer() {
		return new Step1Writer();
	}

	//Generar nuevos huevos si aplica
	@Bean
	public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("step2", jobRepository)
		  .<Chicken, Chicken> chunk(1, transactionManager)
		  .allowStartIfComplete(true)
	      .reader(step2Reader())
	      .processor(step2Processor())
	      .writer(step2Writer())
	      .build();
	}
	
	@Bean
	public Step2Reader step2Reader() {
		return new Step2Reader();
	}

	@JobScope
	@Bean
	public Step2Processor step2Processor() {
		return new Step2Processor();
	}
	
	@Bean
	public Step2Writer step2Writer() {
		return new Step2Writer();
	}

	//Generar nuevas gallinas si aplica
	@Bean
	public Step step3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("step3", jobRepository)
		  .<Egg, Egg> chunk(1, transactionManager)
		  .allowStartIfComplete(true)
	      .reader(step3Reader())
	      .processor(step3Processor())
	      .writer(step3Writer())
	      .build();
	}
	
	@Bean
	public Step3Reader step3Reader() {
		return new Step3Reader();
	}

	@JobScope
	@Bean
	public Step3Processor step3Processor() {
		return new Step3Processor();
	}
	
	@Bean
	public Step3Writer step3Writer() {
		return new Step3Writer();
	}
	
}
