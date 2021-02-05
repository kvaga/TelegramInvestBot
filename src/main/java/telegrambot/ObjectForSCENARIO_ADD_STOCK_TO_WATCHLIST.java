package telegrambot;

public class ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST {
	private String stockTiker;
	private String stockName;
	private float watchPrice=Float.MIN_VALUE;
	public String getStockTiker() {
		return stockTiker;
	}
	public void setStockTiker(String stockTiker) {
		this.stockTiker = stockTiker;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public float getWatchPrice() {
		return watchPrice;
	}
	public void setWatchPrice(float watchPrice) {
		this.watchPrice = watchPrice;
	}
	
}
