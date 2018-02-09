package org.celllife.mobilisr.service.message.processors;

import java.text.MessageFormat;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.converter.DtoConverterFactory;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

/**
 * Transformer that reserved credit for each message and creates an smslog for
 * each message.
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
@Component
public class OutgoingMessagePreProcessor {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	@Transformer(inputChannel = "messagesWithChannel", outputChannel = "preProcessedMessages")
	public SmsMt preProcess(SmsMt message) {
		
		log.debug("Pre-processing message before sending [createdFor={}] [msisdn={}]",
				message.getCreatedFor(), message.getMsisdn());
		
		SmsLog smslog = DtoConverterFactory.getInstance().fromDto(message, SmsLog.class, ApiVersion.getLatest());
		
		Organization org = new Organization();
		org.setId(message.getOrganizationId());
		smslog.setOrganization(org);
		
		Long channelId = message.getChannelId();
		if (channelId != null){
			Channel channel = generalDao.getReference(Channel.class, channelId);
			smslog.setChannel(channel);
		}
		
		if (message.getContactId() != null){
			Contact contact = generalDao.find(Contact.class, message.getContactId());
			smslog.setContact(contact);
		}
		
		if (message.getStatus() == null) {
			smslog.setStatus(SmsStatus.QUEUED_SUCCESS);
	
			if (message.getTransactionRef() == null){
				int reserveAmount = MobilisrUtility
						.calculateNumberOfMessages(message.getMessage());
				
				// ?? put transaction message in smsmt??
				String transactionMessage = MessageFormat
						.format("Message for contact ''{0}''",
								new Object[] { message.getMsisdn() });
		
				try {
					User createdBy = null;
					if (message.getUserId() != null){
						createdBy = new User();
						createdBy.setId(message.getUserId());
					}
					Long reference = userBalanceService.reserveAmount(org,
							reserveAmount, message.getCreatedFor(), createdBy,
							transactionMessage);
					message.setTransactionRef(reference);
					
				} catch (InsufficientBalanceException e) {
					message.setStatus(SmsStatus.QUEUE_FAIL);
					message.setErrorMessage(e.getMessage());
	
					/*
					 * channelName must match a channel in outgoingMessageContext.xml
					 */
					message.setChannelName("individualMessageResponse");
				}
			}
		}
		
		generalDao.save(smslog);
		message.setMessageLogId(smslog.getId());
		
		return message;
	}

}
