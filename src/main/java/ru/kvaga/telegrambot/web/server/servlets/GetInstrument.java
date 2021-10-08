package ru.kvaga.telegrambot.web.server.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.Parameter;

import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.investments.etfs.Etf;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.telegrambot.web.listeners.StartStopListener;
import ru.kvaga.telegrambot.web.util.ServerUtils;
import telegrambot.ConfigMap;

/**
 * Servlet implementation class GetInstrument
 */
@WebServlet("/GetInstrument")
public class GetInstrument extends HttpServlet {
	 final static Logger log = LogManager.getLogger(GetInstrument.class);
	private static final long serialVersionUID = 1L;
	private static enum instruments{
		stock,
		bond,
		etf
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetInstrument() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @param instrumentName
	 * @param instrumentType
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String instrumentType = request.getParameter("instrumentType");
		String instrumentName = request.getParameter("instrumentName");
		log.debug("Got parameters instrumentName ["+instrumentName+"], instrumentType ["+instrumentType+"]");
		StockItem stock = new StockItem();
		StockItem stock2;
		stock.setName("T");
		if(!ServerUtils.parameterExists("instrumentType", request, response)) return;
		if(!ServerUtils.parameterExists("instrumentName", request, response)) return;

		try{
			instruments.valueOf(instrumentType);
		} catch(IllegalArgumentException e) {
			log.debug("Unsupported instrumentType ["+instrumentType+"]");
			response.getWriter().write(ServerUtils.getHTMLFailText("Unsupported instrumentType ["+instrumentType+"]"));
			return;
		}
		Object instrument = getInstrument(instrumentName, instrumentType);
		if(instrument==null) {
			log.debug("Instrument ["+instrumentName+"] with type ["+instrumentType+"] not found");
			response.getWriter().write(ServerUtils.getHTMLFailText("Instrument ["+instrumentName+"] with type ["+instrumentType+"] not found"));
			return;
		}else {
			log.debug("Returned instrument ["+instrument.toString()+"]" );
			response.getWriter().write(instrument.toString());
		}
		return;
	}
	
	public Object getInstrument(String insName, String instrumentType) {
		try {
			if(instrumentType.equals("stock"))
				return StockItem.readXMLObjectFromFile(insName);
			else if(instrumentType.equals("bond"))
				return Bond.readXMLObjectFromFile(insName);
			else if(instrumentType.equals("etf"))
				return Etf.readXMLObjectFromFile(insName);
		} catch (JAXBException e) {
			log.error("Exception on getting instrument ["+insName+"] andd type ["+instrumentType+"]", e);
		}
		return null;
	}
	
	
	// Read XML object from file, then print this object
//		public static RSS getRSSObjectFromXMLFile(File xmlFile) throws JAXBException {
//	    	long t1 = new Date().getTime();
//	    	JAXBContext jaxbContext;
//		    jaxbContext = JAXBContext.newInstance(RSS.class);              
//		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//		    RSS rss = (RSS) jaxbUnmarshaller.unmarshal(xmlFile);
//		    MonitoringUtils.sendResponseTime2InfluxDB(new Object() {}, new Date().getTime() - t1);
//		    return rss;
//		}

}
