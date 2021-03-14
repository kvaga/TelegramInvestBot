package telegrambot;

public class ConfigMap {
	public static final int httpConnectionConnectTimeout=5000;
	public static final int httpConnectionReadTimeout=15000;
	public static String URL_TEXT_TINKOFF_STOCKS="https://www.tinkoff.ru/invest/stocks/%s/";
	public static String URL_TEXT_TINKOFF_ETFS="https://www.tinkoff.ru/invest/etfs/%s/";
	public static String URL_TEXT_TINKOFF_BONDS="https://www.tinkoff.ru/invest/bonds/%s/";

	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_STOCKS="<title data-meta-dynamic=\"true\">Купить акции (?<fullName>.*) \\(%s\\).*<\\/title>" ;
	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_ETFS="<title data-meta-dynamic=\"true\">.+ ETF (?<fullName>.*) \\(%s\\).*<\\/title>.*" ;
	//       <title data-meta-dynamic=\"true\">Купить ETF » (FXDE) онлайн: стоимость биржевых фондов сегодня</title>

	public static String REGEX_PATTERN_TEXT_TINKOFF_FULL_NAME_BONDS="<meta charset=\"UTF-8\">.*" + 
			"<title data-meta-dynamic=\"true\">Купить облигации (?<fullName>.*) \\(%s\\).*<\\/title>.*" + 
			"<meta property=\"og:title\"" ;

}
