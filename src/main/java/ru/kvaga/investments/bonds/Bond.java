package ru.kvaga.investments.bonds;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.telegrambot.web.listeners.StartStopListener;
import ru.kvaga.telegrambot.web.util.ServerUtils;

@XmlRootElement
public class Bond extends Instrument{
	final static Logger log = LogManager.getLogger(Bond.class);
	
	public Bond(String name, double traceablePrice, double lastPrice) {
		super(name, traceablePrice, lastPrice);
	}
	public Bond(String name) {
		super(name);
	}
	public Bond() {
		super();
	}
	
	private ArrayList<DataAmortizations> data;
	public ArrayList<DataAmortizations> getData() {
		return data;
	}
	
	public void setData(ArrayList<DataAmortizations> data) {
		this.data = data;
	}
	
	public static synchronized Instrument readXMLObjectFromFile(String instrumentName) throws JAXBException {
		return readXMLObjectFromFile(instrumentName, new Bond());
	}
//	public String toString() {
//		return "Bond [name="+name+", traceablePrice="+traceablePrice+", lastPrice="+lastPrice+", lastUpdated="+lastUpdated+"]";
//	}
}
