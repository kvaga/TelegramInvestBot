package telegrambot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import com.google.common.base.Ticker;

import ru.kvaga.invest.investbot.jobs.UpdateCurrentPricesOfStocksJob;
import ru.kvaga.invest.investbot.jobs.UpdateProfitabilityOfBonds;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.investments.stocks.StockItemComparatorByTicker;
import ru.kvaga.investments.stocks.StocksTrackingException;
import ru.kvaga.investments.stocks.StocksTrackingException.GetContentOFSiteException;
import ru.kvaga.investments.stocks.StocksTrackingException.GetCurrentPriceOfStockException.Common;
import ru.kvaga.investments.stocks.StocksTrackingException.GetFullStockNameException.ParsingResponseException;
import ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.IncorrectFormatOfRow;
import ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.ItemsFileNotFound;
import ru.kvaga.investments.stocks.StocksTrackingLib;
import telegrambot.InvestBotException.UserIncorrectScenarioException;
import telegrambot.InvestBotException.UserIncorrectStateException;
import telegrambot.InvestBotException.UserNotFoundException;

// https://habr.com/ru/post/476306/

public class InvestBot extends TelegramLongPollingBot {
	private static final Logger log = Logger.getLogger(InvestBot.class);
	private static int count = 0;
	final int RECONNECT_PAUSE = 10000;

	String botUserName;
	String token;

	public InvestBot(String botUserName, String token) {
		this.botUserName = botUserName;
		this.token = token;
	}

	public void setBotUsername(String botUserName) {
		this.botUserName = botUserName;
	}

	public String getBotUsername() {
		return botUserName;
	}

	public void setBotToken(String token) {
		this.token = token;
	}

	public String getBotToken() {
		return token;
	}

	
	public void simpleReply(Update update, String text) {
		try {
			Long chatId = 1L;
			if (update.hasCallbackQuery()) {
				chatId = update.getCallbackQuery().getMessage().getChatId();
			} else if (update.hasChannelPost()) {
				chatId = update.getChannelPost().getChatId();
			} else if (update.hasMessage()) {
				chatId = update.getMessage().getChatId();
			} 

			execute(new SendMessage(chatId, text.length()>4096?text.substring(0,4095):text).setParseMode("html").disableWebPagePreview());
		} catch (TelegramApiException e) {
			log.error("", e);		
		}
	}

	
	
	
	


