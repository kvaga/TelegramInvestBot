package ru.kvaga.investments.stocks;

import java.io.File;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.telegrambot.web.listeners.StartStopListener;
import ru.kvaga.telegrambot.web.util.ServerUtils;

@XmlRootElement
public class StockItem extends Instrument{
	final static Logger log = LogManager.getLogger(StockItem.class);
	
	public StockItem(String name, double traceablePrice, double lastPrice) {
		super(name, traceablePrice, lastPrice);
	}
	public StockItem(String name) {
		super(name);
	}
	public StockItem() {
		super();
	}
	
	public static synchronized Instrument readXMLObjectFromFile(String instrumentName) throws JAXBException {
		return readXMLObjectFromFile(instrumentName, new StockItem());
	}
	
	public synchronized void saveXMLObjectToFile() throws JAXBException {
		saveXMLObjectToFile(this);
	}
}
