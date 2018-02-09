package org.celllife.mobilisr.client.contacts.view;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Anchor;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.MyGXTCheckBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.Contact;

public class ContactForm {
	
	private TextField<String> msisdn;
	private TextField<String> firstName;
	private TextField<String> lastName;
	private TextField<String> mobileNetwork;

    private CheckBox valid;

	private Anchor anchor;
	private AdapterField anchorField;
	
	public ContactForm(LayoutContainer panel) {
		msisdn = new MyGXTTextField(Messages.INSTANCE.compulsory()
				+ Messages.INSTANCE.contactMobileNumber(), Contact.PROP_MSISDN,
				false, "e.g. 27821234567");
		msisdn.setValidator(ValidatorFactory.getMsisdnValidator());
		firstName = new MyGXTTextField(Messages.INSTANCE.contactFirstName(), Contact.PROP_FIRST_NAME, true, "e.g. Thandi");
		lastName = new MyGXTTextField(Messages.INSTANCE.contactLastName(), Contact.PROP_LAST_NAME, true, "e.g. Mandela");
		mobileNetwork = new MyGXTTextField(Messages.INSTANCE.contactMobileNetwork(), Contact.PROP_MOBILE_NETWORK, true, "Unknown");
		mobileNetwork.setVisible(false);
        valid = new MyGXTCheckBox(Messages.INSTANCE.invalid(),Contact.PROP_INVALID);
		
		anchor = new Anchor(Messages.INSTANCE.contactManageGroups());
		anchorField = new AdapterField(anchor);
		
		panel.add(msisdn);
		panel.add(firstName);
		panel.add(lastName);
		panel.add(mobileNetwork);
        panel.add(valid);
		panel.add(anchorField);
	}
	
	public void showAnchor(boolean show){
		anchorField.setVisible(show);
	}
	
	public Anchor getAnchor() {
		return anchor;
	}
	
	public TextField<String> getFirstNameField() {
		return firstName;
	}
	
	public TextField<String> getLastNameField() {
		return lastName;
	}
	
	public TextField<String> getMobileNetworkField() {
		return mobileNetwork;
	}
	
	public TextField<String> getMsisdnField() {
		return msisdn;
	}
	
	public void enableFields(boolean enabled){
		firstName.setEnabled(enabled);
		lastName.setEnabled(enabled);
		mobileNetwork.setEnabled(enabled);
		msisdn.setEnabled(enabled);
        valid.setEnabled(enabled);
		anchor.setEnabled(enabled);
	}
}
