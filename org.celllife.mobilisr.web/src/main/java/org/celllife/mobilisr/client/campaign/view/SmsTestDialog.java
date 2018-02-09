package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.FormDialog;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

public class SmsTestDialog extends FormDialog {

	private Radio radioMyNumber = new Radio();
	private Radio radioCustomNumber = new Radio();
	private MyGXTTextField msisdnField = new MyGXTTextField("", "msisdnField", false, "");
	
	private String mymsisdn = "";
	
	public SmsTestDialog() {
		buildDialog();
	}
	
	@Override
	protected void createFormContents(FormPanel formPanel) {
		setResizable(false);
		setHeading("Send Test Message");
		
		getSaveButton().setText("Send Now");
		
		// Setup widgets (radios, buttons, etc.)
		radioMyNumber = new Radio();
		radioMyNumber.setName("whichNumber");
		radioMyNumber.setValue(true);
		radioMyNumber.setId("radioMyNumber");
		
		radioCustomNumber = new Radio();
		radioCustomNumber.setName("whichNumber");
		radioCustomNumber.setBoxLabel("Use custom number:");
		radioCustomNumber.setId("radioCustomNumber");
		
		msisdnField = new MyGXTTextField("", "msisdnField", false, "");
		msisdnField.setEnabled(false);
		msisdnField.setValidator(ValidatorFactory.getMsisdnValidator());
		msisdnField.setId("msisdnField");
		
		addListeners();
		
		RadioGroup radioGroup = new RadioGroup();
		radioGroup.setOrientation(Orientation.VERTICAL);
		radioGroup.add(radioMyNumber);
		radioGroup.add(radioCustomNumber);
		
		formPanel.add(radioGroup);
		formPanel.add(msisdnField);
	}
	
	private void addListeners() {
		radioMyNumber.addListener(Events.Focus, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				enableCustomMsisdnField(false);
			}
		});
		
		radioCustomNumber.addListener(Events.Focus, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				enableCustomMsisdnField(true);
			}
		});
	}
	
	public void enableCustomMsisdnField(boolean enabled) {
		if (enabled) {
			msisdnField.setEnabled(true);
			msisdnField.reset();
		} else {
			msisdnField.setEnabled(false);
			msisdnField.reset();
		}
	}
	
	public String getSelectedMsisdn(){
		if (msisdnField.isEnabled()){
			return msisdnField.getValue();
		}
		return mymsisdn;
	}
	
	public void setMyMsisdn(String mymsisdn){
		this.mymsisdn = mymsisdn;
		radioMyNumber.setBoxLabel("me (" + mymsisdn + ")");
	}

}
