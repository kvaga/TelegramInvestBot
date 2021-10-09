package telegrambot;

import java.io.File;

public class ConfigMap {
	public static final int httpConnectionConnectTimeout=5000;
	public static final int httpConnectionReadTimeout=15000;
	public static String TEMPLATE_URL_TINKOFF_STOCKS="https://www.tinkoff.ru/invest/stocks/%s/";
	public static String TEMPLATE_URL_TINKOFF_ETFS="https://www.tinkoff.ru/invest/etfs/%s/";
	public static String TEMPLATE_URL_TINKOFF_BONDS="https://www.tinkoff.ru/invest/bonds/%s/";
	public static boolean jobsEnabledBol=false;

	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS="<title data-meta-dynamic=\"true\">Купить акции (?<fullName>.*) \\(%s\\).*<\\/title>" ;
	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS="<title data-meta-dynamic=\"true\">.+ ETF (?<fullName>.*) \\(%s\\).*<\\/title>.*" ;
	//       <title data-meta-dynamic=\"true\">������ ETF � (FXDE) ������: ��������� �������� ������ �������</title>

	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS="<meta charset=\"UTF-8\">.*" + 
			"<title data-meta-dynamic=\"true\">Купить облигации (?<fullName>.*) \\(%s\\).*<\\/title>.*" + 
			"<meta property=\"og:title\"" ;

	public static boolean TEST_MODE=System.getProperty("TEST_MODE")!=null?true:false;
	public static String adminLogin;
	public static String appHttpLink; 
	public static String adminPassword;
	public static File configFile = new File(System.getProperty("telegrambot.config.file"));
	public static File dataPath;
	public static File 	bondsPath,
						stocksPath,
						etfsPath;
	public static String TELEGRAM_TOKEN;
	public static String TELEGRAM_CHANNEL_NAME;
	public static String TELEGRAM_BOT_NAME;
}
