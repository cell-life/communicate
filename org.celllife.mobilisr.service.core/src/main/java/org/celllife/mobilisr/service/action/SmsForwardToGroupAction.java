package org.celllife.mobilisr.service.action;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

/**
 * @author Dagmar Timler
 * @author Simon Kelly
 */
@Component("SmsForwardToGroupAction")
public class SmsForwardToGroupAction extends BaseAction implements Action {
	
	private static final String GROUP_ID = "group";
	
	public static final String TEMPLATE = "template";

	static final String BEAN_NAME = "SmsForwardToGroupAction";

	private static final Integer DEFAULT_BATCH_SIZE = 50;

	private static Logger log = LoggerFactory.getLogger(SmsForwardToGroupAction.class);
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private TemplateService templateService;
	
	@Autowired
	private ContactsService contactService;
	
	@Autowired
	private SettingService settingService;

	public SmsForwardToGroupAction() {
		super();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean execute(Context context) throws Exception {
		
		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		MessageFilter filter = (MessageFilter) context.get(FILTER);
		log.debug("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());

		String template = (String) context.get(TEMPLATE);
		String message = "";
		
		Map<String, Object> map = MapBuilder.stringObject()
				.put("sender", smsLog.getMsisdn())
				.put("messageText", smsLog.getMessage())
				.put("dateReceived", smsLog.getDatetime())
				.put("receiver", filter.getChannel().getShortCode())
				.getMap();
		if (template == null || template.isEmpty()){
			message = smsLog.getMessage();
		} else {
			message = templateService.generateDynamicContent(map,template);
		}
		
		Long groupId = (Long) context.get(GROUP_ID);
		if (groupId == null){
			log.warn("No group specified in forward to group command");
			return CONTINUE_PROCESSING;
		}
		
		ContactGroup group = (ContactGroup) getDao().find(ContactGroup.class, groupId);
		if (group == null){
			log.warn("No group found with [id={}]", groupId);
			return CONTINUE_PROCESSING;
		}

		int totalNumOfContacts = contactService.countContactsForGroup(group).intValue();
		int batchNumber = 0;
		List<Contact> batchedContacts;
		
		String correlationId = UUID.randomUUID().toString();
		do {
			// execute inside loop to allow adjustment of value during sending
			Integer batchSize = settingService.getSettingValue(SettingsEnum.MESSAGE_BATCH_SIZE);
			batchSize = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;
			
			PagingLoadConfig pagingLoadConfig = new BasePagingLoadConfig(
					batchNumber*batchSize, batchSize);
			PagingLoadResult<Contact> contacts = contactService.listAllContactsForGroup(group, pagingLoadConfig);
			batchedContacts = contacts.getData();
			
			log.debug("Batch number {} with size {}",batchNumber, batchedContacts.size());	
			batchNumber++;
			
			if(!batchedContacts.isEmpty()){
				final SmsBatchConfig batchConfig = new SmsBatchConfig(
						correlationId, filter.getIdentifierString(), message,
						batchedContacts, totalNumOfContacts,
						null, null, filter
								.getOrganization().getId(), true);
				messageService.sendMessage(batchConfig);
			}
		}while(!batchedContacts.isEmpty());
		
		return CONTINUE_PROCESSING;
	}

	public Pconfig getConfigDescriptor() {
		Pconfig config = new Pconfig(null, "Forward SMS to a group");
		config.setResource(BEAN_NAME);
		LabelParameter note = new LabelParameter();
		note.setValue("<p>Note that credits will be used from the organization's" +
				" account to forward the SMS received.");
		config.addParameter(note);
		EntityParameter group = new EntityParameter(GROUP_ID, "Group:");
		group.setDisplayProperty(ContactGroup.PROP_GROUP_NAME);
		group.setValueProperty(ContactGroup.PROP_ID);
		group.setValueType(Long.class.getSimpleName());
		group.setEntityClass(ContactGroup.class.getName());
		config.addParameter(group);
		
		StringParameter template = new StringParameter(TEMPLATE, "Message template");
		template.setOptional(true);
		template.setTooltip("Template for the message.");
		template.setDisplayType("sms");
		config.addParameter(template);
		
		LabelParameter templateExplanation = new LabelParameter();
		templateExplanation.setValue("<p>A template can contain tags that will be replaced" +
				" with actual values.</p><p>Allowed values are:</p>" +
				"<ul>" +
				"<li>${sender}: the mobile number of the sender<br/></li>" +
				"<li>${receiver}: the number that this message was received on<br/></li>"+
				"<li>${text}: the message text<br/></li>"+
				"<li>${dateReceived}: the date the message was received<br/></li>" +
				"</ul>" +
				"<p>If no template is specified the exact message will be forwarded.</p>"
				);
		config.addParameter(templateExplanation);
		
		return config;
	}

	public MessageService getMessageService() {
		return messageService;
	}

	// setter for unit test
	void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
}