package org.celllife.mobilisr.service.message.route;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.message.ErrorMessage;

/**
 * This error handler is the last port of call for messaging errors. It simply
 * marks the message as having failed.
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
public class GenericErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(GenericErrorHandler.class);
	
	@Autowired
	public SmsLogDAO smslogDao;
	
	@ServiceActivator(inputChannel="genericErrorChannel")
	public void handleError(ErrorMessage errorMessage){
		log.error("Messaging error",errorMessage.getPayload());
		
		Throwable payload = errorMessage.getPayload();
		if (payload instanceof MessagingException){
			MessagingException exception = (MessagingException) payload;
			Message<?> failedMessage = exception.getFailedMessage();
			Object messagePayload = failedMessage.getPayload();
			if (messagePayload instanceof SmsMt){
				SmsMt message = (SmsMt) messagePayload;
				log.error("Failed message: [{}]", message);

				message.setStatus(SmsStatus.QUEUE_FAIL);
				Throwable cause = exception.getCause();
				message.setErrorMessage(cause != null ? cause.getMessage()
						: exception.getMessage());
				
				try {
					smslogDao.updateSmsLog(message);
				} catch (Exception e) {
					log.error(LogUtil.getMarker_notifyAdmin(),
							"Unable to update smslog status", e);
				}
			}
		}
	}
}
