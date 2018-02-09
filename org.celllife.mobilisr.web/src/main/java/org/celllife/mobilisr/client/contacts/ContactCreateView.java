package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

import com.google.gwt.user.client.ui.Anchor;

public interface ContactCreateView extends EntityCreate<Contact> {

	Anchor getAnchor();
	
	void showAddGroupPopup();
	
	GenericContactManagementView<ContactGroup> getAddPopup();
	
	String getMsisdn();
}
