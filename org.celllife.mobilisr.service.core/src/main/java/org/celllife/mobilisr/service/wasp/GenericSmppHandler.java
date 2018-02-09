package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service("GenericSmpp")
public class GenericSmppHandler extends SmppChannelHandler{

    /**
     * @param handlerName
     * @param handlerResource must match channel in outgoingChannelContext.xml
     */
    public GenericSmppHandler(String handlerName, String handlerResource) {
        super(handlerName, handlerResource);
    }

    public GenericSmppHandler() {
		/*
		 * handlerResource must match existing channels in
		 * outgoingMessageContext.xml
		 */
        super("GENERIC SMPP", "genericSmpp");
    }

    /**
     * inputChannel and outputChannel must match existing channels in
     * outgoingMessageContext.xml
     */
    @ServiceActivator(inputChannel = "genericSmpp", outputChannel = "individualMessageResponse")
    public SmsMt sendMTSms(final SmsMt smsMt) throws ChannelProcessingException {
        return super.sendMTSms(smsMt);
    }

}
