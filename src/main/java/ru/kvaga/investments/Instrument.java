package ru.kvaga.investments;

import java.io.File;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.investments.etfs.Etf;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.telegrambot.web.util.ServerUtils;

public class Instrument {
	final static Logger log = LogManager.getLogger(Instrument.class);

	protected String name;
	protected double traceablePrice = Double.MAX_VALUE;
	protected double lastPrice = Double.MIN_NORMAL;
	protected Date lastUpdated = new Date();
	protected String fullName = null;
	
	public String getFullName() {
		return fullName;
	}
	public Instrument setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}
	public Instrument(String name, double traceablePrice, double lastPrice) {
		this.name=name;
		this.traceablePrice=traceablePrice;
		this.lastPrice=lastPrice;
	}
	public Instrument(String name) {
		this.name=name;
	}
	public Instrument() {
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getTraceablePrice() {
		return traceablePrice;
	}
	public void setTraceablePrice(double traceablePrice) {
		this.traceablePrice = traceablePrice;
	}
	public double getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public String toString() {
		return this.getClass().getName() + " [name="+name+", fullName="+fullName+", traceablePrice="+traceablePrice+", lastPrice="+lastPrice+", lastUpdated="+lastUpdated+"]";
	}
	public synchronized void saveXMLObjectToFile(Instrument instrument) throws JAXBException {
		 if(instrument instanceof StockItem) {
		       ServerUtils.saveXMLObjectToFile(ServerUtils.getStockFileByName(name), this);
	       }else if(instrument instanceof Bond) {
	           ServerUtils.saveXMLObjectToFile(ServerUtils.getBondFileByName(name), this);
	       }else if(instrument instanceof Etf) {
	           ServerUtils.saveXMLObjectToFile(ServerUtils.getEtfFileByName(name), this);
	       }
        log.debug(instrument + " successfully saved to a file");
	}
	public static synchronized Instrument readXMLObjectFromFile(String instrumentName, Instrument instrument) throws JAXBException {
	       Instrument item = null;
	       if(instrument instanceof StockItem) {
	    	   item = (StockItem) ServerUtils.readXMLObjectFromFile(ServerUtils.getStockFileByName(instrumentName), new StockItem());
	       }else if(instrument instanceof Bond) {
	    	   item = (Bond) ServerUtils.readXMLObjectFromFile(ServerUtils.getStockFileByName(instrumentName), new Bond());
	       }else if(instrument instanceof Etf) {
	    	   item = (Etf) ServerUtils.readXMLObjectFromFile(ServerUtils.getStockFileByName(instrumentName), new Etf());
	       }
	       if(item==null) {
	    	   return null;
	       }
	       log.debug(item + " successfully read from the [" + ServerUtils.getStockFileByName(instrumentName) + "] file");
	       return item;
	}
	public static synchronized Instrument readXMLObjectFromFile(File file, Instrument instrumentType) throws JAXBException {
//		Instrument instrument = null;
//		if(instrument instanceof StockItem) {
//			instrument = (StockItem) ServerUtils.readXMLObjectFromFile(file, new StockItem());
//	       }else if(instrument instanceof Bond) {
//	    	   instrument = (Bond) ServerUtils.readXMLObjectFromFile(file, new Bond());
//	       }else if(instrument instanceof Etf) {
//	    	   instrument = (Etf) ServerUtils.readXMLObjectFromFile(file, new Etf());
//	       }
		Instrument instrument = (Instrument) ServerUtils.readXMLObjectFromFile(file, instrumentType);

	       log.debug(instrument + " successfully read from the [" + file + "] file");
	       return instrument;
	}
	
//	public synchronized void saveXMLObjectToFile() throws JAXBException {
//		ServerUtils.saveXMLObjectToFile(ServerUtils.getBondFileByName(name), this);
//		log.debug(this + " successfully saved to the [" + ServerUtils.getBondFileByName(name) + "] file");
//	}
//
//	public static synchronized Bond readXMLObjectFromFile(String insName) throws JAXBException {
//		Bond item = (Bond) ServerUtils.readXMLObjectFromFile(ServerUtils.getBondFileByName(insName), new Bond());
//		log.debug(item + " successfully read from the [" + ServerUtils.getBondFileByName(insName) + "] file");
//		return item;
//	}
}
