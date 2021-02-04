package ru.kvaga.investments.bonds;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="row")
public class RowCoupon {
	public RowCoupon() {
		
	}
	public String getIsin() {
		return isin;
	}
	
	@XmlAttribute
	public void setIsin(String isin) {
		this.isin = isin;
	}
	public String getName() {
		return name;
	}
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	public String getIssuevalue() {
		return issuevalue;
	}
	@XmlAttribute
	public void setIssuevalue(String issuevalue) {
		this.issuevalue = issuevalue;
	}
	
	public String getCoupondate() {
		return coupondate;
	}
	@XmlAttribute
	public void setCoupondate(String coupondate) {
		this.coupondate = coupondate;
	}
	public String getRecorddate() {
		return recorddate;
	}
	@XmlAttribute
	public void setRecorddate(String recorddate) {
		this.recorddate = recorddate;
	}
	public String getStartdate() {
		return startdate;
	}
	@XmlAttribute
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}
	public String getInitialfacevalue() {
		return initialfacevalue;
	}
	@XmlAttribute
	public void setInitialfacevalue(String initialfacevalue) {
		this.initialfacevalue = initialfacevalue;
	}
	public String getFacevalue() {
		return facevalue;
	}
	@XmlAttribute
	public void setFacevalue(String facevalue) {
		this.facevalue = facevalue;
	}
	public String getFaceunit() {
		return faceunit;
	}
	@XmlAttribute
	public void setFaceunit(String faceunit) {
		this.faceunit = faceunit;
	}
	public String getValue() {
		return value;
	}
	@XmlAttribute
	public void setValue(String value) {
		this.value = value;
	}
	public String getValueprc() {
		return valueprc;
	}
	@XmlAttribute
	public void setValueprc(String valueprc) {
		this.valueprc = valueprc;
	}
	public String getValue_rub() {
		return value_rub;
	}
	@XmlAttribute
	public void setValue_rub(String value_rub) {
		this.value_rub = value_rub;
	}
	private String isin;
	private String name;
	private String issuevalue;
	private String coupondate;
	private String recorddate;
	private String startdate;
	private String initialfacevalue;
	private String facevalue;
	private String faceunit;
	private String value;
	private String valueprc;
	private String value_rub;
	
}
