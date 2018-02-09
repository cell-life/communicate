package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class MyGXTFormPanel extends FormPanel {

	public MyGXTFormPanel(){
		super();
	}
	
	public MyGXTFormPanel(String headingText, boolean setAsFrame){
		super();
		setHeading(headingText);
		setFrame(setAsFrame);
	}
}