	public void scenarioAddStockToWatchList(Update update, User user, String URL_TEXT_TINKOFF, File dataFile) throws UserIncorrectStateException, StocksTrackingException {
		String url="";
		//String URL_TEXT_TINKOFF="https://www.tinkoff.ru/invest/stocks/%s/";

//		simpleReply(update, "<a href=\"https://www.tinkoff.ru/invest/stocks/NFLX/\">NFLX</a>: 123");

		//  Получаем тикер
		if (user.getState() == User.STATE_WAIT_FOR_TICKER) {
			if(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST()==null) {
				ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST objectForSCENARIO_ADD_STOCK_TO_WATCHLIST = new ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST();
				user.setObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST(objectForSCENARIO_ADD_STOCK_TO_WATCHLIST);
			}
			user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().setStockTiker(update.getMessage().getText().toUpperCase());

			if(update.getMessage().getText().split(" ").length==2) {
				String ticker=update.getMessage().getText().split(" ")[0];
				String _price=update.getMessage().getText().split(" ")[1];

				log.debug("Got string [" + update.getMessage().getText() + "] with two elements. Possibly they are ticker ["+ticker+"] and price ["+_price+"]. Check this out");
				user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().setStockTiker(ticker.toUpperCase());
				try {
					user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().setWatchPrice(Float.parseFloat(_price));
				}catch(NumberFormatException e) {
					log.error("Can't parse value [" + _price + "]");
					simpleReply(update, "Это не может быть ценой: [" + _price + "]. Введите тикер отдельное либо тикер и цену через пробел");
					return;
				}
				user.setState(User.STATE_WAIT_FOR_WATCH_PRICE);
				log.debug("Set state User.STATE_WAIT_FOR_WATCH_PRICE");
			}
			
			//Проверяем существует ли такая акция по тикеру
			try {
//				if(!TelegramBotLib.stockExistsByTicker(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker())) {
//					simpleReply(update, "Тикер ["+update.getMessage().getText()+"] не найден на биржах. Проверьте корректность написания тикера и пришлите повторно");
//					return;
//				}
				
				url=String.format(URL_TEXT_TINKOFF, user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker());
				String ticker=user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker();
				if(user.getScenario()==User.SCENARIO_ADD_STOCK_TO_WATCHLIST) {
					
					user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST()
					.setStockName(StocksTrackingLib.getFullNameOfStock(TelegramBotLib.getURLContent(url), ticker, url,String.format(ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS,ticker)));
				}else if(user.getScenario()==User.SCENARIO_ADD_FUND_TO_WATCHLIST) {

					user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST()
					.setStockName(StocksTrackingLib.getFullNameOfStock(TelegramBotLib.getURLContent(url), ticker, url,String.format(ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS,ticker)));
				}else if(user.getScenario()==User.SCENARIO_ADD_BOND_TO_WATCHLIST) {

					user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST()
					.setStockName(StocksTrackingLib.getFullNameOfStock(TelegramBotLib.getURLContent(url), ticker, url,String.format(ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS,ticker)));
				}

				} catch (Exception e) {
				simpleReply(update, "Тикер ["+user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker()+"] не найден на биржах. Проверьте корректность написания тикера и пришлите повторно");
				log.error("", e);				
				return;
			} 
			//
			if(user.getState()!=User.STATE_WAIT_FOR_WATCH_PRICE) {
				user.setState(User.STATE_WAIT_FOR_WATCH_PRICE);
				simpleReply(update, "Введите цену для отслеживания");
				return;
			}
		}
		
		// Получаем цену отслеживания
		if (user.getState() == User.STATE_WAIT_FOR_WATCH_PRICE) {
			log.debug("State User.STATE_WAIT_FOR_WATCH_PRICE processing ...");
			StockItem stockItem=null;
			StringBuilder sb = null;
			float watchPrice = -Float.MAX_VALUE;
			try {
				if(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice()==Float.MIN_VALUE) {
					watchPrice = Float.parseFloat(update.getMessage().getText());
				}else {
					watchPrice = user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice();
				}
				user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().setWatchPrice(watchPrice);
				//File dataFile = new File(App.DATA_FILE);
				
				// Check and find already existed stock item in the data file
				stockItem = StocksTrackingLib.getStockItemByTickerFromFile(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(),  dataFile);
				ArrayList<StockItem> al = StocksTrackingLib.getListOfStocksFromFile(dataFile);
				log.debug("Работаем с StockItem="+stockItem);
				if(stockItem!=null) {
//					StocksTrackingLib.removeStockItemByTikerFromFile(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(), dataFile);
					for(StockItem si : al) {
						if(si.getName().equals(stockItem.getName())) {
							si.setTraceablePrice(watchPrice);
							log.debug("Установили цену отслеживания " + watchPrice + " для тикера " + si.getName());
							break;
						}
					}
					
				}else {
					StockItem _s = new StockItem();
					_s.setName(user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker());
					_s.setLastPrice(0);
					_s.setTraceablePrice(watchPrice);
					al.add(_s);
				}
				// some code to save data to file on server
				StocksTrackingLib.storeActualData(dataFile, al);
				
				// Prepare for printing full list of stored stocks
				sb = new StringBuilder();
				sb.append("Текущий список активов с ценами отслеживания\n");
				Collections.sort(al, new StockItemComparatorByTicker());
				for(StockItem si : al) {
					sb.append("<a href=\""+String.format(URL_TEXT_TINKOFF, si.getName())+"\">"+si.getName()+"</a>: "+si.getTraceablePrice()+"\n");
				}
				sb.append("\n");
			} catch (NumberFormatException e) {
				simpleReply(update, "Цена должна быть числовой. Введите повторно");
				return;
			} catch (ru.kvaga.investments.stocks.StocksTrackingException.ReadStockItemsFileException.Common e) {
				// TODO Auto-generated catch block
				log.error("", e);				
			} catch (IncorrectFormatOfRow e) {
				// TODO Auto-generated catch block
				log.error("", e);				
			} catch (ItemsFileNotFound e) {
				// TODO Auto-generated catch block
				log.error("", e);				
			}
			try {
				if(stockItem!=null) {
					simpleReply(update, String.format("Для актива [%s:%s] установлена новая цена отслеживания %s вместо прежней %s",user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockName(),user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(), ""+user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice(), stockItem.getTraceablePrice()));
				}else {
					simpleReply(update, String.format("Для актива [%s:%s] установлена цена отслеживания %s",user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockName(),user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(), ""+user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice()));
				}
//				if(stockItem!=null) {
//					App.telegramSendMessage.sendMessage( String.format("Для актива [%s:%s] установлена новая цена отслеживания %s вместо прежней %s",user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockName(),user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(), ""+user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice(), stockItem.getTraceablePrice()));
//				}else {
//					App.telegramSendMessage.sendMessage( String.format("Для актива [%s:%s] установлена цена отслеживания %s",user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockName(),user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getStockTiker(), ""+user.getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST().getWatchPrice()));
//				}
//                 execute(sendIKMenuOfAllowedOperations(update.getMessage().getChatId()));
				log.debug("Отправляем актуальный список активов и цен отслеживаний");
				simpleReply(update, sb.toString());
			}catch(Exception e) {
				log.error("Exception", e);
			}finally {
			
				user.setState(User.STATE_IDLE);
				try {
					user.setScenario(User.SCENARIO_NONE);
				} catch (UserIncorrectScenarioException e) {
					// TODO Auto-generated catch block
					log.error("", e);				
				}
			}

		}

		
		if (update.hasMessage()) {
			log.debug("[scenarioAddStockToWatchList point 1]");
			if (update.getMessage().hasText()) {
						// old 
			}
		} else if (update.hasChannelPost()) {

			Long chatId = update.getChannelPost().getChatId();

			String inputText = update.getChannelPost().getText();

		}

	}
	
