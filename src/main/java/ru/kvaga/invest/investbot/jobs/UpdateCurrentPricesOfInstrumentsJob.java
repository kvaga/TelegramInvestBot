package ru.kvaga.invest.investbot.jobs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.lib.InstrumentsTrackingLib;
import ru.kvaga.telegrambot.web.server.servlets.WorkingDay;
//
//import ru.kvaga.rss.feedaggr.Exec;
//import ru.kvaga.rss.feedaggr.FeedAggrException.GetSubstringForHtmlBodySplitException;
//import ru.kvaga.rss.feedaggr.FeedAggrException.GetURLContentException;
//import ru.kvaga.rss.feedaggr.FeedAggrException.SplitHTMLContent;
//import ru.kvaga.rss.feedaggr.Item;
//import ru.kvaga.rss.feedaggr.objects.Channel;
//import ru.kvaga.rss.feedaggr.objects.GUID;
//import ru.kvaga.rss.feedaggr.objects.RSS;
//import ru.kvaga.rss.feedaggr.objects.utils.ObjectsUtils;
//import ru.kvaga.rss.feedaggrwebserver.objects.user.User;
//import ru.kvaga.rss.feedaggrwebserver.objects.user.UserFeed;
import telegrambot.ConfigMap;
import telegrambot.Settings;

public class UpdateCurrentPricesOfInstrumentsJob implements Runnable {
	final static Logger log = LogManager.getLogger(UpdateCurrentPricesOfInstrumentsJob.class);
	private String urlTextTinkoff = null;
	private String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME;
	private String label=null;
	private Instrument instrument;

	private boolean forcedBol = true;

	public UpdateCurrentPricesOfInstrumentsJob(boolean forced, Instrument instrument, String urlTextTinkoff, String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME) {
		this.forcedBol = forced;
		this.urlTextTinkoff=urlTextTinkoff;
		this.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME=REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME;
		this.label=instrument.getClass().toString();
		this.instrument=instrument;
	}
	
	public UpdateCurrentPricesOfInstrumentsJob(Instrument instrument, String urlTextTinkoff, String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME) {
		this.urlTextTinkoff=urlTextTinkoff;
		this.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME=REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME;
		this.label=instrument.getClass().toString();
		this.instrument=instrument;

	}

	void updateStocks() {
		try {
			InstrumentsTrackingLib.updateCurrentPricesOfStocks(instrument, urlTextTinkoff, REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME);
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}

	public void run() {
		
		
		log.info("UpdateCurrentPricesOfStocksJob started");
		if (!forcedBol) {
			if (!ConfigMap.jobsEnabledBol) {
				log.info("UpdateCurrentPricesOfStocksJob disabled by user. Finished.");
				return;
			}
		}
		
		try {
			if(!BackgroudJobManager.isWorkingDay()) {
				return;
			}
			if(!BackgroudJobManager.isWorkingHours()) {
				return;
			}
			updateStocks();
			log.debug("UpdateCurrentPricesOfStocksJob finished");
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}
}
