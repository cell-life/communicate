package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

public class ContactGroupContactViewModel extends ParentChildViewModel<ContactGroup, Contact> {

	private static final long serialVersionUID = -6750300847252905815L;

	public ContactGroupContactViewModel() {
		super();
	}

	public ContactGroupContactViewModel(ContactGroup contact,
			List<Contact> addedGroupList, List<Contact> removedGroupList,
			boolean addAll, boolean removeAll) {
		super(contact, addedGroupList, removedGroupList, addAll, removeAll);
	}
}
