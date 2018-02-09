package org.celllife.mobilisr.client.org.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.org.AdminOrgCreateView;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextArea;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.Organization;

import com.extjs.gxt.ui.client.widget.button.Button;

public class AdminOrgCreateViewImpl extends EntityCreateTemplateImpl<Organization> implements
		AdminOrgCreateView {

	private MyGXTTextField orgName;
	private MyGXTTextArea orgAddress;
	private MyGXTTextField orgContactPerson;
	private MyGXTTextField orgContactNumber;
	private MyGXTTextField orgContactEmail;
	private MyGXTNumberField orgBalanceThreshold;
	
	@Override
	public void createView(){
		super.createView();
		
		orgName = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.orgName(),
				Organization.PROP_NAME, false, "e.g. Sherman Dental");
		
		orgAddress = new MyGXTTextArea(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.orgAddress(),
				Organization.PROP_ADDRESS, false, "e.g.\n42 Wallaby Way\nSydney\nAustralia");
		
		orgContactPerson = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.orgContactName(),
				Organization.PROP_CONTACT_PERSON, false, "e.g. P. Sherman");
		
		orgContactNumber = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.orgContactNumber(),
				Organization.PROP_CONTACT_NUMBER, false, "e.g. 27215551234");
		
		orgContactEmail = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.orgContactEmail(),
				Organization.PROP_CONTACT_EMAIL, false, "e.g. psherman@shermandental.co.au");

		orgBalanceThreshold = new MyGXTNumberField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.orgBalanceThreshold(),
				Organization.PROP_BALANCE_THRESHOLD, false, "e.g. 1000",0);

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);		
		formButtons.put(cancelButton, false);
		
		layoutCreateTemplate(Messages.INSTANCE.orgCreateHeader());

		createContents();
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}

	private void createContents() {
		orgName.setId("orgName");
		orgName.setMinLength(3);
		
		orgAddress.setId("orgAddress");
		orgAddress.setHeight(120);
		orgAddress.setMaxLength(255);
		
		orgContactPerson.setId("orgContactPerson");
		orgContactPerson.setMaxLength(70);
		
		orgContactNumber.setId("orgContactNumber");
		orgContactNumber.setValidator(ValidatorFactory.getMsisdnValidator());
		
		orgContactEmail.setId("orgContactEmail");
		orgContactEmail.setRegex(Constants.INSTANCE.emailRegex(), 
				Messages.INSTANCE.validationEmail());
		orgContactEmail.setMaxLength(255);
		
		orgBalanceThreshold.setId("orgBalanceThreshold");
		orgBalanceThreshold.setPropertyEditorType(Integer.class);
		
		formElementContainer.add(orgName);
		formElementContainer.add(orgAddress);
		formElementContainer.add(orgContactPerson);
		formElementContainer.add(orgContactNumber);
		formElementContainer.add(orgContactEmail);
		formElementContainer.add(orgBalanceThreshold);
	}

	@Override
	public void setFormObject(ViewModel<Organization> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		
		Organization org = getModel();
		
		boolean modeCreate = viewEntityModel.isModeCreate();
		if (modeCreate) {
			getHeaderLabel().setText(Messages.INSTANCE.orgCreateHeader());
		} else {
			getHeaderLabel().setText("Organisation: " + org.getName());
		} 
	}
	
}
