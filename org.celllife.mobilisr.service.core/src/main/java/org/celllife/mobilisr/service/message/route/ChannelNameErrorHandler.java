package org.celllife.mobilisr.service.message.route;

import java.util.Properties;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.message.ErrorMessage;

/**
 * This class handles messaging errors that result from messages being sent to a
 * channel that does not exist.
 * 
 * The status of the message is set to QUEUE_FAIL and the message is then sent to
 * the individual message channel where it will get processed as usual.
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
public class ChannelNameErrorHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ChannelNameErrorHandler.class);
	
	@Autowired
	private ApplicationContext context;

	@ServiceActivator(inputChannel="illegalChannel")
	public void handleError(ErrorMessage errorMessage){
		log.error("Messaging error",errorMessage.getPayload());
		
		Throwable payload = errorMessage.getPayload();
		if (payload instanceof MessagingException){
			MessagingException exception = (MessagingException) payload;
			processFailure(exception);
		}
	}

	private void processFailure(MessagingException exception) {
		Message<?> failedMessage = exception.getFailedMessage();
		
		MessageHistory history = failedMessage.getHeaders().get(MessageHistory.HEADER_NAME, MessageHistory.class);
		if (log.isTraceEnabled() && history != null) {
			for (Properties p : history) {
				String name = (String) p.get(MessageHistory.NAME_PROPERTY);
				log.trace(
						"Message history: [name={}] [type={}] [timestamp={}]",
						new Object[] { name,
								p.get(MessageHistory.TYPE_PROPERTY),
								p.get(MessageHistory.TIMESTAMP_PROPERTY) });
			}
		}
		
		Object messagePayload = failedMessage.getPayload();
		if (messagePayload instanceof SmsMt){
			SmsMt message = (SmsMt) messagePayload;
			message.setStatus(SmsStatus.QUEUE_FAIL);
			message.setErrorMessage(exception.getMessage());

			MessageChannel responseChannel = (MessageChannel) context.getBean("individualMessageResponse");
			responseChannel.send(failedMessage);
		}
	}
}
