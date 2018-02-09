package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.TextArea;

public class MyGXTTextArea extends TextArea {

	public MyGXTTextArea() {
		super();
	}

	public MyGXTTextArea(String fieldLabel, String fieldName, boolean allowBlank, String emptyText){
		super();
		setFieldLabel(fieldLabel);
		setName(fieldName);
		setAllowBlank(allowBlank);
		setEmptyText(emptyText);
	}
	
	protected void onDisable() {
		getInputEl().disable();
	}

	protected void onEnable() {
		getInputEl().enable();
	}
}
