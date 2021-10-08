package ru.kvaga.investments.stocks;

import java.util.Comparator;

import ru.kvaga.investments.Instrument;

public class StockItemComparatorByTicker implements Comparator<Instrument> {

	public int compare(Instrument o1, Instrument o2) {
		if(o1 == o2)
			return 0;
		if(o1==null)
			return 1;
		if(o2==null)
			return -1;
		
		return o1.getName().compareTo(o2.getName());
	}

}
