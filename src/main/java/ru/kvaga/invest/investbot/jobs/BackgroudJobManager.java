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
			schedulerStocks.scheduleAtFixedRate(new UpdateCurrentPricesOfInstrumentsJob(new StockItem(), ConfigMap.TEMPLATE_URL_TINKOFF_STOCKS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS), 0, 3, TimeUnit.HOURS);
			log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfStocksJob for each 3 hours]");
			schedulerETFs.scheduleAtFixedRate(new UpdateCurrentPricesOfInstrumentsJob(new Etf(), 		ConfigMap.TEMPLATE_URL_TINKOFF_ETFS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS), 0, 3, TimeUnit.HOURS);
			log.info("BackgroudJobManager started with jobs [UpdateCurrentPricesOfETFsJob for each 3 hours]");
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
	    int todaysDayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
	    WorkingDay todaysWorkingDay = new WorkingDay(todaysDayOfWeekNumber);
		if(Settings.getInstance().getWorkingDays().contains(todaysWorkingDay)) {
			for(WorkingDay item : Settings.getInstance().getWorkingDays() ) {
				if(item.equals(todaysWorkingDay) && item.isWorkingDayBol()) {
					log.debug("Today ["+todaysWorkingDay+"] is a working day because working days are ["+Settings.getInstance().getWorkingDays()+"]");
					return true;
				}
			}
		}
		log.debug("Today ["+todaysWorkingDay+"] is not working day because working days are ["+Settings.getInstance().getWorkingDays()+"]");
		return false;
	}
	
	public static boolean isWorkingHours() throws JAXBException, IOException {
		Date currentDate = new Date();   // given date
//		Calendar calendarCurrent = GregorianCalendar.getInstance(); // creates a new calendar instance
//		calendarCurrent.setTime(date);   // assigns calendar to given date
		
		Calendar calFrom = GregorianCalendar.getInstance();
		calFrom.set(Calendar.HOUR_OF_DAY, Settings.getInstance().getWorkingHours().getHoursFrom());
		calFrom.set(Calendar.MINUTE, Settings.getInstance().getWorkingHours().getMinsFrom());
		
		Calendar calTo = GregorianCalendar.getInstance();
		calTo.set(Calendar.HOUR_OF_DAY, Settings.getInstance().getWorkingHours().getHoursTo());
		calTo.set(Calendar.MINUTE, Settings.getInstance().getWorkingHours().getMinsTo());
		Date from = calFrom.getTime();
		Date to = calTo.getTime();
		if(	currentDate.after(from) && currentDate.before(to)) { 
			log.debug("Current time ["+currentDate+"] is in the range from ["+from+"] to ["+to+"]");
			return true;
		}
		log.debug("Current time ["+currentDate+"] is out of range from ["+from+"] to ["+to+"]");
		return false;
	}
}
