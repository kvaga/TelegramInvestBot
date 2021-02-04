package ru.kvaga.investments.bonds;

import ru.kvaga.investments.stocks.StocksTrackingException.GetContentOFSiteException;
import ru.kvaga.investments.stocks.StocksTrackingException.GetCurrentPriceOfStockException.Common;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ru.kvaga.investments.stocks.StocksTrackingLib;

public class BondsTrackingLib {
	private static String testString=""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<document>" + 
				"<data id=\"amortizations\">" + 
					"<metadata>" + 
						"<columns>" + 
							"<column name=\"isin\" type=\"string\" bytes=\"765\" max_size=\"0\"/>" + 
						"</columns>" + 
					"</metadata>" + 
					"<rows>" + 
					"<row isin=\"RU000A0JXQ93\" name=\"ÃÊ ÏÈÊ (ÏÀÎ) ÁÎ-Ï02\" issuevalue=\"10000000000\" amortdate=\"2019-07-26\" facevalue=\"440\" initialfacevalue=\"1000\" faceunit=\"RUB\" valueprc=\"8.00\" value=\"80\" value_rub=\"80\" data_source=\"amortization\"/>" + 
					"<row isin=\"RU000A0JXQ93\" name=\"ÃÊ ÏÈÊ (ÏÀÎ) ÁÎ-Ï02\" issuevalue=\"10000000000\" amortdate=\"2019-07-26\" facevalue=\"440\" initialfacevalue=\"1000\" faceunit=\"RUB\" valueprc=\"8.00\" value=\"80\" value_rub=\"80\" data_source=\"amortization\"/>" + 
					"</rows>" + 
				"</data>"
				+ "<data id=\"coupons\">"+
				"<metadata>" + 
				"<columns>" + 
					"<column name=\"isin\" type=\"string\" bytes=\"765\" max_size=\"0\"/>" + 
				"</columns>" + 
			"</metadata>" + 
				"<rows>"+
						"<row isin=\"RU000A0JXQ93\" name=\"ÃÊ ÏÈÊ (ÏÀÎ) ÁÎ-Ï02\" issuevalue=\"10000000000\" coupondate=\"2017-07-28\" recorddate=\"\" startdate=\"2017-04-28\" initialfacevalue=\"1000\" facevalue=\"440\" faceunit=\"RUB\" value=\"28.05\" valueprc=\"11.25\" value_rub=\"28.05\"/>" + 
						"<row isin=\"RU000A0JXQ93\" name=\"ÃÊ ÏÈÊ (ÏÀÎ) ÁÎ-Ï02\" issuevalue=\"10000000000\" coupondate=\"2017-10-27\" recorddate=\"2017-10-26\" startdate=\"2017-07-28\" initialfacevalue=\"1000\" facevalue=\"440\" faceunit=\"RUB\" value=\"28.05\" valueprc=\"11.25\" value_rub=\"28.05\"/>"+
				 "</rows>"
				 + "</data>" + 
			"</document>";
	public static String MOEX_BONDIZATION_URL_PATTERN = "https://iss.moex.com/iss/securities/%s/bondization.xml";

	public static void main(String args[]) throws Common, GetContentOFSiteException, JAXBException {
		printXMLObject(getBondsProfitabilty("RU000A0JXQ93"));
	}

	public static Document getBondsProfitabilty(String bondIsin) throws Common, GetContentOFSiteException, JAXBException {
		String urlText=String.format(MOEX_BONDIZATION_URL_PATTERN, bondIsin);
//		String response = StocksTrackingLib.getContentOfSite(bondIsin, urlText);
		return getBondObjectFromString(testString);
		
	}
	
	// Read XML object from file, then print this object
    public static Document getBondObjectFromString(String xmlString) throws JAXBException {
    	JAXBContext jaxbContext;
		File feedXml = new File(xmlString);
	    jaxbContext = JAXBContext.newInstance(Document.class);              
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    Document rss = (Document) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
	    return rss;
	}
    
    public static void printXMLObject(Object object) throws JAXBException {
    	// For printing
	    StringWriter writer = new StringWriter();
	    JAXBContext jc = JAXBContext.newInstance(object.getClass());
	    Marshaller marshaller = jc.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(object, writer);
	    System.out.println(writer.toString());
    }
}