package ru.kvaga.investments.bonds;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name="rows")
@XmlAccessorType(XmlAccessType.FIELD)
public class RowsAmortizations {
	@XmlElement(name="row")
	private ArrayList<RowAmortizations> rows;

	public RowsAmortizations() {
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<RowAmortizations> getRows() {
		return rows;
	}

	public void setRows(ArrayList<RowAmortizations> rows) {
		this.rows = rows;
	}
}
