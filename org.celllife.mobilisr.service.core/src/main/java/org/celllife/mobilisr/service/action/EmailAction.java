package org.celllife.mobilisr.service.action;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.constants.Templates;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 
 * @author Simon Kelly
 */
@Component("EmailAction")
public class EmailAction implements Action {

	public static final String BEAN_NAME = "EmailAction";

	private static Logger log = LoggerFactory.getLogger(EmailAction.class);

	/**
	 * A comma delimited list of email addresses
	 */
	public static final String MAIL_TO = "mailto";

	public static final String MAIL_TEMPLATE = "template";
	
	@Autowired
	private MailService mailService;

	@Autowired
	private TemplateService templateService;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean execute(Context context) {

		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		MessageFilter filter = (MessageFilter) context.get(FILTER);
		log.debug("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());

		String mailTo = (String) context.get(MAIL_TO);
		if (mailTo == null || mailTo.isEmpty()){
			log.warn("Empty email address in email action");
			return CONTINUE_PROCESSING;
		}
		
		String template = (String) context.get(MAIL_TEMPLATE);
		String message = "";
		
		Map<String, Object> map = MapBuilder.stringObject()
				.put("sender", smsLog.getMsisdn())
				.put("messageText", smsLog.getMessage())
				.put("dateReceived", smsLog.getDatetime())
				.put("receiver", filter.getChannel().getShortCode())
				.getMap();
		if (template == null || template.isEmpty()){
			message = templateService.generateContent(map,Templates.INCOMING_MESSAGE);
		} else {
			message = templateService.generateDynamicContent(map,template);
		}
		
		String subject = MessageFormat.format("Incoming message from {0}", smsLog.getMsisdn());
		mailService.enqueueMail(mailTo, subject, message);
		
		return CONTINUE_PROCESSING;
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig config = new Pconfig(null, "Send email");
		config.setResource(BEAN_NAME);
		StringParameter mailto = new StringParameter(MAIL_TO,"Email to:");
		mailto.setTooltip("Comma separated list of email addresses");
		mailto.setRegex("([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}(,\\s*([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4})*");
		mailto.setErrorMessage("Invalid email addresses");
		config.addParameter(mailto);
		
		StringParameter template = new StringParameter(MAIL_TEMPLATE, "Email template");
		template.setOptional(true);
		template.setTooltip("Template for the email message.");
		template.setDisplayType("large");
		config.addParameter(template);
		
		LabelParameter templateExplanation = new LabelParameter();
		templateExplanation.setValue("<p>A template can contain tags that will be replaced" +
				" with actual values.</p><p>Allowed values are:</p>" +
				"<ul>" +
				"<li>${sender}: the mobile number of the sender<br/></li>" +
				"<li>${receiver}: the number that this message was received on<br/></li>"+
				"<li>${messageText}: the message text<br/></li>"+
				"<li>${dateReceived}: the date the message was received<br/></li>" +
				"</ul>"
				);
		config.addParameter(templateExplanation);
		return config;
	}

	/*package private*/ void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	/*package private*/ void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}
}
