package ru.kvaga.investments.lib;

import java.util.Comparator;

import ru.kvaga.investments.Instrument;
import ru.kvaga.investments.stocks.StockItemForPrintingComparatorByPercentFromTrackingPrice;

public class InstrumentsComparatorByPercentFromTrackingPrice implements Comparator<Instrument>{

	public int compare(Instrument o1, Instrument o2) {
		if(InstrumentsTrackingLib.getPercentFromTrackingPrice(o1)<InstrumentsTrackingLib.getPercentFromTrackingPrice(o2))
			return 1;
		else if(InstrumentsTrackingLib.getPercentFromTrackingPrice(o1)>InstrumentsTrackingLib.getPercentFromTrackingPrice(o2)) {
			return -1;
		}else {
			return 0;
		}
	}
	
	
}
