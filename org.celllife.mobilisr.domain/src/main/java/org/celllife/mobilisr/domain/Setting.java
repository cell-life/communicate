package org.celllife.mobilisr.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "setting")
public class Setting extends AbstractBaseEntity {

	private static final long serialVersionUID = 7042325334184245676L;

	public static final String PROP_NAME = "name";
	public static final String PROP_VALUE_STRING = "valueString";
	
	@Column(nullable = false, length=35)
	private String name;

	private String valueString;
	
	public Setting() {
	}

	public Setting(String name, String valueString) {
		super();
		this.name = name;
		this.valueString = valueString;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValueString(){
		return valueString;
	}
	
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
	
}
