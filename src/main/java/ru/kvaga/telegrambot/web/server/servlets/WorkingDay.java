package ru.kvaga.telegrambot.web.server.servlets;

import java.util.Calendar;

public class WorkingDay {
	private int id;
	private boolean workingDayBol;

	public String toString() {
		return "[id: "+id+", name: "+name+", workingDayBol: "+workingDayBol+"]";
	}
	
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + id;
//	    result = prime * result + q;
	    return result;
	}

	
	public boolean equals(Object obj) {
	    return this.id == ((WorkingDay) obj).id;
	}
	
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isWorkingDayBol() {
		return workingDayBol;
	}
	public void setWorkingDayBol(boolean workingDayBol) {
		this.workingDayBol = workingDayBol;
	}
	public WorkingDay() {}
	public WorkingDay(int id, String name, boolean workingDay) {
		this.id=id;
		this.name=name;
		workingDayBol=workingDay;
	}
	
	public WorkingDay(int id, boolean workingDay) {
		this.id=id;
		initNameByDayId(id);
		workingDayBol=workingDay;
	}
	
	private void initNameByDayId(int id) {
		switch(id) {
		case Calendar.MONDAY:
			name="Monday"; break;
		case Calendar.TUESDAY:
			name="Tuesday"; break;
		case Calendar.WEDNESDAY:
			name="Wednesday"; break;
		case Calendar.THURSDAY:
			name="Thursday"; break;
		case Calendar.FRIDAY:
			name="Friday"; break;
		case Calendar.SATURDAY:
			name="Saturday"; break;
		case Calendar.SUNDAY:
			name="Sunday"; break;
	}
	}
	public WorkingDay(int id) {
		this.id=id;
		initNameByDayId(id);
		workingDayBol=true;
	}
	
}
	
