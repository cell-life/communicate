package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service("IntegratSmpp")
public class IntegratSmppHandler extends SmppChannelHandler {

	public IntegratSmppHandler() {
		/*
		 * handlerResource must match existing channels in
		 * outgoingMessageContext.xml 
		 */
		super("Integrat SMPP", "integratSmpp");
	}

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * outgoingMessageContext.xml 
	 */
	@ServiceActivator(inputChannel = "integratSmpp", outputChannel = "individualMessageResponse")
	public SmsMt sendMTSms(final SmsMt smsMt) throws ChannelProcessingException {
		return super.sendMTSms(smsMt);
	}
}