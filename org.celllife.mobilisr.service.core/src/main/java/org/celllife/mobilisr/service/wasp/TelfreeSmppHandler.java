package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service("TelfreeSmpp")
public class TelfreeSmppHandler extends SmppChannelHandler {

	public TelfreeSmppHandler() {
		/*
		 * handlerResource must match existing channels in
		 * outgoingMessageContext.xml 
		 */
		super("Telfree SMPP", "telfreeSmpp");
	}

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * outgoingMessageContext.xml 
	 */
	@ServiceActivator(inputChannel = "telfreeSmpp", outputChannel = "individualMessageResponse")
	public SmsMt sendMTSms(final SmsMt smsMt) throws ChannelProcessingException {
		return super.sendMTSms(smsMt);
	}
}