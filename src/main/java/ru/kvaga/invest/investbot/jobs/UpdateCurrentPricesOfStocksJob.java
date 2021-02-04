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

public class UpdateCurrentPricesOfStocksJob implements Runnable {
	final static Logger log = LogManager.getLogger(UpdateCurrentPricesOfStocksJob.class);
	private static String urlTextTinkoff = "https://www.tinkoff.ru/invest/stocks/%s/";
	private File listOfStocksFile;

	public UpdateCurrentPricesOfStocksJob(File listOfStocksFile) {
		if (listOfStocksFile == null) {
			throw new RuntimeException("List of stocks file can't be null");
		}
		this.listOfStocksFile = listOfStocksFile;
	}

	void updateStocks() {
		try {
			log.debug("listOfStocksFile=" + listOfStocksFile);
			StocksTrackingLib.updateCurrentPricesOfStocks(urlTextTinkoff, listOfStocksFile);
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
