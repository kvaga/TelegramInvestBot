package ru.kvaga.investments.stocks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ru.kvaga.investments.stocks.StocksTrackingException.GetContentOFSiteException;
import ru.kvaga.investments.stocks.StocksTrackingException.StoreDataException;
import ru.kvaga.telegram.sendmessage.TelegramSendMessage;
import telegrambot.App;
import telegrambot.TelegramBotLib;
import ru.kvaga.investments.stocks.StocksTrackingException.GetCurrentPriceOfStockException.Common;
import ru.kvaga.investments.stocks.StocksTrackingException.GetCurrentPriceOfStockException.ParsingResponseException;
import ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.IncorrectFormatOfRow;
import ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.ItemsFileNotFound;

public class StocksTrackingLib {
	private static Logger log = LogManager.getLogger(StocksTrackingLib.class);

	public static String getFullNameOfStock(String response, String stockName, String urlText)
			throws ru.kvaga.investments.stocks.StocksTrackingException.GetFullStockNameException.ParsingResponseException {
//		String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME = "<meta charset=\"UTF-8\">.*"
//				+ "<title data-meta-dynamic=\"true\">РљСѓРїРёС‚СЊ Р°РєС†РёРё (?<fullName>.*) \\(" + stockName
//				+ "\\).*</title>.*" + "<meta property=\"og:title\"";
		String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME="<meta charset=\"UTF-8\">.*" + 
				"<title data-meta-dynamic=\"true\">.* .* (?<fullName>.*) \\("+stockName+"\\).*</title>.*" + 
				"<meta property=\"og:title\"" ;
		
		response = response.replaceAll("\r\n", "").replaceAll("\n", "");
		if(response.length()>=500) {
			response=response.substring(0, 499);
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
			log.error("[point 2]");
			throw new StocksTrackingException.GetFullStockNameException.ParsingResponseException(String.format(
					"Couldn't find fullName for stock during parsing web response with " + "regex pattern text [%s]. \n"
//					+ "Web response [%s]"
					, REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME
//					,response
			), urlText);
		}

		return stockFullName;

	}

