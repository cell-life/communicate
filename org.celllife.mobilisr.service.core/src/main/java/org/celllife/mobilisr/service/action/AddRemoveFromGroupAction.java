package org.celllife.mobilisr.service.action;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.stereotype.Component;

import com.trg.search.Search;


/**
 * This class differs from the {@link AddToGroupAction} and {@link RemoveFromGroupAction}
 * in that it will add the contact if they are not in the group and remove them if they
 * are already in the group.
 */
@Component("AddRemoveFromGroupAction")
public class AddRemoveFromGroupAction extends GroupManagementCommand implements Action {

	static final String BEAN_NAME = "AddRemoveFromGroupAction";

	@Override
	void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, ContactGroup group) {
		Boolean isNewContact = (Boolean) context.get(IS_NEW_CONTACT);
		List<ContactGroup> added = null;
		List<ContactGroup> removed = null;

		if (isNewContact){
			// if it is a new contact then don't bother searching
			added = Arrays.asList(group);
		} else {
			Search s = new Search(Contact_ContactGroup.class);
			s.addFilterEqual(Contact_ContactGroup.PROP_CONTACT, contact);
			s.addFilterEqual(Contact_ContactGroup.PROP_CONTACT_GROUP, group);
			int existingContacts = getDao().count(s);
			
			if (existingContacts > 0){
				removed = Arrays.asList(group);
			} else {
				added = Arrays.asList(group);
			}
		}
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
				contact, added, removed, false, false);
		
		contactService.addGroupsToContact(smsLog.getOrganization(), contactModel);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		return super.getConfigDescriptor(BEAN_NAME, "Add / remove sender from a group");
	}
}
