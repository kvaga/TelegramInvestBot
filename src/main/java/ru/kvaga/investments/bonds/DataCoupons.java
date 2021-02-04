package ru.kvaga.investments.bonds;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DataCoupons {
		private String id;
		private RowsCoupons rows;
		public String getId() {
			return id;
		}
		@XmlAttribute
		public void setId(String id) {
			this.id = id;
		}
		public RowsCoupons getRows() {
			return rows;
		}
		public void setRows(RowsCoupons rows) {
			this.rows = rows;
		}

}
