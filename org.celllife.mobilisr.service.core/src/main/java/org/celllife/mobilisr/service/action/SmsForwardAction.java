package org.celllife.mobilisr.service.action;

import java.util.Map;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dagmar Timler
 * @author Simon Kelly
 */
@Component("SmsForwardAction")
public class SmsForwardAction extends BaseAction implements Action {
	
	private static final String MSISDNS = "msisdns";
	
	public static final String TEMPLATE = "template";

	static final String BEAN_NAME = "SmsForwardAction";

	private static Logger log = LoggerFactory.getLogger(SmsForwardAction.class);
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private TemplateService templateService;

	public SmsForwardAction() {
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
		
		String msisdns = (String)context.get(MSISDNS);
		log.debug("Forwarding SMS message to ="+msisdns);
		String[] numbers = msisdns.split(",");
		
		for (String number : numbers) {
			SmsMt smsMt = new SmsMt(number.trim(), message, filter.getIdentifierString());
			smsMt.setOrganizationId(smsLog.getOrganization().getId());
			
			messageService.sendMessage(smsMt);
		}
		
		return CONTINUE_PROCESSING;
	}

	public Pconfig getConfigDescriptor() {
		Pconfig config = new Pconfig(null, "Forward SMS");
		config.setResource(BEAN_NAME);
		LabelParameter note = new LabelParameter();
		note.setValue("<p>Note that credits will be used from the organization's" +
				" account to forward the SMS received.");
		config.addParameter(note);
		StringParameter msisdn = new StringParameter(MSISDNS, "Mobile number(s):");
		msisdn.setValidator("msisdn_list");
		msisdn.setTooltip("Comma separated list of mobile phone numbers");
		config.addParameter(msisdn);
		
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