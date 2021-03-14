package telegrambot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.api.objects.Update;

import ru.kvaga.invest.investbot.jobs.BackgroudJobManager;
import ru.kvaga.telegram.sendmessage.TelegramSendMessage;
import ru.kvaga.telegram.sendmessage.TelegramSendMessageException.UnsupportedParameterException;

public class App {
    private static final Logger log = Logger.getLogger(App.class);
	public static ru.kvaga.telegram.sendmessage.TelegramSendMessage telegramSendMessage=null;
	private static String TELEGRAM_TOKEN;
	private static String TELEGRAM_CHANNEL_NAME;
	private static String TELEGRAM_BOT_NAME;
	
	public static String DATA_FILE_STOCKS="";
	public static String DATA_FILE_ETFS="";
	public static String DATA_FILE_BONDS="";

    public static void main(String[] args) throws FileNotFoundException, IOException, UnsupportedParameterException {
    	if(args.length!=3) {
    		log.error("The parameters of application must be a pathes to the stocks, etfs amd bonds list files.\n"
    				+ "For instance:\n"
    				+ "#java App -data_file_stocks=data/StocksTracking.csv -data_file_etfs=data/ETFsTracking.csv -data_file_bonds=data/BondsTracking.csv\n"
    				+ "Exit.");
    		System.exit(-1);
    	}
    	DATA_FILE_STOCKS=getConsoleParameter("-data_file_stocks", args);
    	DATA_FILE_ETFS=getConsoleParameter("-data_file_etfs", args);
    	DATA_FILE_BONDS=getConsoleParameter("-data_file_bonds", args);
    	log.info("DATA_FILE_STOCKS="+DATA_FILE_STOCKS);
    	log.info("DATA_FILE_ETFS="+DATA_FILE_ETFS);
    	log.info("DATA_FILE_BONDS="+DATA_FILE_BONDS);

    	
    	log.info("\n\n\n"
    			+ "============================="
    			+ "        InvestBot Start      "
    			+ "============================="
    			+ "\n\n\n");
		String configFileName="conf/InvestBot.env";
    	getParameters(configFileName);
		telegramSendMessage = new ru.kvaga.telegram.sendmessage.TelegramSendMessage(TELEGRAM_TOKEN, TELEGRAM_CHANNEL_NAME, TelegramSendMessage.PARSE_MODE_HTML, TelegramSendMessage.WEB_PAGE_PREVIEW_DISABLE);
		
		File listOfStocksFile = new File(DATA_FILE_STOCKS);
		File listOfETFsFile = new File(DATA_FILE_ETFS);
		File listOfBondsFile = new File(DATA_FILE_BONDS);

		if(!listOfStocksFile.exists() || !listOfETFsFile.exists() || !listOfBondsFile.exists()) {
			log.error("One or more data files don't exist");
			System.exit(-1);
		}
		
    	BackgroudJobManager.init(listOfStocksFile, listOfETFsFile, listOfBondsFile);
        
    	ApiContextInitializer.init();
        Users.addUser(new User("Kvagalex"));
        InvestBot investBot = new InvestBot(TELEGRAM_BOT_NAME, TELEGRAM_TOKEN);
        investBot.botConnect();
        
//        BackgroudJobManager.destroy();

    }
    
    private static void getParameters(String filePath) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(filePath)));
		
		log.info(String.format("Reading information from the %s configuration file ... ", filePath));
		TELEGRAM_TOKEN=props.getProperty("telegram.token");
		log.debug(String.format("telegram.token=%s ", TELEGRAM_TOKEN.substring(0, 5)+"******************************8"));
		TELEGRAM_CHANNEL_NAME=props.getProperty("telegram.channel.name");
		log.debug(String.format("telegram.channel.name=%s ", TELEGRAM_CHANNEL_NAME));
		TELEGRAM_BOT_NAME=props.getProperty("telegram.bot.name");
		log.debug(String.format("telegram.bot.name=%s ", TELEGRAM_BOT_NAME));

	}
    
    private static String getConsoleParameter(String parameter, String args[]) {
    	for(String str : args) {
    		if(str.startsWith(parameter+"=")) {
    			return str.replaceAll(parameter+"=", "");
    		}
    	}
    	return null;
    }
}