package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dagmar Timler
 */
@Component("SmsAutoReplyAction")
public class SmsAutoReplyAction extends BaseAction implements Action {
	
	static final String BEAN_NAME = "SmsAutoReplyAction";

	private static Logger log = LoggerFactory.getLogger(SmsAutoReplyAction.class);
	
	@Autowired
	private MessageService messageService;
	
	public SmsAutoReplyAction() {
		super();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean execute(Context context) throws Exception {
		
		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		MessageFilter filter = (MessageFilter) context.get(FILTER);
		log.debug("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());

		String message = (String)context.get("message");
		log.debug("Sending auto reply message="+message);
		
		SmsMt smsMt = new SmsMt(smsLog.getMsisdn(), message, filter.getIdentifierString());
		smsMt.setOrganizationId(smsLog.getOrganization().getId());
		
		messageService.sendMessage(smsMt);

		
		return CONTINUE_PROCESSING;
	}

	public Pconfig getConfigDescriptor() {
		Pconfig config = new Pconfig(null, "Auto reply SMS");
		config.setResource(BEAN_NAME);
		LabelParameter templateExplanation = new LabelParameter();
		templateExplanation.setValue("<p>Note that credits will be used from the organization's" +
				" account when sending a reply to the sender.");
		config.addParameter(templateExplanation);
		
		StringParameter message = new StringParameter("message", "Message Response:");
		message.setDisplayType("sms");
		config.addParameter(message);

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