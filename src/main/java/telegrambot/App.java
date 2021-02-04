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
	
	public static String DATA_FILE="";

    public static void main(String[] args) throws FileNotFoundException, IOException, UnsupportedParameterException {
    	if(args.length!=1) {
    		log.error("The first parameter of application must be a path to the stocks list file.\n"
    				+ "For instance:\n"
    				+ "#java App data/StocksTracking.csv\n"
    				+ "Exit.");
    		System.exit(-1);
    	}
    	DATA_FILE=args[0];
    	log.info("\n\n\n"
    			+ "============================="
    			+ "        InvestBot Start      "
    			+ "============================="
    			+ "\n\n\n");
		String configFileName="conf/InvestBot.env";
    	getParameters(configFileName);
		telegramSendMessage = new ru.kvaga.telegram.sendmessage.TelegramSendMessage(TELEGRAM_TOKEN, TELEGRAM_CHANNEL_NAME, TelegramSendMessage.PARSE_MODE_HTML, TelegramSendMessage.WEB_PAGE_PREVIEW_DISABLE);
		File listOfStocksFile = new File(DATA_FILE);
    	
    	BackgroudJobManager.init(listOfStocksFile);
        
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
}