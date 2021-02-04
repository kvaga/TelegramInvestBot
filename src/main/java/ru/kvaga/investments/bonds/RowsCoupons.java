package ru.kvaga.investments.bonds;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="rows")
@XmlAccessorType(XmlAccessType.FIELD)
public class RowsCoupons {
		@XmlElement(name="row")
		private ArrayList<RowCoupon> rows;

		public RowsCoupons() {
			
		}
		public ArrayList<RowCoupon> getRows() {
			return rows;
		}

		public void setRows(ArrayList<RowCoupon> rows) {
			this.rows = rows;
		}
}
