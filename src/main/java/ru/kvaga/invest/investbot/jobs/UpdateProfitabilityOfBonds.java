package ru.kvaga.invest.investbot.jobs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.bonds.BondItemForPrinting;
import ru.kvaga.investments.bonds.BondItemForProfitabilityPrinting;
import ru.kvaga.investments.bonds.BondItemForProfitabilityPrintingComparatorByProfitability;
import ru.kvaga.investments.bonds.BondsTrackingLib;
import ru.kvaga.investments.stocks.StockItemForPrintingComparatorByPercentFromTrackingPrice;
import ru.kvaga.investments.lib.InstrumentsTrackingLib;
import ru.kvaga.telegram.sendmessage.TelegramSendMessage;
import telegrambot.App;
import telegrambot.ConfigMap;
import telegrambot.TelegramBotLib;

public class UpdateProfitabilityOfBonds implements Runnable {
	final static Logger log = LogManager.getLogger(UpdateProfitabilityOfBonds.class);
	String urlOfForeignBondsList = "https://www.tinkoff.ru/invest/bonds/?country=Foreign&orderType=Desc&sortType=ByYieldToClient&start=0&end=220";

	private boolean forcedBol = true;

	public UpdateProfitabilityOfBonds(boolean forced) {
		this.forcedBol = forced;
	}

	public UpdateProfitabilityOfBonds() {

	}

	public void run() {
		try {
			log.debug("UpdateProfitabilityOfBonds started");
			if (!forcedBol) {
				if (!ConfigMap.jobsEnabledBol) {
					log.info("Jobs disabled by user. Finished.");
					return;
				}
			}
//			BondItemForProfitabilityPrinting bifpp = new BondItemForProfitabilityPrinting();
			ArrayList<BondItemForProfitabilityPrinting> listOfBonds = new ArrayList<BondItemForProfitabilityPrinting>();
			HashMap<String, String> tickersAndUrls = new HashMap<String, String>();
			HashMap<String, String> mapOfTickersAndBondNames = BondsTrackingLib
					.getListOfBondsFromURL(urlOfForeignBondsList);
			App.telegramSendMessage.sendMessage("List of Bond's profitability");

			for (String ticker : mapOfTickersAndBondNames.keySet()) {
				try {
					String urlOfBond = String.format(ConfigMap.TEMPLATE_URL_TINKOFF_BONDS, ticker);
					String html = TelegramBotLib.getURLContent(urlOfBond);
					listOfBonds.add(new BondItemForProfitabilityPrinting(ticker, mapOfTickersAndBondNames.get(ticker),
							BondsTrackingLib.bondProfitability(
									BondsTrackingLib.getInfoFromHTMLForCalculationProfitability(html),
									InstrumentsTrackingLib.getCurrentPriceOfStock("", html, ""))));
				} catch (Exception e) {
					log.error("Couldn't get info for calculation profitability for ticker [" + ticker + "]", e);
					App.telegramSendMessage.sendMessage("BondException: <a href='"
							+ String.format(ConfigMap.TEMPLATE_URL_TINKOFF_BONDS, ticker) + "'>"
							+ URLEncoder.encode(mapOfTickersAndBondNames.get(ticker), StandardCharsets.UTF_8.toString())
							+ "</a> ");
				}
			}

			Collections.sort(listOfBonds, new BondItemForProfitabilityPrintingComparatorByProfitability());

			for (BondItemForProfitabilityPrinting bifpp : listOfBonds) {
				String message = "Bond: <a href='" + String.format(ConfigMap.TEMPLATE_URL_TINKOFF_BONDS, bifpp.getTicker())
						+ "'>" + bifpp.getBondName() + "</a> " + TelegramSendMessage.LINEBREAK
						+ String.format("Profitability: %.2f", bifpp.getProfitability());

				log.debug("Message:\n" + message);
				try {
//				App.telegramSendMessage.sendMessage(URLEncoder.encode(message,StandardCharsets.UTF_8.toString()));
					App.telegramSendMessage.sendMessage(message);

					log.info("Message for bond " + bifpp.getBondName() + " " + bifpp.getTicker()
							+ " was sent to telegram");
				} catch (Exception e) {
					if (e.getMessage().contains("Server returned HTTP response code: 429 for URL")) {
						log.error(
								"Server returned HTTP response code: 429 for URL due to too many messages per minute. Sleeping for 1 minute and try to resend message about stock "
										+ bifpp.getBondName() + " " + bifpp.getTicker(),
								e);
						Thread.sleep(60 * 1000);
						log.debug(
								"Trying to resend message about bond " + bifpp.getBondName() + " " + bifpp.getTicker());
						try {
							App.telegramSendMessage.sendMessage(message);
							log.debug("Resend SUCCESSFUL for bond " + bifpp.getBondName() + " " + bifpp.getTicker());
						} catch (Exception e2) {
							log.error("Resend FAILED for bond " + bifpp.getBondName() + " " + bifpp.getTicker()
									+ ". Continue next iteration", e2);
						}
					} else {
						log.error("Exception", e);
						try {
							App.telegramSendMessage.sendMessage(
									"Couldn't send message for bond " + bifpp.getBondName() + " " + bifpp.getTicker());
						} catch (Exception e1) {
							log.error("Couldn't send message about error during sending information about bond "
									+ bifpp.getBondName() + " " + bifpp.getTicker(), e);
						}
					}
					continue;
				}
			}
		} catch (Exception e) {
			log.error("UpdateProfitabilityOfBondsException", e);
		}
		log.debug("UpdateProfitabilityOfBonds finished");

	}

}
