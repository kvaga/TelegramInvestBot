package ru.kvaga.investments.bonds;

import ru.kvaga.investments.stocks.StockItemForPrinting;

public class BondItemForPrinting extends StockItemForPrinting{
	double profitability;
	public BondItemForPrinting(String name, String fullName, double traceablePrice, double lastPrice,
			double currentPrice2, double percentFromTrackingPrice, double percentFromLastPrice, double profitability) {
		super(name, fullName, traceablePrice, lastPrice, currentPrice2, percentFromTrackingPrice, percentFromLastPrice);
		this.profitability=profitability;
	}
	public double getProfitability() {
		return profitability;
	}
	public void setProfitability(double profitability) {
		this.profitability = profitability;
	}
	
}
