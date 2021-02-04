package ru.kvaga.invest.investbot.jobs;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;


public class BackgroudJobManager {
	private static ScheduledExecutorService scheduler;
	final static Logger log = LogManager.getLogger(BackgroudJobManager.class);

	public static void init (File listOfStocksFile) {
		log.info("Try to start BackgroudJobManager");
		scheduler = Executors.newSingleThreadScheduledExecutor();
//		scheduler.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob(listOfStocksFile), 0, 60, TimeUnit.SECONDS);
		
		scheduler.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob(listOfStocksFile), 0, 4, TimeUnit.HOURS);
		log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfStocksJob for each 4 hours]");
		
		
//		System.out.println("BackgroudJobManager started: " + event);
//		https://examples.javacodegeeks.com/enterprise-java/quartz/quartz-cron-schedule-example/
//		Scheduler scheduler = Scheduler
//        scheduler.schedule("5 8,21 * * 1-5", new UpdateCurrentPricesOfStocksJob(listOfStocksFile));
//        scheduler.start();
//        servletContextEvent.getServletContext().setAttribute("SCHEDULER", scheduler);
	}
	

    public static  void destroy() {
		log.info("BackgroudJobManager destroyed");
		scheduler.shutdownNow();    }
}
