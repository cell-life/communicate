package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.NumberField;

public class MyGXTNumberField extends NumberField {

	public MyGXTNumberField() {
		super();
	}

	public MyGXTNumberField(String fieldLabel, String fieldName, boolean allowBlank, String emptyText, Number defaultValue) {
		super();
		setFieldLabel(fieldLabel);
		setName(fieldName);
		setAllowBlank(allowBlank);
		setEmptyText(emptyText);
		setValue(defaultValue);
	}
}
