package org.celllife.mobilisr.service.message.route;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.springframework.integration.annotation.Gateway;

/**
 * This is the gateway interface for sending messages. Calls to this method
 * are processed by Spring integration.
 * 
 * @author Simon Kelly
 */
public interface MessageService {
	
	@Gateway(requestChannel="bulkMessageChannel")
	public void sendMessage(SmsBatchConfig configuration);
	
	@Gateway(requestChannel="individualMessageChannel")
	public void sendMessage(SmsMt sms);
	
	@Gateway(requestChannel="individualMessageResponse")
	public void sendMessageResponse(SmsMt sms);
	
	@Gateway(requestChannel="deliveryChannel")
	public void deliveryReceived(DeliveryReceipt receipt);
	
	@Gateway(requestChannel="incomingQueue")
	public void messageReceived(SmsMo sms);

	@Gateway(requestChannel="messageProcessing")
	public void processMessage(SmsLog sms);
}
