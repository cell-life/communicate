package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Kelly
 * @author Dagmar Timler
 */
public abstract class GroupManagementCommand extends GetContactCommand {

	public static final String GROUP_ID = "group";

	@Autowired
	protected ContactsService contactService;
	
	protected static Logger log = LoggerFactory.getLogger(AddToGroupAction.class);


	public GroupManagementCommand() {
		super();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean execute(Context context) throws Exception {
		super.execute(context);
		
		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		log.debug("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());
		
		Contact contact = (Contact) context.get(CONTACT);

		Long groupId = (Long) context.get(GROUP_ID);
		if (groupId == null){
			log.warn("No group specified in group management command");
			return CONTINUE_PROCESSING;
		}
		
		ContactGroup group = (ContactGroup) getDao().find(ContactGroup.class, groupId);
		if (group != null){
			executeManagementCommand(context, smsLog, contact, group);
		} else {
			log.warn("No group found with [id={}]", groupId);
		}
		
		return CONTINUE_PROCESSING;
	}
	
	abstract void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, ContactGroup group); 

	protected Pconfig getConfigDescriptor(String beanName, String beanDescription) {
		Pconfig config = new Pconfig(null, beanDescription);
		config.setResource(beanName);
		EntityParameter group = new EntityParameter(GROUP_ID, "Group:");
		group.setDisplayProperty(ContactGroup.PROP_GROUP_NAME);
		group.setValueProperty(ContactGroup.PROP_ID);
		group.setValueType(Long.class.getSimpleName());
		group.setEntityClass(ContactGroup.class.getName());
		config.addParameter(group);
		
		return config;
	}

	protected void setContactService(ContactsService contactService) {
		this.contactService = contactService;
	}

}