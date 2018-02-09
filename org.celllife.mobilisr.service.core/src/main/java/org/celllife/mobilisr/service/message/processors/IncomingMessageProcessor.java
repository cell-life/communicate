package org.celllife.mobilisr.service.message.processors;

import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.SmsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * This class receives incoming messages, transforms them to SmsLogs,
 * saves them to the database with state = QUEUED_PROCESSING before
 * forwarding them to the messageProcessing channel.
 * 
 * @author Simon Kelly
 */
@Component("IncomingMessageProcessor")
public class IncomingMessageProcessor {
	
	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@ServiceActivator(inputChannel="incomingMessages", outputChannel="messageProcessing")
	public SmsLog processIncomingMessage(SmsMo message){
		
		String smsOrigin = message.getDestAddr();
		SmsLog smsLog = new SmsLog(message.getSourceAddr(), message.getMobileNetwork(),
				message.getMessage(), smsOrigin, SmsStatus.QUEUED_PROCESSING, message.getReference(),
				null, message.getDateReceived(), null, null);
		smsLog.setDir(SmsLog.SMS_DIR_IN);
		
		smsLogDAO.save(smsLog);
		
		return smsLog;
	}
}
