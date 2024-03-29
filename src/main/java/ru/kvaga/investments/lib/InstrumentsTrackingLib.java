package ru.kvaga.investments.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import kotlin.reflect.TypeOfKt;
import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.investments.bonds.BondItemForPrinting;
import ru.kvaga.investments.bonds.BondsTrackingLib;
import ru.kvaga.investments.etfs.Etf;
import ru.kvaga.investments.lib.StocksTrackingException.GetContentOFSiteException;
import ru.kvaga.investments.lib.StocksTrackingException.StoreDataException;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.investments.stocks.StockItemForPrinting;
import ru.kvaga.investments.stocks.StockItemForPrintingComparatorByPercentFromTrackingPrice;
import ru.kvaga.telegram.sendmessage.TelegramSendMessage;
import ru.kvaga.telegrambot.web.util.ServerUtils;
import telegrambot.App;
import telegrambot.ConfigMap;
import telegrambot.Settings;
import telegrambot.TelegramBotLib;
import ru.kvaga.investments.lib.StocksTrackingException.GetCurrentPriceOfStockException.Common;
import ru.kvaga.investments.lib.StocksTrackingException.GetCurrentPriceOfStockException.ParsingResponseException;
import ru.kvaga.investments.lib.StocksTrackingException.ReadStockItemsFileException.IncorrectFormatOfRow;
import ru.kvaga.investments.lib.StocksTrackingException.ReadStockItemsFileException.ItemsFileNotFound;

public class InstrumentsTrackingLib {
	final static Logger log = LogManager.getLogger(InstrumentsTrackingLib.class);

	public static synchronized String getFullNameOfStock(String response, String stockName, String urlText, String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME)
			throws ru.kvaga.investments.lib.StocksTrackingException.GetFullStockNameException.ParsingResponseException {
//		String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME = "<meta charset=\"UTF-8\">.*"
//				+ "<title data-meta-dynamic=\"true\">РљСѓРїРёС‚СЊ Р°РєС†РёРё (?<fullName>.*) \\(" + stockName
//				+ "\\).*</title>.*" + "<meta property=\"og:title\"";
//		String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME = "<meta charset=\"UTF-8\">.*"
//				+ "<title data-meta-dynamic=\"true\">.* .* (?<fullName>.*) \\(" + stockName + "\\).*</title>.*"
//				+ "<meta property=\"og:title\"";

		
		response = response.replaceAll("\r\n", "").replaceAll("\n", "");
		if (response.length() >= 500) {
			response = response.substring(0, 499);
		}
//		response=new String(response.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		log.debug("RESPONSE (first 500 symbols): " + response);

		Pattern patternForFullName = Pattern.compile(REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME);
		Matcher matcherForFullName = patternForFullName.matcher(response);
		String stockFullName;

		if (matcherForFullName.find()) {
			stockFullName = matcherForFullName.group("fullName");
			log.debug("Found full name of ticker " + stockName + ": " + stockFullName);

		} else {
			
			throw new StocksTrackingException.GetFullStockNameException.ParsingResponseException(String.format(
					"Couldn't find fullName for stock during parsing web response with " + "regex pattern text [%s]. \n"
					+ "Web response [%s]"
					, REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME
					,response
			), urlText);
		}

		return stockFullName;

	}