	public void sendListOfTrackingItemsFromFile(String urlTinkoff, File dataFile, Update update) {
		ArrayList<StockItem> al;
		try {
			al = StocksTrackingLib.getListOfStocksFromFile(dataFile);
			StringBuilder sb = new StringBuilder();
			sb.append("Текущий список активов с ценами отслеживания\n");
			Collections.sort(al, new StockItemComparatorByTicker());
			for (StockItem si : al) {
				sb.append("<a href=\"" + String.format(urlTinkoff, si.getName()) + "\">" + si.getName() + "</a>: "
						+ si.getTraceablePrice() + "\n");
			}
			sb.append("\n");
			log.debug("Отправляем актуальный список активов и цен отслеживаний");
			simpleReply(update, sb.toString());
		} catch (StocksTrackingException e) {
			log.error("Couldn't send list of tracking items from file ["+dataFile+"] and url ["+urlTinkoff+"]", e);
		}
	}
	public void onUpdateReceived(Update update) {
		
//		simpleReply(update, "<a href=\"https://www.tinkoff.ru/invest/stocks/NFLX/\">NFLX</a>: 123\n"
//				+ "<a href=\"https://www.tinkoff.ru/invest/stocks/SBER/\">SBER</a>: 123");

//    	System.out.println("SCENARIO_ADD_STOCK_TO_PORTFOLIO_BOL="+SCENARIO_ADD_STOCK_TO_PORTFOLIO_BOL);
		User user=null;
		String userName=TelegramBotLib.getIncomingUserName(update);
		
		user = TelegramBotLib.getUser(update);
		
		if(user==null) {
			simpleReply(update, "User ["+userName+"] is not registered");
			return;
		}
		if(update.getMessage()!=null && update.getMessage().getText().equals("/addstock")) {
			try {
				user.setScenario(User.SCENARIO_ADD_STOCK_TO_WATCHLIST);
				user.setState(user.STATE_WAIT_FOR_TICKER);
				simpleReply(update, "Введите тикер и цену через пробел");
				return;
			} catch (UserIncorrectScenarioException e) {
				log.error("Couldn't set scenario [SCENARIO_ADD_STOCK_TO_WATCHLIST] for user ["+user.getUserName()+"]");
				return;
			} catch (UserIncorrectStateException e) {
				log.error("Couldn't set state [STATE_WAIT_FOR_TICKER] for user ["+user.getUserName()+"]");
				return;
			}
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/addbond")) {
			try {
				user.setScenario(User.SCENARIO_ADD_BOND_TO_WATCHLIST);
				user.setState(user.STATE_WAIT_FOR_TICKER);
				simpleReply(update, "Введите тикер и цену через пробел");
				return;
			} catch (UserIncorrectScenarioException e) {
				log.error("Couldn't set scenario [SCENARIO_ADD_BOND_TO_WATCHLIST] for user ["+user.getUserName()+"]");
				return;
			} catch (UserIncorrectStateException e) {
				log.error("Couldn't set state [STATE_WAIT_FOR_TICKER] for user ["+user.getUserName()+"]");
				return;
			}
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/addetf")) {
			try {
				user.setScenario(User.SCENARIO_ADD_FUND_TO_WATCHLIST);
				user.setState(user.STATE_WAIT_FOR_TICKER);
				simpleReply(update, "Введите тикер и цену через пробел");
				return;
			} catch (UserIncorrectScenarioException e) {
				log.error("Couldn't set scenario [SCENARIO_ADD_FUND_TO_WATCHLIST] for user ["+user.getUserName()+"]");
				return;
			} catch (UserIncorrectStateException e) {
				log.error("Couldn't set state [STATE_WAIT_FOR_TICKER] for user ["+user.getUserName()+"]");
				return;
			}
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/listetf")) {
			sendListOfTrackingItemsFromFile(ConfigMap.URL_TEXT_TINKOFF_ETFS, new File(App.DATA_FILE_ETFS), update);
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/liststocks")) {
			sendListOfTrackingItemsFromFile(ConfigMap.URL_TEXT_TINKOFF_STOCKS, new File(App.DATA_FILE_STOCKS), update);
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/listbonds")) {
			sendListOfTrackingItemsFromFile(ConfigMap.URL_TEXT_TINKOFF_BONDS, new File(App.DATA_FILE_BONDS), update);
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/disableenablejobs")) {
			if(ConfigMap.jobsEnabledBol) {
				ConfigMap.jobsEnabledBol=false;
				log.debug("User disabled jobs");
				simpleReply(update, "Jobs disabled");
			}else {
				ConfigMap.jobsEnabledBol=true;
				log.debug("User enabled jobs");
				simpleReply(update, "Jobs enabled");
			}
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/getbondsprofitability")) {
			new Thread(new UpdateProfitabilityOfBonds(true)).start();
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/getstocksprices")) {
			new Thread(new UpdateCurrentPricesOfStocksJob(true, "Stock",new File(App.DATA_FILE_STOCKS), ConfigMap.URL_TEXT_TINKOFF_STOCKS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS)).start();
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/getbondsprices")) {
			new Thread(new UpdateCurrentPricesOfStocksJob(true, "Bond",new File(App.DATA_FILE_BONDS), ConfigMap.URL_TEXT_TINKOFF_BONDS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS)).start();
			return;
		}else if(update.getMessage()!=null && update.getMessage().getText().equals("/getetfprices")) {
			new Thread(new UpdateCurrentPricesOfStocksJob(true, "ETF",new File(App.DATA_FILE_ETFS), ConfigMap.URL_TEXT_TINKOFF_ETFS, ConfigMap.REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS)).start();
			return;
		}
		
		log.debug("User="+user+", userName="+userName+", user.scenario="+User.getScenarionNAmeById(user.getScenario()) + ", state="+User.getStateNameById(user.getState()));
		
		switch (user.getScenario()){
			case User.SCENARIO_NONE:
				log.debug("SCENARIO_NONE");
				try {
						if(user.getState()==User.STATE_IDLE) {
							log.debug("Отправили приветственное сообщение. Отправили меню. Перевели состояние в STATE_WAIT_FOR_CHOICE_OF_OPERATION");
							simpleReply(update, "Привет, " + user.getUserName());
						// Показываем меню с доступными операциями
							execute(sendIKMenuOfAllowedOperations(update.getMessage().getChatId()));
							user.setState(User.STATE_WAIT_FOR_CHOICE_OF_OPERATION);
						}else if(user.getState()==User.STATE_WAIT_FOR_CHOICE_OF_OPERATION) {
							log.debug("Зашли в блок STATE_WAIT_FOR_CHOICE_OF_OPERATION");
							log.debug("update.hasCallbackQuery()="+update.hasCallbackQuery() + ", update.getCallbackQuery().getData()='"+update.getCallbackQuery().getData()+"'");
							if (update.hasCallbackQuery()) {
//								try {
									if (update.getCallbackQuery().getData().equals(""+User.SCENARIO_ADD_STOCK_TO_WATCHLIST)) {
										log.debug("User.SCENARIO_ADD_STOCK_TO_WATCHLIST='"+User.SCENARIO_ADD_STOCK_TO_WATCHLIST+"'");

										log.debug("Сценарий добавления акции активирован");
//										execute(new SendMessage().setText(update.getCallbackQuery().getData())
//												.setChatId(update.getCallbackQuery().getMessage().getChatId()));
										try {
											user.setScenario(User.SCENARIO_ADD_STOCK_TO_WATCHLIST);
										} catch (UserIncorrectScenarioException e) {
											// TODO Auto-generated catch block
											log.error("Exception", e);
										}
										simpleReply(update, "Укажите просто тикер акции либо тикер и цену отслеживания через пробел");
										user.setState(User.STATE_WAIT_FOR_TICKER);
//										scenarioAddStockToWatchList(update, user);
										return;
									}else if (update.getCallbackQuery().getData().equals(""+User.SCENARIO_ADD_FUND_TO_WATCHLIST)) {
										log.debug("User.SCENARIO_ADD_FUND_TO_WATCHLIST='"+User.SCENARIO_ADD_FUND_TO_WATCHLIST+"'");
										log.debug("Сценарий добавления фонда активирован");
										try {
											user.setScenario(User.SCENARIO_ADD_FUND_TO_WATCHLIST);
										} catch (UserIncorrectScenarioException e) {
											// TODO Auto-generated catch block
											log.error("Exception", e);
										}
										simpleReply(update, "Укажите просто тикер фонда либо тикер и цену отслеживания через пробел");
										user.setState(User.STATE_WAIT_FOR_TICKER);
//										scenarioAddStockToWatchList(update, user);
										return;
									}else if (update.getCallbackQuery().getData().equals(""+User.SCENARIO_ADD_BOND_TO_WATCHLIST)) {
											log.debug("User.SCENARIO_ADD_BOND_TO_WATCHLIST='"+User.SCENARIO_ADD_BOND_TO_WATCHLIST+"'");
											log.debug("Сценарий добавления облигации активирован");
											try {
												user.setScenario(User.SCENARIO_ADD_BOND_TO_WATCHLIST);
											} catch (UserIncorrectScenarioException e) {
												// TODO Auto-generated catch block
												log.error("Exception", e);
											}
											simpleReply(update, "Укажите просто тикер облигации либо тикер и цену отслеживания через пробел");
											user.setState(User.STATE_WAIT_FOR_TICKER);
//											scenarioAddStockToWatchList(update, user);
											return;
									}else {
										log.error("incorrect scenario exception");
									}

//								} catch (TelegramApiException e) {
//									log.error("Exception", e);;
//								}
							}else {
								
							}
						}
				} catch (TelegramApiException e) {
					log.error("Exception", e);;
				} catch (UserIncorrectStateException e) {
					log.error("Exception", e);;
				}
				return;
			case User.SCENARIO_ADD_STOCK_TO_WATCHLIST:
				log.debug("SCENARIO_ADD_STOCK_TO_WATCHLIST");
				try {
					scenarioAddStockToWatchList(update, user, ConfigMap.URL_TEXT_TINKOFF_STOCKS,  new File(App.DATA_FILE_STOCKS));
				} catch (UserIncorrectStateException | StocksTrackingException e1) {
					simpleReply(update, "Попробуйте еще раз");
					log.error("Exception", e1);;
				} 
				return;
			case User.SCENARIO_ADD_FUND_TO_WATCHLIST:
				log.debug("SCENARIO_ADD_FUND_TO_WATCHLIST");
				try {
					scenarioAddStockToWatchList(update, user, ConfigMap.URL_TEXT_TINKOFF_ETFS,  new File(App.DATA_FILE_ETFS));
				} catch (UserIncorrectStateException | StocksTrackingException e1) {
					simpleReply(update, "Попробуйте еще раз");
					log.error("Exception", e1);;
				} 
				return;
			case User.SCENARIO_ADD_BOND_TO_WATCHLIST:
				log.debug("SCENARIO_ADD_BOND_TO_WATCHLIST");
				try {
					scenarioAddStockToWatchList(update, user, ConfigMap.URL_TEXT_TINKOFF_BONDS,  new File(App.DATA_FILE_BONDS));
				} catch (UserIncorrectStateException | StocksTrackingException e1) {
					simpleReply(update, "Попробуйте еще раз");
					log.error("Exception", e1);;
				} 
				return;
			default:
				try {
					throw new InvestBotException.UserIncorrectScenarioException(user.getScenario());
				} catch (UserIncorrectScenarioException e) {
					log.error("", e);
				}
		}


	}

