package org.celllife.mobilisr.client.view.gxt;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * NB Autobinding won't work with this field.
 * Manually bind using com.extjs.gxt.ui.client.binding.TimeFieldBinding.
 * e.g.
 * {@code
 * formBindings.addFieldBinding( new TimeFieldBinding(timefield, strProperty) );}
 * @see com.extjs.gxt.ui.client.binding.TimeFieldBinding
 */
public class MyGXTTimeField extends TimeField {

	public MyGXTTimeField(String fieldLabel, String name, int incrementCounts, boolean allowBlank,
			boolean isForceSelection, boolean isEnabled){
		super();
		setFieldLabel(fieldLabel);
		setName(name);
		setIncrement(incrementCounts);
		setAllowBlank(allowBlank);
		setForceSelection(isForceSelection);
		setEnabled(isEnabled);
		setTriggerAction(TriggerAction.ALL);
		setFormat(DateTimeFormat.getFormat("HH:mm"));
	}

	public MyGXTTimeField(String fieldLabel, String name, int incrementCounts, boolean allowBlank,
			boolean isForceSelection, boolean isEnabled, boolean autoWidth){
		super();
		setFieldLabel(fieldLabel);
		setName(name);
		setIncrement(incrementCounts);
		setAllowBlank(allowBlank);
		setForceSelection(isForceSelection);
		setEnabled(isEnabled);
		setTriggerAction(TriggerAction.ALL);
		setAutoWidth(autoWidth);
		setFormat(DateTimeFormat.getFormat("HH:mm"));
	}

	public void setDateValue(Date date) {
		super.setDateValue(date);
	}
}
