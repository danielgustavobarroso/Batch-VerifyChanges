package com.retooling.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {
	
    private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

	final private String farmId = "1";
    
	@Autowired
	private ApiCall apiCall;
    
	@Value("${api.microservice.use-date-simulator}")
	private boolean useDateSimulator;
	
    @Override
    public void beforeJob(JobExecution jobExecution) { 
    	
    	this.initializeContext(jobExecution);
    	
    	Date currentDate = null;
		try {
			currentDate = apiCall.getDate();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	int eggsCount = apiCall.getEggsByFarm(farmId).size();
				
    	int chickensCount = apiCall.getChickensByFarm(farmId).size();
    	
    	writeInitialInfoInLog(currentDate, eggsCount, chickensCount);
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
    	
		Date currentDate = null;
		try {
			currentDate = apiCall.getDate();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	int eggsCount = apiCall.getEggsByFarm(farmId).size();
				
    	int chickensCount = apiCall.getChickensByFarm(farmId).size();
    	
    	writeFinalInfoInLog(currentDate, eggsCount, chickensCount, jobExecution);
		
    	generateReportIfApplied(jobExecution);
    	
        logger.info("!!! EL JOB FINALIZO! Por favor verificar los resultados");
    }
    
    private void initializeContext(JobExecution jobExecution) {
    	//step1
    	jobExecution.getExecutionContext().putInt("CHICKENS_DEAD", 0);
    	
    	//step2
    	jobExecution.getExecutionContext().putInt("CHICKENS_PUT_EGGS", 0);
    	jobExecution.getExecutionContext().putInt("NEW_EGGS_BY_CHICKEN", 0);
    	jobExecution.getExecutionContext().putInt("EGGS_DISCARTED", 0);
    	jobExecution.getExecutionContext().putString("IS_EGGS_LIMIT", "false");

    	//step3
    	jobExecution.getExecutionContext().putInt("EGGS_CONVERT_TO_CHICKEN", 0);
    	jobExecution.getExecutionContext().putInt("CHICKENS_DISCARTED", 0);
    	jobExecution.getExecutionContext().putString("IS_CHICKENS_LIMIT", "false");
    }
    
    private void writeInitialInfoInLog(Date currentDate, int eggsCount, int chickensCount) {
		logger.info("********************************************************************************");
		logger.info("Fecha y hora de inicio: " + (new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(currentDate)));
    	logger.info("Cantidad de huevos disponibles ANTES de la ejecución del job: " + eggsCount);
    	logger.info("Cantidad de gallinas disponibles ANTES de la ejecución del job: " + chickensCount);
    	logger.info("********************************************************************************");
    }
   
    private void writeFinalInfoInLog(Date currentDate, int eggsCount, int chickensCount, JobExecution jobExecution) {
		logger.info("********************************************************************************");
		logger.info("Fecha y hora de fin: " + (new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(currentDate)));
    	logger.info("Cantidad de huevos disponibles DESPUES de la ejecución del job: " + eggsCount);
    	logger.info("Cantidad de gallinas disponibles DESPUES de la ejecución del job: " + chickensCount);
    	logger.info("********************************************************************************");
    	logger.info("Resumen Step 1:");
    	logger.info("Cantidad de gallinas marcadas como muertas: " + jobExecution.getExecutionContext().getInt("CHICKENS_DEAD"));
    	logger.info("********************************************************************************");
    	logger.info("Resumen Step 2");
    	logger.info("Cantidad de gallinas que pusieron huevos: " + jobExecution.getExecutionContext().getInt("CHICKENS_PUT_EGGS"));
    	logger.info("Cantidad de huevos que pusieron las gallinas: " + jobExecution.getExecutionContext().getInt("NEW_EGGS_BY_CHICKEN"));
    	logger.info("Cantidad de huevos descartados: " + jobExecution.getExecutionContext().getInt("EGGS_DISCARTED"));
    	logger.info("********************************************************************************");
    	logger.info("Resumen Step 3");
    	logger.info("Cantidad de gallinas que crecieron de un huevo: " + jobExecution.getExecutionContext().getInt("EGGS_CONVERT_TO_CHICKEN"));
    	logger.info("Cantidad de gallinas descartadas: " + jobExecution.getExecutionContext().getInt("CHICKENS_DISCARTED"));
    	logger.info("********************************************************************************");
    }
    
    private void generateReportIfApplied(JobExecution jobExecution) {
    	if (jobExecution.getExecutionContext().getString("IS_EGGS_LIMIT").equals("true") ||
    			jobExecution.getExecutionContext().getString("IS_CHICKENS_LIMIT").equals("true")) {
    		logger.info("Genero reporte de situación de granja por haberse alcanzado uno de los límites");
			apiCall.generateReport(farmId);
    	}
    }
    
}
