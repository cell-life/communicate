package org.celllife.mobilisr.service.message.processors;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * This class manages the processing of sent messages.
 * 
 * @author Simon Kelly
 */
@Component("MessagePostProcessor")
public class MessagePostProcessor {

	private static final Logger log = LoggerFactory.getLogger(MessagePostProcessor.class);
	
	@Autowired
	private SmsLogDAO smslogDao;

	@Autowired
	private ContactDAO contactDao;
	
	@ServiceActivator(inputChannel="postProcessMessages")
	public void postProcess(SmsMt message){
		log.trace("--> Processing message [{}] for [{}]", message.getBatchId(), message.getCreatedFor()) ;
		if (log.isTraceEnabled())
			log.trace(message.toString());
		
		smslogDao.updateSmsLog(message);
		
		if (message.isInvalidNumber()){
			contactDao.markContactsAsInvalid(message.getMsisdn());
		}
	}

	/*package private*/ void setSmslogDao(SmsLogDAO smslogDao) {
		this.smslogDao = smslogDao;
	}
}