	private static void scenarionAddStockToPortfolio() {

	}

	public static SendMessage sendIKMenuOfAllowedOperations(long chatId) {
		// https://habr.com/ru/post/418905/
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
		InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
		InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();

		inlineKeyboardButton1.setText("Добавить акцию к отслеживанию");
		inlineKeyboardButton1.setCallbackData(""+User.SCENARIO_ADD_STOCK_TO_WATCHLIST);
		inlineKeyboardButton2.setText("Добавить фонд к отслеживанию");
		inlineKeyboardButton2.setCallbackData(""+User.SCENARIO_ADD_FUND_TO_WATCHLIST);
		inlineKeyboardButton3.setText("Добавить облигацию к отслеживанию");
		inlineKeyboardButton3.setCallbackData(""+User.SCENARIO_ADD_BOND_TO_WATCHLIST);
		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<InlineKeyboardButton>();
		List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<InlineKeyboardButton>();
		keyboardButtonsRow1.add(inlineKeyboardButton1);
		keyboardButtonsRow1.add(inlineKeyboardButton2);

//		keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Fi4a").setCallbackData("CallFi4a"));
		keyboardButtonsRow2.add(inlineKeyboardButton3);
		List<List<InlineKeyboardButton>> rowList = new ArrayList<List<InlineKeyboardButton>>();
		rowList.add(keyboardButtonsRow1);
		rowList.add(keyboardButtonsRow2);
		inlineKeyboardMarkup.setKeyboard(rowList);
		return new SendMessage().setChatId(chatId).setText("Доступные функции инвестиционного бота")
				.setReplyMarkup(inlineKeyboardMarkup);
	}

	public void botConnect() {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		try {
			telegramBotsApi.registerBot(this);
			log.info("TelegramAPI started. Look for messages");
		} catch (TelegramApiRequestException e) {
			log.error("Cant Connect. Pause " + RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.getMessage());
			try {
				Thread.sleep(RECONNECT_PAUSE);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			botConnect();
		}
		
		//
//		simpleReply(update, "<a href=\"https://www.tinkoff.ru/invest/stocks/NFLX/\">NFLX</a>: 123");

	}

}