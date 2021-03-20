package ru.kvaga.investments.bonds;

public class BondItemForProfitabilityPrinting {
	private String ticker;
	private String bondName;
	private double profitability;
	
	public BondItemForProfitabilityPrinting(String ticker, String bondName, double profitability) {
		this.ticker=ticker;
		this.bondName=bondName;
		this.profitability=profitability;
	}
	
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getBondName() {
		return bondName;
	}
	public void setBondName(String bondName) {
		this.bondName = bondName;
	}
	public double getProfitability() {
		return profitability;
	}
	public void setProfitability(double profitability) {
		this.profitability = profitability;
	}
	
}
