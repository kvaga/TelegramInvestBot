package ru.kvaga.investments.stocks;

import java.util.Comparator;

public class StockItemForPrintingComparatorByPercentFromTrackingPrice implements Comparator<StockItemForPrinting>{

	public int compare(StockItemForPrinting o1, StockItemForPrinting o2) {
		if(o1.getPercentFromTrackingPrice()<o2.getPercentFromTrackingPrice())
			return 1;
		else if(o1.getPercentFromTrackingPrice()>o2.getPercentFromTrackingPrice()) {
			return -1;
		}else {
			return 0;
		}
	}

}