	public static synchronized  void storeActualData(File file, ArrayList<StockItem> al) throws StoreDataException {
		StringBuilder sb = new StringBuilder();
		for (StockItem si : al) {
			sb.append(si.getName());
			sb.append(",");
			sb.append(si.getTraceablePrice());
			sb.append(",");
			sb.append(si.getLastPrice());
			sb.append("\n");
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(sb.toString().getBytes());
			fos.flush();
		} catch (Exception e) {
			throw new StocksTrackingException.StoreDataException(
					String.format("Couldn't store data to %s file. %s", file.getAbsoluteFile(), e.getMessage()));
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	public static synchronized String getContentOfSite( String urlText)
			throws Common, GetContentOFSiteException {
		URL url = null;
		HttpURLConnection con = null;
		BufferedReader br = null;
		String s;
		StringBuilder sb = new StringBuilder();

		try {
			url = new URL(urlText);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
//				con.setRequestProperty("accept-encoding", "gzip, deflate, br");
//				con.setRequestProperty("accept-language", "en-GB,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6");
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			return sb.toString();
		} catch (IOException e) {
			throw new StocksTrackingException.GetContentOFSiteException(e.getMessage(), urlText);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.debug("Exception", e);
				}
			}
		}

	}
*/
	public static synchronized double getCurrentPriceOfStock(String name, String response, String url)
			throws Common, ParsingResponseException {
		String REGEX_PATTERN_TEXT_MOEXX = "\\<row SECID=\"" + name
				+ "\" PREVADMITTEDQUOTE=\"(?<lastPrice>\\d+\\.{0,1}\\d*)\" />";
//		String REGEX_PATTERN_TEXT_TINKOFF = "<div class=\"GridColumn__column_2h5Ek GridColumn__column_hidden_on_phone_15UiO GridColumn__column_hidden_on_tabletS_G1iCc GridColumn__column_hidden_on_tabletL_3WX2Z.*"
//				+ "<span class=\"Money__money_3_Tn4\" data-qa-type=\"uikit\\/money\">(?<currentPrice>\\d+.*<span>.*)[₽|$]<\\/span><\\/span><\\/span>";
//		String REGEX_PATTERN_TEXT_TINKOFF =         "data-qa-type=\"uikit\\/money\">(?<currentPrice>\\d+.*?)[₽|$|€]<\\/span>";
		//String REGEX_PATTERN_TEXT_TINKOFF = "На \\d{2}[.]\\d{2}[.]\\d{4} стоимость одной акции [компании|фонда] (?<name>.*) составляет (?<currentPrice>.*) [дол|евр|руб|$].* Сделка по";

		String REGEX_PATTERN_TEXT_TINKOFF = "На \\d{2}[.]\\d{2}[.]\\d{4} стоимость одной акции (компании|фонда) (?<name>.*) составляет (?<currentPrice>.*) (долларов|доллар|доллара|евро|рубль|рубля|рублей|\\$|₽|€)[.]";
//				+ "<span class=\"Money-module__money_2PlRa\" data-qa-type=\"uikit\\/money\">(?<currentPrice>\\d+.*<span>.*)[₽|$]<\\/span><\\/span><\\/span>";
//		_hidden_on_phone_2W092 Column-module__column_hidden_on_tabletS_JyZiL Column-module__column_hidden_on_tabletL_skZDO Column-module__column_size_desktopS_4_laKAk" style="margin-bottom:0"><div data-qa-file="Sticky"><div data-qa-file="Sticky"></div><div data-qa-file="StickySecurityPriceDetail"><div class="SecurityPriceDetailsPure__wrapper_srZsI" data-qa-file="SecurityPriceDetailsPure"><div class="SecurityPriceDetailsPure__inviting_llWIp" data-qa-file="SecurityPriceDetailsPure"><div class="SecurityInvitingScreenPure__wrapper_2wZTB" data-qa-file="SecurityInvitingScreenPure"><div class="SecurityInvitingScreenPure__priceText_37tcc" data-qa-file="SecurityInvitingScreenPure">Цена<!-- --> <!-- -->акции<!-- --> <!-- -->10 февраля 2021</div><div class="SecurityInvitingScreenPure__price_31WUF" data-qa-file="SecurityInvitingScreenPure"><span class="SecurityInvitingScreenPure__priceValue_1GEPt" data-qa-file="SecurityInvitingScreenPure"><span class="Money-module__money_2PlRa" data-qa-type="uikit/money">266<span>,97<!-- --> <!-- -->₽</span></span></span>
		String regexPatternText = REGEX_PATTERN_TEXT_TINKOFF; //266<span>,97<!-- --> <!-- -->₽
		// System.out.println(response);
		Pattern pattern = Pattern.compile(regexPatternText);
		Matcher matcher = pattern.matcher(response);
		if (matcher.find()) {
//				System.out.println(String.format("Last price of %s: %s", name, matcher.group("lastPrice")));
			String str = matcher.group("currentPrice");
			str = str.replaceAll("<!-- -->", "").replaceAll("<span>", "").replaceAll(" ", "").replaceAll(",", ".");
//			System.out.println("Web response["+response+"]");
//			System.out.println("URL ["+url+"]");
			//System.out.println("str["+Double.parseDouble(str)+"]");
			return Double.parseDouble(str);
		} else {
			throw new StocksTrackingException.GetCurrentPriceOfStockException.ParsingResponseException(
					String.format("Couldn't find lastPrice value for stock during parsing web response with "
							+ "regex pattern text [%s]. \n" + "Web response (first 500 symbols) [%s]", regexPatternText, response.length()>500?response.substring(0,499):response),
					name, url);
		}
	}

