package telegrambot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import ru.kvaga.investments.stocks.StocksTrackingException.GetContentOFSiteException;
import ru.kvaga.investments.stocks.StocksTrackingException.GetCurrentPriceOfStockException.Common;
import ru.kvaga.investments.stocks.StocksTrackingException.GetFullStockNameException.ParsingResponseException;
import ru.kvaga.investments.stocks.StocksTrackingException;
//import ru.kvaga.investments.stocks.StocksTrackingLib;
import telegrambot.InvestBotException.GetURLContentException;


public class TelegramBotLib {

	public static telegrambot.User getUser(Update update) {
		String _userName=getIncomingUserName(update);
		
		return Users.getUser(_userName);
	}
	
	public static String getURLContent(String urlText) throws InvestBotException.GetURLContentException {
		String body = null;
		String charset; // You should determine it based on response header.
		HttpURLConnection con=null;

		try {
			URL url = new URL(urlText);
			con = (HttpURLConnection) url.openConnection();
//			con.connect();

//			System.out.println("Con: " + con.getResponseCode());
//			log.debug("Connection response code: " + con.getResponseCode());
			con.setRequestMethod("GET");
			con.setRequestProperty("accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
			con.setRequestProperty("accept-encoding", "gzip, deflate, br");
			con.setRequestProperty("accept-language", "en-GB,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6");
			con.setRequestProperty("cache-control", "max-age=0");
			con.setRequestProperty("sec-ch-ua",
					"\"Google Chrome\";v=\"87\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"87\"");
			con.setRequestProperty("sec-ch-ua-mobile", "?0");
			con.setRequestProperty("sec-fetch-dest", "document");
			con.setRequestProperty("sec-fetch-mode", "navigate");
			con.setRequestProperty("sec-fetch-site", "none");
			con.setRequestProperty("sec-fetch-user", "?1");
			con.setRequestProperty("upgrade-insecure-requests", "1");
			con.setRequestProperty("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");

			if (con.getContentType().toLowerCase().contains("charset=utf-8")) {
				charset = "UTF-8";
			} else {
				throw new InvestBotException.GetURLContentException(urlText,
						String.format("Received unsupported charset: %s. ", con.getContentType()));
			}
			if (con.getContentEncoding().equals("gzip")) {
				try (InputStream gzippedResponse = con.getInputStream();
						InputStream ungzippedResponse = new GZIPInputStream(gzippedResponse);
						Reader reader = new InputStreamReader(ungzippedResponse, charset);
						Writer writer = new StringWriter();) {
					char[] buffer = new char[10240];
					for (int length = 0; (length = reader.read(buffer)) > 0;) {
						writer.write(buffer, 0, length);
					}
					body = writer.toString();
					writer.close();
//				    System.err.println(body);
				}

			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String s;
//				System.err.println("Response Message: " + con.getContentEncoding());
				StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null) {
//					System.out.println(s);
					sb.append(s);
				}
				body = sb.toString();
				br.close();
			}
			return body;
			
		} catch (Exception e) {
			e.printStackTrace();
//			log.error("GetURLContentException: couldn't get a content for the ["+urlText+"] URL", e);
			if(con!=null) {
				con.disconnect();
			}
			throw new InvestBotException.GetURLContentException(e.getMessage(),urlText);
		}
	}

	public static boolean stockExistsByTicker(String ticker) throws GetURLContentException, ParsingResponseException  {
		String URL_TEXT_TINKOFF="https://www.tinkoff.ru/invest/stocks/%s/";
		String url=String.format(URL_TEXT_TINKOFF, ticker);
		
		System.out.println("Url is ready: " + url);
		String response=getURLContent(url);
//		System.out.println("RESP: ["+response+"]");
		System.out.println("The response received");
		String fullName=TelegramBotLib.getFullNameOfStock(response, ticker, url);
		System.out.println("Full name received: " + fullName);
//		currentPrice = getCurrentPriceOfStock(si.getName(), response, url);
//		System.out.println("Current price received: " + currentPrice);
		return true;
	}
	
	
	public static String getFullNameOfStock(String response, String stockName, String urlText) throws ru.kvaga.investments.stocks.StocksTrackingException.GetFullStockNameException.ParsingResponseException {
		String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME="<meta charset=\"UTF-8\">.*" + 
				"<title data-meta-dynamic=\"true\">Купить акции (?<fullName>.*) \\("+stockName+"\\).*</title>.*" + 
				"<meta property=\"og:title\"" ;
		response=response.replaceAll("\r\n", "").replaceAll("\n", "");
//		response=new String(response.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//		System.out.println("RESPONSE: " + response);

		Pattern patternForFullName = Pattern.compile(REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME);
		Matcher matcherForFullName = patternForFullName.matcher(response);
		String stockFullName;
		if(matcherForFullName.find()) {
			stockFullName=matcherForFullName.group("fullName");
		}else {
			throw new StocksTrackingException.
			GetFullStockNameException.
			ParsingResponseException(String.format("Couldn't find fullName for stock during parsing web response with "
					+ "regex pattern text [%s]. \n"
//					+ "Web response [%s]"
					,
					REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME
//					,response
					), 
					urlText);
		}
		return stockFullName;
		
	}
	
	
	public static String getIncomingUserName(Update update) {
		String _userName=null;
		if(update.hasMessage()) {
			_userName=update.getMessage().getChat().getUserName();
		}else if(update.hasChannelPost()) {
			_userName=update.getChannelPost().getChat().getUserName();
		}else if(update.hasCallbackQuery()) {
			_userName=update.getCallbackQuery().getMessage().getChat().getUserName();
		}else {
			return null;
		}
		return _userName;
	}
	
	public static void main(String args[]) throws ParsingResponseException {
		String url="https://www.tinkoff.ru/invest/stocks/SBER/";
		try {
			String response =getURLContent(url);
//			System.out.println(response.substring(0, 200));
			System.out.println(getFullNameOfStock(response, "SBER", url));
		} catch (GetURLContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


