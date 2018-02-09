package org.celllife.mobilisr.client.org.view;

import org.celllife.mobilisr.client.view.gxt.FormDialog;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.Organization;

import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class OrganizationAccountDialog extends FormDialog {

	private MyGXTTextField reason;
	private MyGXTNumberField amount;
	
	public OrganizationAccountDialog() {
		buildDialog();
	}
	
	@Override
	protected void createFormContents(FormPanel formPanel) {
		reason = new MyGXTTextField("Transaction reason:", "transactionReason",
				false, "e.g. Invoice #1076");
		amount = new MyGXTNumberField("Amount (+/-):", null, false, "0", 0);
		
		amount.setPropertyEditorType(Integer.class);
		formPanel.add(amount);
		formPanel.add(reason);
	}
	
	public int getAmount(){
		return amount.getValue().intValue();
	}
	
	public String getReason(){
		return reason.getValue();
	}
	
	public void setFormObject(Organization org) {
		setHeading("Adjust account for organisation: " + org.getName());
		amount.clear();
		reason.clear();
	}
}