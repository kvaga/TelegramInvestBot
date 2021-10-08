/**
 * 
 */
package ru.kvaga.investments.etfs;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.bonds.Bond;
import ru.kvaga.investments.bonds.DataAmortizations;

/**
 * @author Xiaomi
 *
 */
@XmlRootElement
public class Etf extends Instrument{
	final static Logger log = LogManager.getLogger(Etf.class);
	private ArrayList<DataAmortizations> data;
	public Etf(String name, double traceablePrice, double lastPrice) {
		super(name, traceablePrice, lastPrice);
	}
	public Etf(String name) {
		super(name);
	}
	public Etf() {
		super();
	}
	
	public static synchronized Instrument readXMLObjectFromFile(String instrumentName) throws JAXBException {
		return readXMLObjectFromFile(instrumentName, new Etf());
	}
	
//	private String name;
//	private double traceablePrice = Double.MAX_VALUE;
//	private double lastPrice = Double.MIN_NORMAL;
//	private Date lastUpdated = new Date();
//	public ArrayList<DataAmortizations> getData() {
//		return data;
//	}
//	public void setData(ArrayList<DataAmortizations> data) {
//		this.data = data;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public double getTraceablePrice() {
//		return traceablePrice;
//	}
//	public void setTraceablePrice(double traceablePrice) {
//		this.traceablePrice = traceablePrice;
//	}
//	public double getLastPrice() {
//		return lastPrice;
//	}
//	public void setLastPrice(double lastPrice) {
//		this.lastPrice = lastPrice;
//	}
//	public Date getLastUpdated() {
//		return lastUpdated;
//	}
//	public void setLastUpdated(Date lastUpdated) {
//		this.lastUpdated = lastUpdated;
//	}
//	public synchronized void saveXMLObjectToFile() throws JAXBException {
//		ServerUtils.saveXMLObjectToFile(ServerUtils.getEtfFileByName(name), this);
//		log.debug(this + " successfully saved to the [" + ServerUtils.getEtfFileByName(name) + "] file");
//	}
//
//	public static synchronized Bond readXMLObjectFromFile(String insName) throws JAXBException {
//		Bond item = (Bond) ServerUtils.readXMLObjectFromFile(ServerUtils.getEtfFileByName(insName), new Etf());
//		log.debug(item + " successfully read from the [" + ServerUtils.getEtfFileByName(insName) + "] file");
//		return item;
//	}
//	
//	public String toString() {
//		return "ETF [name="+name+", traceablePrice="+traceablePrice+", lastPrice="+lastPrice+", lastUpdated="+lastUpdated+"]";
//	}
//	
}
