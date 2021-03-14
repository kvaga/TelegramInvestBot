package ru.kvaga.invest.investbot.jobs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ru.kvaga.investments.stocks.StocksTrackingLib;
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

public class UpdateCurrentPricesOfStocksJob implements Runnable {
	final static Logger log = LogManager.getLogger(UpdateCurrentPricesOfStocksJob.class);
	private String urlTextTinkoff = null;
	private File listOfStocksFile = null;
	private String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME;
	private String label;

	public UpdateCurrentPricesOfStocksJob(String label, File listOfStocksFile, String urlTextTinkoff, String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME) {
		if (listOfStocksFile == null) {
			throw new RuntimeException("List of stocks file can't be null");
		}
		this.listOfStocksFile = listOfStocksFile;
		this.urlTextTinkoff=urlTextTinkoff;
		this.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME=REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME;
		this.label=label;
	}

	void updateStocks() {
		try {
			log.debug("listOfStocksFile=" + listOfStocksFile);
			StocksTrackingLib.updateCurrentPricesOfStocks(label, urlTextTinkoff, listOfStocksFile, REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME);
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}

	public void run() {
		log.info("Try to start Job ");

		try {
			updateStocks();
			log.debug("Job finished");
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}
}
