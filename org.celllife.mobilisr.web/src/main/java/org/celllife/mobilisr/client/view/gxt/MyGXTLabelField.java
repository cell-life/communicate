package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.LabelField;

public class MyGXTLabelField extends LabelField {
	
	public MyGXTLabelField(String text){
		this.setText(text);
	}
	public MyGXTLabelField(String text, String label){
		this.setText(text);
		this.setFieldLabel(label);
	}

}
