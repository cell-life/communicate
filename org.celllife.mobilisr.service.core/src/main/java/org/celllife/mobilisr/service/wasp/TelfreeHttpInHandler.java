package org.celllife.mobilisr.service.wasp;

import java.util.Date;
import java.util.Map;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

/**
 * Responsible for handling incoming message on the in-telfree channel
 */
@Component("TelfreeTransformer")
public class TelfreeHttpInHandler extends HttpTransformer implements ChannelHandler {

	public static final String MO_TO = "to";
	public static final String MO_FROM = "from";
	public static final String MO_MESSAGE = "message";

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-telfree", outputChannel = "incomingQueue")
	public SmsMo transformIncomingMessage(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String destAddr = getParameter(map, MO_TO);
		String msisdn = getParameter(map, MO_FROM);
		String smsContent = getParameter(map, MO_MESSAGE);
		
		SmsMo smsMO = new SmsMo(msisdn, destAddr, smsContent, new Date(), null);
		
		return smsMO;
	}
	
	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.IN.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig pconfig = new Pconfig(null,"Telfree HTTP");
		pconfig.setResource("in-telfree");
		return pconfig;
	}
}
