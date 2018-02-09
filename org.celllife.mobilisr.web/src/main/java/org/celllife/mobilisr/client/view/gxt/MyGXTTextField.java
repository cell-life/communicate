package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.TextField;

public class MyGXTTextField extends TextField<String>{

	public MyGXTTextField(String fieldLabel) {
		super();
		setFieldLabel(fieldLabel);
	}
	
	public MyGXTTextField(String fieldLabel, String fieldName, boolean allowBlank, String emptyText) {
		super();
		setFieldLabel(fieldLabel);
		setName(fieldName);
		setAllowBlank(allowBlank);
		setEmptyText(emptyText);
	}
	
	
	
	public void setRegex(String regex, String errorMsg){
		setRegex(regex);
		setErrorMsg(errorMsg);
	}
	
	public void setErrorMsg(String errorMsg){
		
		TextFieldMessages messages = this.getMessages();
		messages.setRegexText(errorMsg);
	}
	
}
