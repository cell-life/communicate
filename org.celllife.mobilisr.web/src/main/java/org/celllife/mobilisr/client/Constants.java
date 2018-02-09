package org.celllife.mobilisr.client;

import com.google.gwt.core.client.GWT;

public interface Constants extends com.google.gwt.i18n.client.Constants {

	public static final Constants INSTANCE = GWT.create(Constants.class);
	
	@DefaultIntValue(20)
	int pageSize();
	
	@DefaultStringValue("^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$")
	String emailRegex();
	
	@DefaultStringValue("^[a-zA-Z0-9\\s-_]*$")
	String alphaNumbericRegex();

	@DefaultStringValue("font_14")
	String styleFont14();
}
