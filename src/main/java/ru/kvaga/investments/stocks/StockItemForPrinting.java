package ru.kvaga.investments.stocks;

import ru.kvaga.telegram.sendmessage.TelegramSendMessage;

public class StockItemForPrinting {
	private String name;
	private String fullName;
	private double traceablePrice;
	private double lastPrice;
	private double currentPrice;
	private double percentFromTrackingPrice;
	private double percentFromLastPrice;
	public StockItemForPrinting(
			String name, 
			String fullName, 
			double traceablePrice, 
			double lastPrice, 
			double currentPrice2,
			double percentFromTrackingPrice,
			double percentFromLastPrice
			) {
		this.name=name;
		this.fullName=fullName;
		this.traceablePrice=traceablePrice;
		this.lastPrice=lastPrice;
		this.currentPrice=currentPrice2;
		this.percentFromTrackingPrice=percentFromTrackingPrice;
		this.percentFromLastPrice=percentFromLastPrice;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public double getTraceablePrice() {
		return traceablePrice;
	}
	public void setTraceablePrice(float traceablePrice) {
		this.traceablePrice = traceablePrice;
	}
	public double getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(float lastPrice) {
		this.lastPrice = lastPrice;
	}
	public double getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(float currentPrice) {
		this.currentPrice = currentPrice;
	}
	public double getPercentFromTrackingPrice() {
		return percentFromTrackingPrice;
	}
	public void setPercentFromTrackingPrice(float percentFromTrackingPrice) {
		this.percentFromTrackingPrice = percentFromTrackingPrice;
	}
	public double getPercentFromLastPrice() {
		return percentFromLastPrice;
	}
	public void setPercentFromLastPrice(float percentFromLastPrice) {
		this.percentFromLastPrice = percentFromLastPrice;
	}
	
}

