package org.celllife.mobilisr.client.view.gxt;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.google.gwt.i18n.client.DateTimeFormat;

public class MyGXTDateField extends DateField{

	public MyGXTDateField(String fieldLabel, String name, boolean allowBlank, boolean isEnabled, Date minDateVal){
		super();
		setFieldLabel(fieldLabel);
		setName(name);
		setAllowBlank(allowBlank);
		setEnabled(isEnabled);
		setMinValue(minDateVal);
		getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd-MM-yyyy"));
	}
	
}
