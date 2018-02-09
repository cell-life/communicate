package org.celllife.mobilisr.service.message.route;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer that simulates message sending by setting message status to sent
 *
 * @author Simon Kelly
 */
public class MockOutChannel {

	private static final Logger log = LoggerFactory.getLogger(MockOutChannel.class);

	public SmsMt transform(SmsMt message) {
		log.debug("Mock sending message {}", message.getMsisdn());
		message.setStatus(SmsStatus.WASP_SUCCESS);
		message.setSendingAttempts(2);
		message.setMessageTrackingNumber(String.valueOf(System
				.currentTimeMillis()));
		return message;
	}
}
