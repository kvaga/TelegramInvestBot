package ru.kvaga.investments.bonds;

import java.util.Comparator;

import ru.kvaga.investments.stocks.StockItemForPrinting;

public class BondItemForProfitabilityPrintingComparatorByProfitability implements Comparator<BondItemForProfitabilityPrinting>{

	public int compare(BondItemForProfitabilityPrinting o1, BondItemForProfitabilityPrinting o2) {
		if(o1.getProfitability()<o2.getProfitability())
			return 1;
		else if(o1.getProfitability()>o2.getProfitability()) {
			return -1;
		}else {
			return 0;
		}
	}

}
