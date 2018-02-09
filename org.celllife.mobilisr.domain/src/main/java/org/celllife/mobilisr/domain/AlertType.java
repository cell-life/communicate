package org.celllife.mobilisr.domain;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public enum AlertType implements BeanModelTag {

	UNEXPECTED_ERROR,
	BALANCE_RUNTIME, 
	BALANCE_PROCESSING,
	USER_EMAIL,
	SYSTEM_ALERT;
	
	public String getName(){
		return this.name();		
	}
}
