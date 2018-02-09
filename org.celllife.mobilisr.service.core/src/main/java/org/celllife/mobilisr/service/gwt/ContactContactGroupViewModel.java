package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

public class ContactContactGroupViewModel extends ParentChildViewModel<Contact,ContactGroup> {

	private static final long serialVersionUID = -6750300847252905815L;

	public ContactContactGroupViewModel() {
		super();
	}

	public ContactContactGroupViewModel(Contact contact,
			List<ContactGroup> addedGroupList,
			List<ContactGroup> removedGroupList, boolean addAll,
			boolean removeAll) {
		super(contact, addedGroupList, removedGroupList, addAll, removeAll);
	}
}
