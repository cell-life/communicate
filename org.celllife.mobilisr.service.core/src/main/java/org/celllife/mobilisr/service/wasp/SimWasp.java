package org.celllife.mobilisr.service.wasp;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Wasp simulator. Should be used only for testing where an external WASP is 
 * not available.
 *
 * @author Simon Kelly
 */
@Component("simWasp")
public class SimWasp {

	private static final Logger log = LoggerFactory.getLogger(SimWasp.class);
	
	@Autowired
	private MessageService messageService;
	
	@Autowired(required=false)
	@Qualifier("messageInTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@ServiceActivator(inputChannel = "out-simulator", outputChannel = "individualMessageResponse")
	public SmsMt sendMTSms(SmsMt message) {
		log.trace("SimWasp sending message to {}", message.getMsisdn());

		message.setStatus(SmsStatus.WASP_SUCCESS);
		message.setSendingAttempts(1);
		String trackingNumber = RandomStringUtils.randomNumeric(10);
		message.setMessageTrackingNumber(trackingNumber);
		
		taskExecutor.execute(new DeliveryThread(trackingNumber, message.getMsisdn()));
		
		return message;
	}
	
	private class DeliveryThread implements Runnable {

		private final String id;
		private final String msisdn;

		public DeliveryThread(String id, String msisdn) {
			this.id = id;
			this.msisdn = msisdn;
		}
		
		@Override
		public void run() {
			try {Thread.sleep(1000);} catch (Exception ignore){}
			
			DeliveryReceipt receipt = new DeliveryReceipt(id, new Date(), DeliveryReceiptState.DELIVRD, "");
			receipt.setSourceAddr(msisdn);
			messageService.deliveryReceived(receipt);
		}
		
	}
}
