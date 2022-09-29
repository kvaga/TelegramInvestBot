package ru.kvaga.telegrambot.web.server.servlets;

public class WorkingHour {
	private int hoursFrom;
	private int hoursTo;
	private int minsFrom;
	private int minsTo;
	public WorkingHour() {}
	public WorkingHour(int hoursFrom, int hoursTo, int minsFrom, int minsTo){
		this.hoursFrom=hoursFrom;
		this.hoursTo=hoursTo;
		this.minsFrom=minsFrom;
		this.minsTo=minsTo;
	}
	public int getHoursFrom() {
		return hoursFrom;
	}
	public void setHoursFrom(int hoursFrom) {
		this.hoursFrom = hoursFrom;
	}
	public int getHoursTo() {
		return hoursTo;
	}
	public void setHoursTo(int hoursTo) {
		this.hoursTo = hoursTo;
	}
	public int getMinsFrom() {
		return minsFrom;
	}
	public void setMinsFrom(int minsFrom) {
		this.minsFrom = minsFrom;
	}
	public int getMinsTo() {
		return minsTo;
	}
	public void setMinsTo(int minsTo) {
		this.minsTo = minsTo;
	}
	

}
