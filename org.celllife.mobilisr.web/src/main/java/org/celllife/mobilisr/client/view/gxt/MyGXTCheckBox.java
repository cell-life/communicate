package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.CheckBox;

public class MyGXTCheckBox extends CheckBox {

	public MyGXTCheckBox(String fieldLabel) {
		super();
		setFieldLabel(fieldLabel);
	}

	public MyGXTCheckBox(String fieldLabel, String fieldName) {
		super();
		setFieldLabel(fieldLabel);
		setName(fieldName);
	}
	
}
