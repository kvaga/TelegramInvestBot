package ru.kvaga.invest.investbot.jobs;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.investments.etfs.Etf;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.telegrambot.web.server.servlets.WorkingDay;
import telegrambot.ConfigMap;
import telegrambot.Settings;


public class BackgroudJobManager {
	private static ScheduledExecutorService schedulerStocks;
	private static ScheduledExecutorService schedulerBonds;
	private static ScheduledExecutorService schedulerETFs;
	private static ScheduledExecutorService schedulerBondsProfitability;

	final static Logger log = LogManager.getLogger(BackgroudJobManager.class);

	public static void init () {
		log.info("Init BackgroudJobManager started");
//		File listOfStocksFile	, File listOfETFsFile, File listOfBondsFile;
		schedulerStocks = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("stocks-job-%d").build());
		schedulerETFs = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("etfs-job-%d").build());
		schedulerBonds = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("bonds-job-%d").build());
		schedulerBondsProfitability = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("bondsprofitability-job-%d").build());

//		scheduler.scheduleAtFixedRate(new UpdateCurrentPricesOfStocksJob(listOfStocksFile), 0, 60, TimeUnit.SECONDS);
		
		if(!ConfigMap.TEST_MODE) {
			schedulerStocks.scheduleAtFixedRate(new UpdateCurrentPricesOfInstrumentsJob(new StockItem(), ConfigMap.TEMPLATE_URL_TINKOFF_STOCKS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS), 0, 1, TimeUnit.HOURS);
			log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfStocksJob for each 10 minutes]");
			schedulerETFs.scheduleAtFixedRate(new UpdateCurrentPricesOfInstrumentsJob(new Etf(), 		ConfigMap.TEMPLATE_URL_TINKOFF_ETFS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS), 0, 1, TimeUnit.HOURS);
			log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfETFsJob for each 10 minutes]");
//			schedulerBondsProfitability.scheduleAtFixedRate(new UpdateProfitabilityOfBonds(), 0, 24, TimeUnit.HOURS);			
//			log.info("BackgroudJobManager started with jobs [UpdateProfitabilityOfBonds for each 24 hours]");
		}
//		schedulerBonds.scheduleAtFixedRate(new UpdateCurrentPricesOfInstrumentsJob(new Bond(),  		ConfigMap.TEMPLATE_URL_TINKOFF_BONDS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS), 0, 4, TimeUnit.HOURS);
//		log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfBondsJob for each 4 hours]");
		
		log.info("Init BackgroudJobManager finished");

	}
	

    public static  void destroy() {
		log.info("BackgroudJobManager destroyed");
		if(schedulerStocks!=null)
			schedulerStocks.shutdownNow();    
		if(schedulerETFs!=null)
			schedulerETFs.shutdownNow();    
		if(schedulerBonds!=null)
			schedulerBonds.shutdownNow();    

    }


	public static boolean isWorkingDay() throws JAXBException, IOException {
		Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date());
	    int todaysDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if(!Settings.getInstance().getWorkingDays().contains(new WorkingDay(todaysDayOfWeek))) {
			log.debug("Today ["+todaysDayOfWeek+"] is not working day because working days are ["+Settings.getInstance().getWorkingDays()+"]");
			return false;
		}
		return true;
	}
	
	public static boolean isWorkingHours() throws JAXBException, IOException {
		Date date = new Date();   // given date
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		calendar.setTime(date);   // assigns calendar to given date 
		if(calendar.get(Calendar.HOUR_OF_DAY)>=Settings.getInstance().getWorkingHours().getHoursFrom() &&
				calendar.get(Calendar.HOUR_OF_DAY)<=Settings.getInstance().getWorkingHours().getHoursTo() &&
						calendar.get(Calendar.MINUTE)>=Settings.getInstance().getWorkingHours().getMinsFrom() &&
						calendar.get(Calendar.MINUTE)<=Settings.getInstance().getWorkingHours().getMinsTo()
				) {
			return true;
		}
		log.debug("Current hours ["+Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+"] are not working because working hours are ["+Settings.getInstance().getWorkingHours().getHoursFrom()+":"+Settings.getInstance().getWorkingHours().getMinsFrom()+"-"+Settings.getInstance().getWorkingHours().getHoursTo()+":"+Settings.getInstance().getWorkingHours().getMinsTo()+"]");

		return false;
	}
}