	public static synchronized void removeStockItemByTikerFromFile(String ticker, File file) throws StocksTrackingException {
		ArrayList<StockItem> al = new ArrayList<StockItem>();
		BufferedReader br = null;
		String s;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((s = br.readLine()) != null) {
				if (s.contains(",")) {
					String mas[] = s.split(",");
					if (mas.length == 3) {
						if (mas[0].equals(ticker)) {
							// skip and don't add this stockitem to list of stocks
						} else {
							StockItem si = new StockItem();
							si.setName(mas[0]);
							si.setTraceablePrice(Double.parseDouble(mas[1]));
							si.setLastPrice(Double.parseDouble(mas[2]));
							al.add(si);
						}
					} else {
						throw new StocksTrackingException.ReadStockItemsFileException.Common(
								String.format(
										"Couldn't parse the '%s' row and get "
												+ "complete information for stock. Massive length is '%s'",
										s, mas.length),
								file);
					}
				} else {
					throw new StocksTrackingException.ReadStockItemsFileException.IncorrectFormatOfRow(
							String.format("The row %s doesn't contain ',' symbol", s), file);
				}
			}
			// Store StockItems to file without skipped ticker
			storeActualData(file, al);
		} catch (FileNotFoundException e) {
			throw new StocksTrackingException.ReadStockItemsFileException.ItemsFileNotFound(
					String.format("Can't find file which stores stock items"), file);
		} catch (IOException e) {
			throw new StocksTrackingException.ReadStockItemsFileException.Common(e.getMessage(), file);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.debug("Exception", e);
				}
			}
		}

	}

	public static synchronized StockItem getStockItemByTickerFromFile(String ticker, File file)
			throws ru.kvaga.investments.lib.StocksTrackingException.ReadStockItemsFileException.Common,
			IncorrectFormatOfRow, ItemsFileNotFound {
		BufferedReader br = null;
		String s;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((s = br.readLine()) != null) {
				if (s.contains(",")) {
					String mas[] = s.split(",");
					if (mas.length == 3) {
						if (mas[0].equals(ticker)) {
							StockItem si = new StockItem();
							si.setName(mas[0]);
							si.setTraceablePrice(Double.parseDouble(mas[1]));
							si.setLastPrice(Double.parseDouble(mas[2]));
							return si;
						}

					} else {
						throw new StocksTrackingException.ReadStockItemsFileException.Common(
								String.format(
										"Couldn't parse the '%s' row and get "
												+ "complete information for stock. Massive length is '%s'",
										s, mas.length),
								file);
					}
				} else {
					throw new StocksTrackingException.ReadStockItemsFileException.IncorrectFormatOfRow(
							String.format("The row %s doesn't contain ',' symbol", s), file);
				}
			}
			return null;
		} catch (FileNotFoundException e) {
			throw new StocksTrackingException.ReadStockItemsFileException.ItemsFileNotFound(
					String.format("Can't find file which stores stock items"), file);
		} catch (IOException e) {
			throw new StocksTrackingException.ReadStockItemsFileException.Common(e.getMessage(), file);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static synchronized ArrayList<Instrument> getListOfInstruments(Instrument instrument) throws StocksTrackingException {
		ArrayList<Instrument> al = new ArrayList<Instrument>();
		BufferedReader br = null;
		String s;
		try {
			if(instrument instanceof StockItem) {
				for(File file : ConfigMap.stocksPath.listFiles()) {
					al.add(Instrument.readXMLObjectFromFile(file, new StockItem()));
				}
			}else if (instrument instanceof Bond) {
				for(File file : ConfigMap.bondsPath.listFiles()) {
					al.add(Instrument.readXMLObjectFromFile(file, new Bond()));
				}
			}else if(instrument instanceof Etf) {
				for(File file : ConfigMap.etfsPath.listFiles()) {
					al.add(Instrument.readXMLObjectFromFile(file, new Etf()));
				}
			}
		}catch(Exception e) {
			log.error("Couldn't get Instruments list", e);
			return null;
		}
		return al;
	}

	public static synchronized void updateCurrentPricesOfStocks(Instrument instrument, String URL_TEXT_TINKOFF, String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME) throws Exception {
		log.info("UpdateCurrentPricesOfStocks "+instrument.getClass()+"s job started");
//		ArrayList<StockItem> actualStockItems = new ArrayList<StockItem>();
//		ArrayList<StockItemForPrinting> stockItemsForPrinting = new ArrayList<StockItemForPrinting>();

		// Work
		String url = null;
		String label = instrument.getClass().toString();

		try {
			for (Instrument si : getListOfInstruments(instrument)) {
				// We can must catch all exception for earch iteration for the future continueation
				log.debug("===> Processing ["+si.getName()+"] started <===");
				try {
//					Instrument actualStockItem = new StockItem();
					double currentPrice = 0;
//					actualStockItem.setName(si.getName());
//					actualStockItem.setTraceablePrice(si.getTraceablePrice());
					si.setLastPrice(currentPrice);
					//if (!si.getName().startsWith("#")) {
						url = String.format(URL_TEXT_TINKOFF, si.getName());
						log.debug("Url is ready: " + url);

//					String response = StocksTrackingLib.getContentOfSite( url);
						String response = TelegramBotLib.getURLContent(String.format(url, si.getName()));
						log.debug("The response received");
//						String fullName = getFullNameOfStock(response /*StocksTrackingLib.getContentOfSite(url)*/,
//								si.getName(), url, String.format(REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME, si.getName()));
						if(si.getFullName()==null) {
							log.debug("Full name received for ["+si.getName()+"]: " + si.setFullName(getFullNameOfStock(response, si.getName(), url, String.format(REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME, si.getName()))).getFullName());
						}
						currentPrice = InstrumentsTrackingLib.getCurrentPriceOfStock(si.getName(), response, url);
						log.debug("Current price received: " + currentPrice);
						si.setLastPrice(currentPrice);

						log.debug("Analyzing "+si.getClass()+" for sending [ticker: " + si.getName() + ", traceablePrice: "
								+ si.getTraceablePrice() + ", lastPrice: " + si.getLastPrice() + "]");

						si.setLastUpdated(new Date());
//						if (si.getTraceablePrice() > currentPrice) {
//							if(instrument instanceof Bond) {
//								si.add((StockItemForPrinting)new BondItemForPrinting(si.getName(), si.getFullName(),
//										si.getTraceablePrice(), si.getLastPrice(), currentPrice,
//										currentPrice * 100.0 / si.getTraceablePrice() - 100,
//										currentPrice * 100.0 / si.getLastPrice() - 100,BondsTrackingLib.bondProfitability(BondsTrackingLib.getInfoFromHTMLForCalculationProfitability(response), currentPrice)));
//							}else {
//								stockItemsForPrinting.add(new StockItemForPrinting(si.getName(), si.getFullName(),
//									si.getTraceablePrice(), si.getLastPrice(), currentPrice,
//									currentPrice * 100.0 / si.getTraceablePrice() - 100,
//									currentPrice * 100.0 / si.getLastPrice() - 100));
//							}
//							/*
//							 * App.telegramSendMessage.sendMessage(
//							 * "Stock: <a href='https://tinkoff.ru/invest/stocks/"+si.getName()+"/'>"+si.
//							 * getName()+"</a> " + fullName + TelegramSendMessage.LINEBREAK +
//							 * "Tracking Price: " + si.getTraceablePrice() + TelegramSendMessage.LINEBREAK +
//							 * "Last Price: " + si.getLastPrice() + TelegramSendMessage.LINEBREAK +
//							 * "Current Price: " + currentPrice + TelegramSendMessage.LINEBREAK + "("+
//							 * String.format("%.2f", currentPrice*100.0/si.getTraceablePrice()-100)
//							 * +"% from Tracking Price, " + "("+
//							 * String.format("%.2f",currentPrice*100.0/si.getLastPrice()-100)
//							 * +"% from Last Price)"
//							 * 
//							 * 
//							 * );
//							 * 
//							 * log.debug("Sent message to telegram for [ticker: "+si.getName()
//							 * +", traceablePrice: "+si.getTraceablePrice()+", lastPrice: "+si.getLastPrice(
//							 * )+"]");
//							 */
//						}

//					} else {
//						actualStockItem.setLastPrice(si.getLastPrice());
//					} 
					si.saveXMLObjectToFile(instrument);
//					actualStockItems.add(actualStockItem);
				} catch (Exception e) {
					log.error("Exception. In such case we add item retrived from file [" + null
							+ "] without any changes to the actual"+label+"Items list and continue to the next item from file",
							e);
					// actualStockItems.add((StockItem)si);
					try {
						log.error("Trying to send message to telegram about exception for url ["+url+"]", e);
						App.telegramSendMessage.sendMessage(("Error for URL [" + url + "]: Exception: " + e).substring(0, 100));
					} catch (Exception e1) {
						log.error("Unable to send message about exception to telegram.");
					}
				}
			}

			// save data
			//InstrumentsTrackingLib.storeActualData(dataFileName, actualStockItems);

			// sending to telegram
//			log.debug("Sorting "+label+"s...");
//			Collections.sort(stockItemsForPrinting, new StockItemForPrintingComparatorByPercentFromTrackingPrice());
//			StringBuilder message = new StringBuilder();
			log.debug("Sending messages...");
			try {
				
				if(instrument instanceof StockItem) {
					ArrayList<String> messages = getInstrumentsForPrintedFormat(getTopInstruments(instrument));
					if(messages.size()>0) {
						messages.add("Current prices of Stocks were updated, link: [" + ConfigMap.appHttpLink + "Stocks.jsp]");
						sendListOfMessages(messages);
					}
				}else if(instrument instanceof Bond) {
					ArrayList<String> messages = getInstrumentsForPrintedFormat(getTopInstruments(instrument));
					if(messages.size()>0) {
						messages.add("Current prices of Bonds were updated, link: [" + ConfigMap.appHttpLink + "Bonds.jsp]");
						sendListOfMessages(messages);
					}
				}else if(instrument instanceof Etf) { 
					ArrayList<String> messages = getInstrumentsForPrintedFormat(getTopInstruments(instrument));
					if(messages.size()>0) {
						messages.add("Current prices of Etfs were updated, link: [" + ConfigMap.appHttpLink + "Etfs.jsp]");
						sendListOfMessages(messages);
					}
					// oDo: App.telegramSendMessage.sendMessage("Current prices of Etfs were updated, link: [" + ConfigMap.appHttpLink + "Etfs.jsp]");
				}
			} catch (Exception e1) {
				log.error("Exception", e1);
				App.telegramSendMessage.sendMessage(("Exception (first 50 symbols): " + (e1.getMessage().length()>50?e1.getMessage().substring(0, 49):e1.getMessage())));
			}	
//			int countOfMessages=0;
//			for (StockItemForPrinting sifp : stockItemsForPrinting) {
//				/*
//				 * message.append(
//				 * "Stock: <a href='https://tinkoff.ru/invest/stocks/"+sifp.getName()+"/'>"+
//				 * sifp. getName()+"</a> " + sifp.getFullName() + TelegramSendMessage.LINEBREAK
//				 * + "Tracking Price: " + sifp.getTraceablePrice() +
//				 * TelegramSendMessage.LINEBREAK + "Last Price: " + sifp.getLastPrice() +
//				 * TelegramSendMessage.LINEBREAK + "Current Price: " + sifp.getCurrentPrice() +
//				 * TelegramSendMessage.LINEBREAK + "("+ String.format("%.2f",
//				 * sifp.getPercentFromTrackingPrice()) +"% from Tracking Price, " + "("+
//				 * String.format("%.2f",sifp.getPercentFromLastPrice()) +"% from Last Price)" +
//				 * TelegramSendMessage.LINEBREAK + TelegramSendMessage.LINEBREAK );
//				 */
//				
//					String message = label+": <a href='" + String.format(URL_TEXT_TINKOFF, sifp.getName()) + "'>"
//						+ sifp.getName() + "</a> " + URLEncoder.encode(sifp.getFullName(),StandardCharsets.UTF_8.toString()) + TelegramSendMessage.LINEBREAK
//						+ String.format("Tracking Price: %.2f", sifp.getTraceablePrice())
//						+ TelegramSendMessage.LINEBREAK + String.format("Last Price: %.2f", sifp.getLastPrice())
//						+ TelegramSendMessage.LINEBREAK + String.format("Current Price: %.2f", sifp.getCurrentPrice())
//						+ TelegramSendMessage.LINEBREAK + "("
//						+ String.format("%.2f", sifp.getPercentFromTrackingPrice()) + "% from Tracking Price, " + "("
//						+ String.format("%.2f", sifp.getPercentFromLastPrice()) + "% from Last Price)";
//					if(label.equals("Bond")) {
//						BondItemForPrinting bifp = (BondItemForPrinting) sifp;
//						message = message + TelegramSendMessage.LINEBREAK + String.format("Profitability: %.2f", bifp.getProfitability())+"%";
//					}
//				log.debug("Message:\n" + message);
//				try {
////					App.telegramSendMessage.sendMessage(URLEncoder.encode(message,StandardCharsets.UTF_8.toString()));
//					App.telegramSendMessage.sendMessage(message);
//
//					log.info(
//							"Message for "+label+" " + sifp.getName() + " " + sifp.getFullName() + " was sent to telegram");
//				} catch (Exception e) {
//					if (e.getMessage().contains("Server returned HTTP response code: 429 for URL")) {
//						log.error(
//								"Server returned HTTP response code: 429 for URL due to too many messages per minute. Sleeping for 1 minute and try to resend message about stock "
//										+ sifp.getName() + " " + sifp.getFullName(),
//								e);
//						Thread.sleep(60 * 1000);
//						log.debug("Trying to resend message about "+label+" " + sifp.getName() + " " + sifp.getFullName());
//						try {
//							App.telegramSendMessage.sendMessage(message);
//							log.debug("Resend SUCCESSFUL for "+label+" " + sifp.getName() + " " + sifp.getFullName());
//						} catch (Exception e2) {
//							log.error("Resend FAILED for "+label+" " + sifp.getName() + " " + sifp.getFullName()
//									+ ". Continue next iteration", e2);
//						}
//					} else {
//						log.error("Exception", e);
//						try {
//							App.telegramSendMessage.sendMessage(
//									"Couldn't send message for "+label+" " + sifp.getName() + " " + sifp.getFullName());
//						} catch (Exception e1) {
//							log.error("Couldn't send message about error during sending information about "+label+" "
//									+ sifp.getName() + " " + sifp.getFullName(), e);
//						}
//					}
//					continue;
//				}
//
//			}

		} catch (Exception e) {
			log.error("Exception", e);
			App.telegramSendMessage.sendMessage(("Exception (first 50 symbols): " + (e.getMessage().length()>50?e.getMessage().substring(0, 49):e.getMessage())));
		}
		log.info("UpdateCurrentPricesOfStocks "+label+"s job finished");

	}

	private static void sendListOfMessages(ArrayList<String> messages) throws Exception {
		for(String message : messages) {
			App.telegramSendMessage.sendMessage(message);
			Thread.sleep(100);
		}
	}
	
	private static ArrayList<String> getInstrumentsForPrintedFormat(ArrayList<Instrument> instrumentList) throws UnsupportedEncodingException {
		ArrayList<String> messages = new ArrayList<String>();	
		for(Instrument instr : instrumentList) {
			String message = " <a href='" + String.format(ConfigMap.TEMPLATE_URL_TINKOFF_STOCKS, instr.getName()) + "'>"
				+ instr.getName() + "</a> " + URLEncoder.encode(instr.getFullName(),StandardCharsets.UTF_8.toString()) + TelegramSendMessage.LINEBREAK
				+ String.format("Tracking Price: %.2f", instr.getTraceablePrice())
				//+ TelegramSendMessage.LINEBREAK + String.format("Last Price: %.2f", instr.getLastPrice())
				+ TelegramSendMessage.LINEBREAK + String.format("Current Price: %.2f", instr.getLastPrice())
				+ TelegramSendMessage.LINEBREAK + 
				"(" + getPercentFromTrackingPrice(instr) + "% from Tracking Price) "; 
			messages.add(message);
		}
		return messages;
	} 
	
	public static double getPercentFromTrackingPrice(Instrument instrument) {
		return (instrument.getLastPrice()/instrument.getTraceablePrice())*100-100;
	}
	
	

	
	private static ArrayList<Instrument> getTopInstruments(Instrument instrument) throws StocksTrackingException, JAXBException, IOException {
		Settings settings = Settings.load();
		ArrayList<Instrument> sortedInstrumentsList = getListOfInstruments(instrument);
		log.debug("Sorting "+instrument+"...");
		Collections.sort(sortedInstrumentsList, new InstrumentsComparatorByPercentFromTrackingPrice());
		if(sortedInstrumentsList.size() -settings.getTelegramNotificationsTopCountForSending()>0) {
			return new ArrayList<Instrument>(sortedInstrumentsList.subList(sortedInstrumentsList.size() -settings.getTelegramNotificationsTopCountForSending(), sortedInstrumentsList.size()));
		}else {
			return sortedInstrumentsList;
		}
	}
	

}