	public static void storeActualData(File file, ArrayList<StockItem> al) throws StoreDataException {
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

	public static String getContentOfSite(String stockShortName, String urlText)
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
			throw new StocksTrackingException.GetContentOFSiteException(e.getMessage(), stockShortName, urlText);
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

	public static double getCurrentPriceOfStock(String name, String response, String url)
			throws Common, ParsingResponseException {
		String REGEX_PATTERN_TEXT_MOEXX = "\\<row SECID=\"" + name
				+ "\" PREVADMITTEDQUOTE=\"(?<lastPrice>\\d+\\.{0,1}\\d*)\" />";
		String REGEX_PATTERN_TEXT_TINKOFF=
				"<div class=\"GridColumn__column_2h5Ek GridColumn__column_hidden_on_phone_15UiO GridColumn__column_hidden_on_tabletS_G1iCc GridColumn__column_hidden_on_tabletL_3WX2Z.*"
				+ "<span class=\"Money__money_3_Tn4\" data-qa-type=\"uikit\\/money\">(?<currentPrice>\\d+.*<span>.*)[₽|$]<\\/span><\\/span><\\/span>";

		String regexPatternText = REGEX_PATTERN_TEXT_TINKOFF;
		// System.out.println(response);
		Pattern pattern = Pattern.compile(regexPatternText);
		Matcher matcher = pattern.matcher(response);
		if (matcher.find()) {
//				System.out.println(String.format("Last price of %s: %s", name, matcher.group("lastPrice")));
			String str = matcher.group("currentPrice");
			str=str.replaceAll("<!-- -->", "").replaceAll("<span>", "").replaceAll(" ", "").replaceAll(",", ".");
			return Double.parseDouble(str);
		} else {
			throw new StocksTrackingException.GetCurrentPriceOfStockException.ParsingResponseException(
					String.format("Couldn't find lastPrice value for stock during parsing web response with "
							+ "regex pattern text [%s]. \n" + "Web response [%s]", regexPatternText, response),
					name, url);
		}
	}

	public static void removeStockItemByTikerFromFile(String ticker, File file) throws StocksTrackingException {
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

	public static StockItem getStockItemByTickerFromFile(String ticker, File file)
			throws ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.Common,
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

	public static ArrayList<StockItem> getListOfStocksFromFile(File file) throws StocksTrackingException {
		ArrayList<StockItem> al = new ArrayList<StockItem>();
		BufferedReader br = null;
		String s;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((s = br.readLine()) != null) {
				if (s.contains(",")) {
					String mas[] = s.split(",");
					if (mas.length == 3) {
						StockItem si = new StockItem();
						si.setName(mas[0]);
						si.setTraceablePrice(Double.parseDouble(mas[1]));
						si.setLastPrice(Double.parseDouble(mas[2]));
						al.add(si);
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
			return al;
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
					log.error("Exception", e);
				}
			}
		}

	}

	public static void updateCurrentPricesOfStocks(String URL_TEXT_TINKOFF, File dataFileName) throws Exception {
		ArrayList<StockItem> actualStockItems = new ArrayList<StockItem>();
		ArrayList<StockItemForPrinting> stockItemsForPrinting = new ArrayList<StockItemForPrinting>();

		try {

			// Work
			for (StockItem si : StocksTrackingLib.getListOfStocksFromFile(dataFileName)) {
				StockItem actualStockItem = new StockItem();
				double currentPrice = 0;
				actualStockItem.setName(si.getName());
				actualStockItem.setTraceablePrice(si.getTraceablePrice());
				if (!si.getName().startsWith("#")) {
					String url = String.format(URL_TEXT_TINKOFF, si.getName());
					log.debug("Url is ready: " + url);

//					String response = StocksTrackingLib.getContentOfSite(si.getName(), url);
					String response = TelegramBotLib.getURLContent(String.format(url, si.getName()));
					log.debug("The response received");
					String fullName=getFullNameOfStock(StocksTrackingLib.getContentOfSite(si.getName(), url), si.getName(), url);
					log.debug("Full name received: " + fullName);
					currentPrice = StocksTrackingLib.getCurrentPriceOfStock(si.getName(), response, url);
					log.debug("Current price received: " + currentPrice);
					actualStockItem.setLastPrice(currentPrice);

					  log.debug("Analyzing stock for sending [ticker: "+si.getName()+", traceablePrice: "+si.getTraceablePrice()+", lastPrice: "+si.getLastPrice()+"]");

					  if(si.getTraceablePrice() > currentPrice) { 
						  stockItemsForPrinting.add(new StockItemForPrinting(
								  si.getName(), 
								  fullName, 
								  si.getTraceablePrice(), 
								  si.getLastPrice(), 
								  currentPrice, 
								  currentPrice*100.0/si.getTraceablePrice()-100, 
								  currentPrice*100.0/si.getLastPrice()-100
								  ));
						  /*
						  App.telegramSendMessage.sendMessage(
								  "Stock: <a href='https://tinkoff.ru/invest/stocks/"+si.getName()+"/'>"+si.
								  getName()+"</a> " + fullName + TelegramSendMessage.LINEBREAK +
								  "Tracking Price: " + si.getTraceablePrice() + TelegramSendMessage.LINEBREAK +
								  "Last Price: " + si.getLastPrice() + TelegramSendMessage.LINEBREAK +
								  "Current Price: " + currentPrice + TelegramSendMessage.LINEBREAK + "("+
								  String.format("%.2f", currentPrice*100.0/si.getTraceablePrice()-100)
								  +"% from Tracking Price, " + "("+
								  String.format("%.2f",currentPrice*100.0/si.getLastPrice()-100)
								  +"% from Last Price)"
								  
					  
					  ); 
					  
						  log.debug("Sent message to telegram for [ticker: "+si.getName()+", traceablePrice: "+si.getTraceablePrice()+", lastPrice: "+si.getLastPrice()+"]");
						  */
						  }
					 
				} else {
					actualStockItem.setLastPrice(si.getLastPrice());
				}
				actualStockItems.add(actualStockItem);		
			}

			// save data
			StocksTrackingLib.storeActualData(dataFileName, actualStockItems);
			
			// sending to telegram
			log.debug("Sorting stocks...");
			Collections.sort(stockItemsForPrinting, new StockItemForPrintingComparatorByPercentFromTrackingPrice());
//			StringBuilder message = new StringBuilder();
			log.debug("Sending messages...");
			App.telegramSendMessage.sendMessage("List of stock's tracking prices");
//			int countOfMessages=0;
			for(StockItemForPrinting sifp : stockItemsForPrinting) {
				/* 
				message.append(
						  "Stock: <a href='https://tinkoff.ru/invest/stocks/"+sifp.getName()+"/'>"+sifp.
						  getName()+"</a> " + sifp.getFullName() + TelegramSendMessage.LINEBREAK +
						  "Tracking Price: " + sifp.getTraceablePrice() + TelegramSendMessage.LINEBREAK +
						  "Last Price: " + sifp.getLastPrice() + TelegramSendMessage.LINEBREAK +
						  "Current Price: " + sifp.getCurrentPrice() + TelegramSendMessage.LINEBREAK + "("+
						  String.format("%.2f", sifp.getPercentFromTrackingPrice())
						  +"% from Tracking Price, " + "("+
						  String.format("%.2f",sifp.getPercentFromLastPrice())
						  +"% from Last Price)"  
						  + TelegramSendMessage.LINEBREAK
						  + TelegramSendMessage.LINEBREAK
						);
				*/
				String message = "Stock: <a href='"+String.format(URL_TEXT_TINKOFF, sifp.getName())+"'>"+sifp.
						  getName()+" </a>" + sifp.getFullName() + TelegramSendMessage.LINEBREAK +
						  String.format("Tracking Price: %.2f", sifp.getTraceablePrice()) + TelegramSendMessage.LINEBREAK +
						  String.format("Last Price: %.2f", sifp.getLastPrice()) + TelegramSendMessage.LINEBREAK +
						  String.format("Current Price: %.2f", sifp.getCurrentPrice()) + TelegramSendMessage.LINEBREAK + "("+
						  String.format("%.2f", sifp.getPercentFromTrackingPrice())
						  +"% from Tracking Price, " + "("+
						  String.format("%.2f",sifp.getPercentFromLastPrice())
						  +"% from Last Price)";
				log.debug("Message:\n" + message);
				try {
					App.telegramSendMessage.sendMessage( message  );
					log.info("Message for stock " + sifp.getName() + " " + sifp.getFullName() + " was sent to telegram");
				}catch(Exception e){
					if(e.getMessage().contains("Server returned HTTP response code: 429 for URL")) {
						log.error("Server returned HTTP response code: 429 for URL due to too many messages per minute. Sleeping for 1 minute and try to resend message about stock " + sifp.getName() + " " + sifp.getFullName(), e);
						Thread.sleep(60 * 1000);
						log.debug("Trying to resend message about stock " + sifp.getName() + " " + sifp.getFullName());
						try {
							App.telegramSendMessage.sendMessage( message );
							log.debug("Resend SUCCESSFUL for stock " + sifp.getName() + " " + sifp.getFullName() );
						}catch(Exception e2) {
							log.error("Resend FAILED for stock " + sifp.getName() + " " + sifp.getFullName() + ". Continue next iteration", e2);
						}
					}else {
						try {
							App.telegramSendMessage.sendMessage( "Couldn't send message for stock " + sifp.getName() + " " + sifp.getFullName());
						}catch(Exception e1) {
							log.error("Couldn't send message about error during sending information about stock " + sifp.getName() + " " + sifp.getFullName(), e);
						}
					}
					continue;
				}

			}
			

		} catch (Exception e) {
			log.error("Error: couldn't send message", e);
			App.telegramSendMessage.sendMessage("Error: couldn't send message");
			// Ошибка возникает при отправке в телеграмм длинного сообщения:
			// java.lang.Exception: Couldn't send message for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Error: Couldn't send message for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Stock: <a href='https://tinkoff.ru/invest/stocks/SBER/'>SBER</a> Р РѕСЃСЃРёРё%0ATracking Price: 262.32000732421875%0ALast Price: 258.42%0ACurrent Price: 258.42%0A(-1,49% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ROSN/'>ROSN</a> Р РѕСЃРЅРµС„С‚СЊ%0ATracking Price: 489.54998779296875%0ALast Price: 475.2%0ACurrent Price: 475.2%0A(-2,93% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WMT/'>WMT</a> Stores%0ATracking Price: 145.75%0ALast Price: 140.65%0ACurrent Price: 140.65%0A(-3,50% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ACN/'>ACN</a> Accenture%0ATracking Price: 246.21%0ALast Price: 243.66%0ACurrent Price: 243.66%0A(-1,04% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MA/'>MA</a> Mastercard%0ATracking Price: 337.86%0ALast Price: 315.51%0ACurrent Price: 315.51%0A(-6,62% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AAPL/'>AAPL</a> Apple%0ATracking Price: 140.0%0ALast Price: 131.94%0ACurrent Price: 131.94%0A(-5,76% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MSFT/'>MSFT</a> Corporation%0ATracking Price: 237.0%0ALast Price: 232.24%0ACurrent Price: 232.24%0A(-2,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEE/'>NEE</a> Energy%0ATracking Price: 83.0%0ALast Price: 80.55%0ACurrent Price: 80.55%0A(-2,95% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SEDG/'>SEDG</a> Inc%0ATracking Price: 312.41%0ALast Price: 290.0%0ACurrent Price: 290.0%0A(-7,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/YNDX/'>YNDX</a> Yandex%0ATracking Price: 5400.0%0ALast Price: 4785.8%0ACurrent Price: 4785.8%0A(-11,37% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NFLX/'>NFLX</a> Netflix%0ATracking Price: 543.0%0ALast Price: 530.65%0ACurrent Price: 530.65%0A(-2,27% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CLX/'>CLX</a> Company%0ATracking Price: 223.0%0ALast Price: 210.24%0ACurrent Price: 210.24%0A(-5,72% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LKOH/'>LKOH</a> Р›РЈРљРћР™Р›%0ATracking Price: 5672.0%0ALast Price: 5401.0%0ACurrent Price: 5401.0%0A(-4,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AIZ/'>AIZ</a> Assurant%0ATracking Price: 136.5399932861328%0ALast Price: 135.47%0ACurrent Price: 135.47%0A(-0,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ARWR/'>ARWR</a> Inc%0ATracking Price: 79.23999786376953%0ALast Price: 78.81%0ACurrent Price: 78.81%0A(-0,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/FIVE/'>FIVE</a> RetailGroup%0ATracking Price: 2792.0%0ALast Price: 2708.0%0ACurrent Price: 2708.0%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LITE/'>LITE</a> Inc%0ATracking Price: 96.70999908447266%0ALast Price: 93.8%0ACurrent Price: 93.8%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ET/'>ET</a> LP%0ATracking Price: 6.46999979019165%0ALast Price: 6.33%0ACurrent Price: 6.33%0A(-2,16% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NVTK/'>NVTK</a> РќРћР’РђРўР­Рљ%0ATracking Price: 1373.800048828125%0ALast Price: 1274.0%0ACurrent Price: 1274.0%0A(-7,26% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CNC/'>CNC</a> Corporation%0ATracking Price: 64.55999755859375%0ALast Price: 60.3%0ACurrent Price: 60.3%0A(-6,60% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZTS/'>ZTS</a> Zoetis%0ATracking Price: 161.41000366210938%0ALast Price: 154.25%0ACurrent Price: 154.25%0A(-4,44% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WERN/'>WERN</a> Inc%0ATracking Price: 42.25%0ALast Price: 39.24%0ACurrent Price: 39.24%0A(-7,12% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZBH/'>ZBH</a> Holdings%0ATracking Price: 164.0%0ALast Price: 153.67%0ACurrent Price: 153.67%0A(-6,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/DAL/'>DAL</a> Lines%0ATracking Price: 42.099998474121094%0ALast Price: 38.36%0ACurrent Price: 38.36%0A(-8,88% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MRK/'>MRK</a> Merck%0ATracking Price: 82.31999969482422%0ALast Price: 77.38%0ACurrent Price: 77.38%0A(-6,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OGKB/'>OGKB</a> СЌР»РµРєС‚СЂРѕСЌРЅРµСЂРіРёРё%0ATracking Price: 0.7878000140190125%0ALast Price: 0.7625%0ACurrent Price: 0.7625%0A(-3,21% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEM/'>NEM</a> Corporation%0ATracking Price: 62.11000061035156%0ALast Price: 59.71%0ACurrent Price: 59.71%0A(-3,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CMA/'>CMA</a> Incorporated%0ATracking Price: 60.959999084472656%0ALast Price: 57.2%0ACurrent Price: 57.2%0A(-6,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AZPN/'>AZPN</a> Inc%0ATracking Price: 140.0%0ALast Price: 133.9%0ACurrent Price: 133.9%0A(-4,36% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ORCL/'>ORCL</a> Oracle%0ATracking Price: 64.33999633789062%0ALast Price: 60.62%0ACurrent Price: 60.62%0A(-5,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/TOT/'>TOT</a> S.A.%0ATracking Price: 43.0%0ALast Price: 42.04%0ACurrent Price: 42.04%0A(-2,23% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PFG/'>PFG</a> Group%0ATracking Price: 53.0%0ALast Price: 49.33%0ACurrent Price: 49.33%0A(-6,92% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/VZ/'>VZ</a> Communications%0ATracking Price: 57.349998474121094%0ALast Price: 54.75%0ACurrent Price: 54.75%0A(-4,53% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PEG/'>PEG</a> Group%0ATracking Price: 57.0%0ALast Price: 56.43%0ACurrent Price: 56.43%0A(-1,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/UCTT/'>UCTT</a> Inc%0ATracking Price: 41.20000076293945%0ALast Price: 38.7%0ACurrent Price: 38.7%0A(-6,07% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CAH/'>CAH</a> Health%0ATracking Price: 54.720001220703125%0ALast Price: 53.7%0ACurrent Price: 53.7%0A(-1,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OLED/'>OLED</a> Corp%0ATracking Price: 260.0%0ALast Price: 230.0%0ACurrent Price: 230.0%0A(-11,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SQ/'>SQ</a> Square%0ATracking Price: 225.0%0ALast Price: 215.25%0ACurrent Price: 215.25%0A(-4,33% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RTKM/'>RTKM</a> Р РѕСЃС‚РµР»РµРєРѕРј%0ATracking Price: 105.0%0ALast Price: 101.17%0ACurrent Price: 101.17%0A(-3,65% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RUAL/'>RUAL</a> Р РЈРЎРђР›%0ATracking Price: 35.5%0ALast Price: 34.8%0ACurrent Price: 34.8%0A(-1,97% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LMT/'>LMT</a> Martin%0ATracking Price: 330.0%0ALast Price: 322.8%0ACurrent Price: 322.8%0A(-2,18% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PSA/'>PSA</a> Storage%0ATracking Price: 228.0%0ALast Price: 227.62%0ACurrent Price: 227.62%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/BA/'>BA</a> BOEING%0ATracking Price: 197.0%0ALast Price: 194.44%0ACurrent Price: 194.44%0A(-1,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WST/'>WST</a> I%0ATracking Price: 300.0%0ALast Price: 299.49%0ACurrent Price: 299.49%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZYXI/'>ZYXI</a> Inc%0ATracking Price: 19.860000610351562%0ALast Price: 18.27%0ACurrent Price: 18.27%0A(-8,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CTXS/'>CTXS</a> Systems%0ATracking Price: 134.3800048828125%0ALast Price: 133.37%0ACurrent Price: 133.37%0A(-0,75% from Tracking Price, (0,00% from Last Price)%0A%0A. Server returned HTTP response code: 400 for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Stock: <a href='https://tinkoff.ru/invest/stocks/SBER/'>SBER</a> Р РѕСЃСЃРёРё%0ATracking Price: 262.32000732421875%0ALast Price: 258.42%0ACurrent Price: 258.42%0A(-1,49% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ROSN/'>ROSN</a> Р РѕСЃРЅРµС„С‚СЊ%0ATracking Price: 489.54998779296875%0ALast Price: 475.2%0ACurrent Price: 475.2%0A(-2,93% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WMT/'>WMT</a> Stores%0ATracking Price: 145.75%0ALast Price: 140.65%0ACurrent Price: 140.65%0A(-3,50% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ACN/'>ACN</a> Accenture%0ATracking Price: 246.21%0ALast Price: 243.66%0ACurrent Price: 243.66%0A(-1,04% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MA/'>MA</a> Mastercard%0ATracking Price: 337.86%0ALast Price: 315.51%0ACurrent Price: 315.51%0A(-6,62% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AAPL/'>AAPL</a> Apple%0ATracking Price: 140.0%0ALast Price: 131.94%0ACurrent Price: 131.94%0A(-5,76% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MSFT/'>MSFT</a> Corporation%0ATracking Price: 237.0%0ALast Price: 232.24%0ACurrent Price: 232.24%0A(-2,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEE/'>NEE</a> Energy%0ATracking Price: 83.0%0ALast Price: 80.55%0ACurrent Price: 80.55%0A(-2,95% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SEDG/'>SEDG</a> Inc%0ATracking Price: 312.41%0ALast Price: 290.0%0ACurrent Price: 290.0%0A(-7,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/YNDX/'>YNDX</a> Yandex%0ATracking Price: 5400.0%0ALast Price: 4785.8%0ACurrent Price: 4785.8%0A(-11,37% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NFLX/'>NFLX</a> Netflix%0ATracking Price: 543.0%0ALast Price: 530.65%0ACurrent Price: 530.65%0A(-2,27% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CLX/'>CLX</a> Company%0ATracking Price: 223.0%0ALast Price: 210.24%0ACurrent Price: 210.24%0A(-5,72% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LKOH/'>LKOH</a> Р›РЈРљРћР™Р›%0ATracking Price: 5672.0%0ALast Price: 5401.0%0ACurrent Price: 5401.0%0A(-4,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AIZ/'>AIZ</a> Assurant%0ATracking Price: 136.5399932861328%0ALast Price: 135.47%0ACurrent Price: 135.47%0A(-0,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ARWR/'>ARWR</a> Inc%0ATracking Price: 79.23999786376953%0ALast Price: 78.81%0ACurrent Price: 78.81%0A(-0,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/FIVE/'>FIVE</a> RetailGroup%0ATracking Price: 2792.0%0ALast Price: 2708.0%0ACurrent Price: 2708.0%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LITE/'>LITE</a> Inc%0ATracking Price: 96.70999908447266%0ALast Price: 93.8%0ACurrent Price: 93.8%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ET/'>ET</a> LP%0ATracking Price: 6.46999979019165%0ALast Price: 6.33%0ACurrent Price: 6.33%0A(-2,16% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NVTK/'>NVTK</a> РќРћР’РђРўР­Рљ%0ATracking Price: 1373.800048828125%0ALast Price: 1274.0%0ACurrent Price: 1274.0%0A(-7,26% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CNC/'>CNC</a> Corporation%0ATracking Price: 64.55999755859375%0ALast Price: 60.3%0ACurrent Price: 60.3%0A(-6,60% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZTS/'>ZTS</a> Zoetis%0ATracking Price: 161.41000366210938%0ALast Price: 154.25%0ACurrent Price: 154.25%0A(-4,44% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WERN/'>WERN</a> Inc%0ATracking Price: 42.25%0ALast Price: 39.24%0ACurrent Price: 39.24%0A(-7,12% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZBH/'>ZBH</a> Holdings%0ATracking Price: 164.0%0ALast Price: 153.67%0ACurrent Price: 153.67%0A(-6,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/DAL/'>DAL</a> Lines%0ATracking Price: 42.099998474121094%0ALast Price: 38.36%0ACurrent Price: 38.36%0A(-8,88% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MRK/'>MRK</a> Merck%0ATracking Price: 82.31999969482422%0ALast Price: 77.38%0ACurrent Price: 77.38%0A(-6,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OGKB/'>OGKB</a> СЌР»РµРєС‚СЂРѕСЌРЅРµСЂРіРёРё%0ATracking Price: 0.7878000140190125%0ALast Price: 0.7625%0ACurrent Price: 0.7625%0A(-3,21% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEM/'>NEM</a> Corporation%0ATracking Price: 62.11000061035156%0ALast Price: 59.71%0ACurrent Price: 59.71%0A(-3,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CMA/'>CMA</a> Incorporated%0ATracking Price: 60.959999084472656%0ALast Price: 57.2%0ACurrent Price: 57.2%0A(-6,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AZPN/'>AZPN</a> Inc%0ATracking Price: 140.0%0ALast Price: 133.9%0ACurrent Price: 133.9%0A(-4,36% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ORCL/'>ORCL</a> Oracle%0ATracking Price: 64.33999633789062%0ALast Price: 60.62%0ACurrent Price: 60.62%0A(-5,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/TOT/'>TOT</a> S.A.%0ATracking Price: 43.0%0ALast Price: 42.04%0ACurrent Price: 42.04%0A(-2,23% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PFG/'>PFG</a> Group%0ATracking Price: 53.0%0ALast Price: 49.33%0ACurrent Price: 49.33%0A(-6,92% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/VZ/'>VZ</a> Communications%0ATracking Price: 57.349998474121094%0ALast Price: 54.75%0ACurrent Price: 54.75%0A(-4,53% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PEG/'>PEG</a> Group%0ATracking Price: 57.0%0ALast Price: 56.43%0ACurrent Price: 56.43%0A(-1,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/UCTT/'>UCTT</a> Inc%0ATracking Price: 41.20000076293945%0ALast Price: 38.7%0ACurrent Price: 38.7%0A(-6,07% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CAH/'>CAH</a> Health%0ATracking Price: 54.720001220703125%0ALast Price: 53.7%0ACurrent Price: 53.7%0A(-1,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OLED/'>OLED</a> Corp%0ATracking Price: 260.0%0ALast Price: 230.0%0ACurrent Price: 230.0%0A(-11,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SQ/'>SQ</a> Square%0ATracking Price: 225.0%0ALast Price: 215.25%0ACurrent Price: 215.25%0A(-4,33% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RTKM/'>RTKM</a> Р РѕСЃС‚РµР»РµРєРѕРј%0ATracking Price: 105.0%0ALast Price: 101.17%0ACurrent Price: 101.17%0A(-3,65% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RUAL/'>RUAL</a> Р РЈРЎРђР›%0ATracking Price: 35.5%0ALast Price: 34.8%0ACurrent Price: 34.8%0A(-1,97% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LMT/'>LMT</a> Martin%0ATracking Price: 330.0%0ALast Price: 322.8%0ACurrent Price: 322.8%0A(-2,18% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PSA/'>PSA</a> Storage%0ATracking Price: 228.0%0ALast Price: 227.62%0ACurrent Price: 227.62%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/BA/'>BA</a> BOEING%0ATracking Price: 197.0%0ALast Price: 194.44%0ACurrent Price: 194.44%0A(-1,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WST/'>WST</a> I%0ATracking Price: 300.0%0ALast Price: 299.49%0ACurrent Price: 299.49%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZYXI/'>ZYXI</a> Inc%0ATracking Price: 19.860000610351562%0ALast Price: 18.27%0ACurrent Price: 18.27%0A(-8,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CTXS/'>CTXS</a> Systems%0ATracking Price: 134.3800048828125%0ALast Price: 133.37%0ACurrent Price: 133.37%0A(-0,75% from Tracking Price, (0,00% from Last Price)%0A%0A. Server returned HTTP response code: 400 for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Error: Couldn't send message for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Stock: <a href='https://tinkoff.ru/invest/stocks/SBER/'>SBER</a> Р РѕСЃСЃРёРё%0ATracking Price: 262.32000732421875%0ALast Price: 258.42%0ACurrent Price: 258.42%0A(-1,49% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ROSN/'>ROSN</a> Р РѕСЃРЅРµС„С‚СЊ%0ATracking Price: 489.54998779296875%0ALast Price: 475.2%0ACurrent Price: 475.2%0A(-2,93% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WMT/'>WMT</a> Stores%0ATracking Price: 145.75%0ALast Price: 140.65%0ACurrent Price: 140.65%0A(-3,50% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ACN/'>ACN</a> Accenture%0ATracking Price: 246.21%0ALast Price: 243.66%0ACurrent Price: 243.66%0A(-1,04% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MA/'>MA</a> Mastercard%0ATracking Price: 337.86%0ALast Price: 315.51%0ACurrent Price: 315.51%0A(-6,62% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AAPL/'>AAPL</a> Apple%0ATracking Price: 140.0%0ALast Price: 131.94%0ACurrent Price: 131.94%0A(-5,76% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MSFT/'>MSFT</a> Corporation%0ATracking Price: 237.0%0ALast Price: 232.24%0ACurrent Price: 232.24%0A(-2,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEE/'>NEE</a> Energy%0ATracking Price: 83.0%0ALast Price: 80.55%0ACurrent Price: 80.55%0A(-2,95% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SEDG/'>SEDG</a> Inc%0ATracking Price: 312.41%0ALast Price: 290.0%0ACurrent Price: 290.0%0A(-7,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/YNDX/'>YNDX</a> Yandex%0ATracking Price: 5400.0%0ALast Price: 4785.8%0ACurrent Price: 4785.8%0A(-11,37% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NFLX/'>NFLX</a> Netflix%0ATracking Price: 543.0%0ALast Price: 530.65%0ACurrent Price: 530.65%0A(-2,27% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CLX/'>CLX</a> Company%0ATracking Price: 223.0%0ALast Price: 210.24%0ACurrent Price: 210.24%0A(-5,72% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LKOH/'>LKOH</a> Р›РЈРљРћР™Р›%0ATracking Price: 5672.0%0ALast Price: 5401.0%0ACurrent Price: 5401.0%0A(-4,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AIZ/'>AIZ</a> Assurant%0ATracking Price: 136.5399932861328%0ALast Price: 135.47%0ACurrent Price: 135.47%0A(-0,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ARWR/'>ARWR</a> Inc%0ATracking Price: 79.23999786376953%0ALast Price: 78.81%0ACurrent Price: 78.81%0A(-0,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/FIVE/'>FIVE</a> RetailGroup%0ATracking Price: 2792.0%0ALast Price: 2708.0%0ACurrent Price: 2708.0%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LITE/'>LITE</a> Inc%0ATracking Price: 96.70999908447266%0ALast Price: 93.8%0ACurrent Price: 93.8%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ET/'>ET</a> LP%0ATracking Price: 6.46999979019165%0ALast Price: 6.33%0ACurrent Price: 6.33%0A(-2,16% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NVTK/'>NVTK</a> РќРћР’РђРўР­Рљ%0ATracking Price: 1373.800048828125%0ALast Price: 1274.0%0ACurrent Price: 1274.0%0A(-7,26% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CNC/'>CNC</a> Corporation%0ATracking Price: 64.55999755859375%0ALast Price: 60.3%0ACurrent Price: 60.3%0A(-6,60% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZTS/'>ZTS</a> Zoetis%0ATracking Price: 161.41000366210938%0ALast Price: 154.25%0ACurrent Price: 154.25%0A(-4,44% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WERN/'>WERN</a> Inc%0ATracking Price: 42.25%0ALast Price: 39.24%0ACurrent Price: 39.24%0A(-7,12% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZBH/'>ZBH</a> Holdings%0ATracking Price: 164.0%0ALast Price: 153.67%0ACurrent Price: 153.67%0A(-6,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/DAL/'>DAL</a> Lines%0ATracking Price: 42.099998474121094%0ALast Price: 38.36%0ACurrent Price: 38.36%0A(-8,88% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MRK/'>MRK</a> Merck%0ATracking Price: 82.31999969482422%0ALast Price: 77.38%0ACurrent Price: 77.38%0A(-6,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OGKB/'>OGKB</a> СЌР»РµРєС‚СЂРѕСЌРЅРµСЂРіРёРё%0ATracking Price: 0.7878000140190125%0ALast Price: 0.7625%0ACurrent Price: 0.7625%0A(-3,21% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEM/'>NEM</a> Corporation%0ATracking Price: 62.11000061035156%0ALast Price: 59.71%0ACurrent Price: 59.71%0A(-3,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CMA/'>CMA</a> Incorporated%0ATracking Price: 60.959999084472656%0ALast Price: 57.2%0ACurrent Price: 57.2%0A(-6,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AZPN/'>AZPN</a> Inc%0ATracking Price: 140.0%0ALast Price: 133.9%0ACurrent Price: 133.9%0A(-4,36% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ORCL/'>ORCL</a> Oracle%0ATracking Price: 64.33999633789062%0ALast Price: 60.62%0ACurrent Price: 60.62%0A(-5,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/TOT/'>TOT</a> S.A.%0ATracking Price: 43.0%0ALast Price: 42.04%0ACurrent Price: 42.04%0A(-2,23% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PFG/'>PFG</a> Group%0ATracking Price: 53.0%0ALast Price: 49.33%0ACurrent Price: 49.33%0A(-6,92% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/VZ/'>VZ</a> Communications%0ATracking Price: 57.349998474121094%0ALast Price: 54.75%0ACurrent Price: 54.75%0A(-4,53% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PEG/'>PEG</a> Group%0ATracking Price: 57.0%0ALast Price: 56.43%0ACurrent Price: 56.43%0A(-1,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/UCTT/'>UCTT</a> Inc%0ATracking Price: 41.20000076293945%0ALast Price: 38.7%0ACurrent Price: 38.7%0A(-6,07% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CAH/'>CAH</a> Health%0ATracking Price: 54.720001220703125%0ALast Price: 53.7%0ACurrent Price: 53.7%0A(-1,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OLED/'>OLED</a> Corp%0ATracking Price: 260.0%0ALast Price: 230.0%0ACurrent Price: 230.0%0A(-11,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SQ/'>SQ</a> Square%0ATracking Price: 225.0%0ALast Price: 215.25%0ACurrent Price: 215.25%0A(-4,33% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RTKM/'>RTKM</a> Р РѕСЃС‚РµР»РµРєРѕРј%0ATracking Price: 105.0%0ALast Price: 101.17%0ACurrent Price: 101.17%0A(-3,65% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RUAL/'>RUAL</a> Р РЈРЎРђР›%0ATracking Price: 35.5%0ALast Price: 34.8%0ACurrent Price: 34.8%0A(-1,97% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LMT/'>LMT</a> Martin%0ATracking Price: 330.0%0ALast Price: 322.8%0ACurrent Price: 322.8%0A(-2,18% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PSA/'>PSA</a> Storage%0ATracking Price: 228.0%0ALast Price: 227.62%0ACurrent Price: 227.62%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/BA/'>BA</a> BOEING%0ATracking Price: 197.0%0ALast Price: 194.44%0ACurrent Price: 194.44%0A(-1,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WST/'>WST</a> I%0ATracking Price: 300.0%0ALast Price: 299.49%0ACurrent Price: 299.49%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZYXI/'>ZYXI</a> Inc%0ATracking Price: 19.860000610351562%0ALast Price: 18.27%0ACurrent Price: 18.27%0A(-8,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CTXS/'>CTXS</a> Systems%0ATracking Price: 134.3800048828125%0ALast Price: 133.37%0ACurrent Price: 133.37%0A(-0,75% from Tracking Price, (0,00% from Last Price)%0A%0A. Server returned HTTP response code: 400 for URL: https://api.telegram.org/bot1462150365:AAEcq2HQXEtuhvWCnDZFGN5FHfgneDZCTcA/sendMessage?chat_id=-1001486526888&parse_mode=html&disable_web_page_preview=True&text=Stock: <a href='https://tinkoff.ru/invest/stocks/SBER/'>SBER</a> Р РѕСЃСЃРёРё%0ATracking Price: 262.32000732421875%0ALast Price: 258.42%0ACurrent Price: 258.42%0A(-1,49% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ROSN/'>ROSN</a> Р РѕСЃРЅРµС„С‚СЊ%0ATracking Price: 489.54998779296875%0ALast Price: 475.2%0ACurrent Price: 475.2%0A(-2,93% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WMT/'>WMT</a> Stores%0ATracking Price: 145.75%0ALast Price: 140.65%0ACurrent Price: 140.65%0A(-3,50% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ACN/'>ACN</a> Accenture%0ATracking Price: 246.21%0ALast Price: 243.66%0ACurrent Price: 243.66%0A(-1,04% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MA/'>MA</a> Mastercard%0ATracking Price: 337.86%0ALast Price: 315.51%0ACurrent Price: 315.51%0A(-6,62% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AAPL/'>AAPL</a> Apple%0ATracking Price: 140.0%0ALast Price: 131.94%0ACurrent Price: 131.94%0A(-5,76% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MSFT/'>MSFT</a> Corporation%0ATracking Price: 237.0%0ALast Price: 232.24%0ACurrent Price: 232.24%0A(-2,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEE/'>NEE</a> Energy%0ATracking Price: 83.0%0ALast Price: 80.55%0ACurrent Price: 80.55%0A(-2,95% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SEDG/'>SEDG</a> Inc%0ATracking Price: 312.41%0ALast Price: 290.0%0ACurrent Price: 290.0%0A(-7,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/YNDX/'>YNDX</a> Yandex%0ATracking Price: 5400.0%0ALast Price: 4785.8%0ACurrent Price: 4785.8%0A(-11,37% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NFLX/'>NFLX</a> Netflix%0ATracking Price: 543.0%0ALast Price: 530.65%0ACurrent Price: 530.65%0A(-2,27% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CLX/'>CLX</a> Company%0ATracking Price: 223.0%0ALast Price: 210.24%0ACurrent Price: 210.24%0A(-5,72% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LKOH/'>LKOH</a> Р›РЈРљРћР™Р›%0ATracking Price: 5672.0%0ALast Price: 5401.0%0ACurrent Price: 5401.0%0A(-4,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AIZ/'>AIZ</a> Assurant%0ATracking Price: 136.5399932861328%0ALast Price: 135.47%0ACurrent Price: 135.47%0A(-0,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ARWR/'>ARWR</a> Inc%0ATracking Price: 79.23999786376953%0ALast Price: 78.81%0ACurrent Price: 78.81%0A(-0,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/FIVE/'>FIVE</a> RetailGroup%0ATracking Price: 2792.0%0ALast Price: 2708.0%0ACurrent Price: 2708.0%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LITE/'>LITE</a> Inc%0ATracking Price: 96.70999908447266%0ALast Price: 93.8%0ACurrent Price: 93.8%0A(-3,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ET/'>ET</a> LP%0ATracking Price: 6.46999979019165%0ALast Price: 6.33%0ACurrent Price: 6.33%0A(-2,16% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NVTK/'>NVTK</a> РќРћР’РђРўР­Рљ%0ATracking Price: 1373.800048828125%0ALast Price: 1274.0%0ACurrent Price: 1274.0%0A(-7,26% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CNC/'>CNC</a> Corporation%0ATracking Price: 64.55999755859375%0ALast Price: 60.3%0ACurrent Price: 60.3%0A(-6,60% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZTS/'>ZTS</a> Zoetis%0ATracking Price: 161.41000366210938%0ALast Price: 154.25%0ACurrent Price: 154.25%0A(-4,44% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WERN/'>WERN</a> Inc%0ATracking Price: 42.25%0ALast Price: 39.24%0ACurrent Price: 39.24%0A(-7,12% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZBH/'>ZBH</a> Holdings%0ATracking Price: 164.0%0ALast Price: 153.67%0ACurrent Price: 153.67%0A(-6,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/DAL/'>DAL</a> Lines%0ATracking Price: 42.099998474121094%0ALast Price: 38.36%0ACurrent Price: 38.36%0A(-8,88% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/MRK/'>MRK</a> Merck%0ATracking Price: 82.31999969482422%0ALast Price: 77.38%0ACurrent Price: 77.38%0A(-6,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OGKB/'>OGKB</a> СЌР»РµРєС‚СЂРѕСЌРЅРµСЂРіРёРё%0ATracking Price: 0.7878000140190125%0ALast Price: 0.7625%0ACurrent Price: 0.7625%0A(-3,21% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/NEM/'>NEM</a> Corporation%0ATracking Price: 62.11000061035156%0ALast Price: 59.71%0ACurrent Price: 59.71%0A(-3,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CMA/'>CMA</a> Incorporated%0ATracking Price: 60.959999084472656%0ALast Price: 57.2%0ACurrent Price: 57.2%0A(-6,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/AZPN/'>AZPN</a> Inc%0ATracking Price: 140.0%0ALast Price: 133.9%0ACurrent Price: 133.9%0A(-4,36% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ORCL/'>ORCL</a> Oracle%0ATracking Price: 64.33999633789062%0ALast Price: 60.62%0ACurrent Price: 60.62%0A(-5,78% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/TOT/'>TOT</a> S.A.%0ATracking Price: 43.0%0ALast Price: 42.04%0ACurrent Price: 42.04%0A(-2,23% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PFG/'>PFG</a> Group%0ATracking Price: 53.0%0ALast Price: 49.33%0ACurrent Price: 49.33%0A(-6,92% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/VZ/'>VZ</a> Communications%0ATracking Price: 57.349998474121094%0ALast Price: 54.75%0ACurrent Price: 54.75%0A(-4,53% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PEG/'>PEG</a> Group%0ATracking Price: 57.0%0ALast Price: 56.43%0ACurrent Price: 56.43%0A(-1,00% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/UCTT/'>UCTT</a> Inc%0ATracking Price: 41.20000076293945%0ALast Price: 38.7%0ACurrent Price: 38.7%0A(-6,07% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CAH/'>CAH</a> Health%0ATracking Price: 54.720001220703125%0ALast Price: 53.7%0ACurrent Price: 53.7%0A(-1,86% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/OLED/'>OLED</a> Corp%0ATracking Price: 260.0%0ALast Price: 230.0%0ACurrent Price: 230.0%0A(-11,54% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/SQ/'>SQ</a> Square%0ATracking Price: 225.0%0ALast Price: 215.25%0ACurrent Price: 215.25%0A(-4,33% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RTKM/'>RTKM</a> Р РѕСЃС‚РµР»РµРєРѕРј%0ATracking Price: 105.0%0ALast Price: 101.17%0ACurrent Price: 101.17%0A(-3,65% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/RUAL/'>RUAL</a> Р РЈРЎРђР›%0ATracking Price: 35.5%0ALast Price: 34.8%0ACurrent Price: 34.8%0A(-1,97% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/LMT/'>LMT</a> Martin%0ATracking Price: 330.0%0ALast Price: 322.8%0ACurrent Price: 322.8%0A(-2,18% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/PSA/'>PSA</a> Storage%0ATracking Price: 228.0%0ALast Price: 227.62%0ACurrent Price: 227.62%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/BA/'>BA</a> BOEING%0ATracking Price: 197.0%0ALast Price: 194.44%0ACurrent Price: 194.44%0A(-1,30% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/WST/'>WST</a> I%0ATracking Price: 300.0%0ALast Price: 299.49%0ACurrent Price: 299.49%0A(-0,17% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/ZYXI/'>ZYXI</a> Inc%0ATracking Price: 19.860000610351562%0ALast Price: 18.27%0ACurrent Price: 18.27%0A(-8,01% from Tracking Price, (0,00% from Last Price)%0A%0AStock: <a href='https://tinkoff.ru/invest/stocks/CTXS/'>CTXS</a> Systems%0ATracking Price: 134.3800048828125%0ALast Price: 133.37%0ACurrent Price: 133.37%0A(-0,75% from Tracking Price, (0,00% from Last Price)%0A%0A

//			telegramSendMessage.sendMessage("Error: " + e.getMessage());
		}
	}

}
