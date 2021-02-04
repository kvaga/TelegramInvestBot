package ru.kvaga.investments.bonds;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
public class DataAmortizations {
	private String id;
	private MetadataAmortizations metadata;
	private RowsAmortizations rows;
	
	public DataAmortizations() {}
	
//	public DataAmortizations(String id, String value){
//		this.id=id;
//	}
    
	public MetadataAmortizations getMetadata() {
		return metadata;
	}
	public void setMetadata(MetadataAmortizations metadata) {
		this.metadata = metadata;
	}
	public RowsAmortizations getRows() {
		return rows;
	}
	public void setRows(RowsAmortizations rows) {
		this.rows = rows;
	}
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
        return id;
    }
}
