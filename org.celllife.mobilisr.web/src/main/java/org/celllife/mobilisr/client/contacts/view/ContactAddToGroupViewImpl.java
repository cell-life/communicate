package org.celllife.mobilisr.client.contacts.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrUIConstants;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class ContactAddToGroupViewImpl extends GenericContactManagementViewImpl<Contact> {

	public ContactAddToGroupViewImpl() {
		ContactManagementText text = new ContactManagementText();
		text.listEntityPanelHeading = MobilisrUIConstants.CONTACT_TO_GROUP_LEFTGRID_HEADING;
		text.addAllAnchorText = MobilisrUIConstants.CONTACT_ANCHOR_ADDALL_GROUP;
		text.selEntityPanelHeading = MobilisrUIConstants.CONTACT_TO_GROUP_RIGHTGRID_HEADING;
		text.removeAllAnchorText = MobilisrUIConstants.GROUP_ANCHOR_REMOVEALL_GROUP;
		text.addNewEntityFieldSetHeading = MobilisrUIConstants.CONTACT_TO_GROUP_NEW_GROUP;
		text.addNewEntityBtnText = MobilisrUIConstants.BTN_GROUP_ADDCONTACT;
		text.popupHeading = MobilisrUIConstants.CONTACTS_TO_GROUP_HEADING;
		init(text);
	}
	
	protected String getEntityName() {
		return "Contact";
	}
	
	protected ColumnConfig getGridColumnConfig() {
		return new ColumnConfig( Contact.PROP_MSISDN, "Mobile Number", 150 );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TextField<Number> getNewEntityField() {
		NumberField msisdn = new NumberField();
		msisdn.setAllowBlank(false);
		msisdn.setFieldLabel(Messages.INSTANCE.compulsory() + Messages.INSTANCE.contactMobileNumber());
		msisdn.setAllowDecimals(false);
		msisdn.setAllowNegative(false);
		msisdn.setValidator(ValidatorFactory.getMsisdnValidator());
		return msisdn;
	}
}
