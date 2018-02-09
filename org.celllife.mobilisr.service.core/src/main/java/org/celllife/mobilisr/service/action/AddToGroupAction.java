package org.celllife.mobilisr.service.action;

import java.util.Arrays;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.stereotype.Component;


/**
 * @author Simon Kelly
 * @author Dagmar Timler
 */
@Component("AddToGroupAction")
public class AddToGroupAction extends GroupManagementCommand implements Action {

	public static final String BEAN_NAME = "AddToGroupAction";

	@Override
	void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, ContactGroup group) {
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
				contact, Arrays.asList(group), null, false, false);
		contactService.addGroupsToContact(smsLog.getOrganization(), contactModel);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		return super.getConfigDescriptor(BEAN_NAME, "Add sender to a group");
	}
}
