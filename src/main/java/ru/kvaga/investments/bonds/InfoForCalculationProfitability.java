package ru.kvaga.investments.bonds;

import java.util.Date;

public class InfoForCalculationProfitability {
	float currentProfitability=Float.MIN_VALUE;
	float profitabilityToEnd=Float.MIN_VALUE;
	Date dateOfEnd=null;
	Date dateOfNextCoupon=null;
	float nkd;
	float coupon=Float.MIN_VALUE;
	float nominal=Float.MIN_VALUE;
	int period=Integer.MIN_VALUE;
	boolean amortization=true;
}
