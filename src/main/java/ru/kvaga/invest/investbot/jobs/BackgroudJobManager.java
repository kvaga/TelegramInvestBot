package ru.kvaga.invest.investbot.jobs;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import telegrambot.ConfigMap;


public class BackgroudJobManager {
	private static ScheduledExecutorService schedulerStocks;
	private static ScheduledExecutorService schedulerBonds;
	private static ScheduledExecutorService schedulerETFs;

	final static Logger log = LogManager.getLogger(BackgroudJobManager.class);

	public static void init (File listOfStocksFile, File listOfETFsFile, File listOfBondsFile) {
		log.info("Try to start BackgroudJobManager");
		schedulerStocks = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("stocks-job-%d").build());
		schedulerETFs = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("etfs-job-%d").build());
		schedulerBonds = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("bonds-job-%d").build());

//		scheduler.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob(listOfStocksFile), 0, 60, TimeUnit.SECONDS);
		
		schedulerStocks.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob("Stock",listOfStocksFile, ConfigMap.URL_TEXT_TINKOFF_STOCKS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS), 0, 4, TimeUnit.HOURS);
		log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfStocksJob for each 4 hours]");
		
		schedulerETFs.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob("ETF", listOfETFsFile, ConfigMap.URL_TEXT_TINKOFF_ETFS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS), 0, 4, TimeUnit.HOURS);
		log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfETFsJob for each 4 hours]");
		
		schedulerBonds.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob("Bond", listOfBondsFile, ConfigMap.URL_TEXT_TINKOFF_BONDS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS), 0, 4, TimeUnit.HOURS);
		log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfBondsJob for each 4 hours]");
		
		
//		System.out.println("BackgroudJobManager started: " + event);
//		https://examples.javacodegeeks.com/enterprise-java/quartz/quartz-cron-schedule-example/
//		Scheduler scheduler = Scheduler
//        scheduler.schedule("5 8,21 * * 1-5", new UpdateCurrentPricesOfStocksJob(listOfStocksFile));
//        scheduler.start();
//        servletContextEvent.getServletContext().setAttribute("SCHEDULER", scheduler);
	}
	

    public static  void destroy() {
		log.info("BackgroudJobManager destroyed");
		schedulerStocks.shutdownNow();    
		schedulerETFs.shutdownNow();    
		schedulerBonds.shutdownNow();    

    }
}
