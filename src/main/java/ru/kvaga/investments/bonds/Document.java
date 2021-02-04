package ru.kvaga.investments.bonds;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Document {
	private ArrayList<DataAmortizations> data;
	
	public ArrayList<DataAmortizations> getData() {
		return data;
	}

	public void setData(ArrayList<DataAmortizations> data) {
		this.data = data;
	}
}
