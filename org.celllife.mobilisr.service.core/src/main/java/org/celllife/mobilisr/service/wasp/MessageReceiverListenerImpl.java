package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiverListenerImpl extends BaseMessageReceiverListener {
	
	@Autowired
	private MessageService messageService;
	
	@Override
	protected void incomingMessageReceived(SmsMo smsMo) {
		messageService.messageReceived(smsMo);		
	}

	@Override
	protected void deliveryReceived(DeliveryReceipt deliveryReceipt) {
		messageService.deliveryReceived(deliveryReceipt);
	}

}
