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
 * This class converts a raw HTTP message from AAT into an SmsMo instance.
 * 
 * From their documentation:
 *  * prem - The actual Short code
 *  * mesg - Message text
 *  * num - Senders handset number
 *  * tonum - The longcode associated with the short code that the sender SMSed
 *  * id - This is an auto incrementing ID in our SMSC applications received table
 */
@Component("AATTransformer")
public class AATHttpInHandler extends HttpTransformer implements ChannelHandler {

	public static final String MO_MSISDN = "num";
	public static final String MO_FROM = "prem";
	public static final String MO_MSG = "mesg";
	public static final String MO_REF = "id";
	public static final String MO_NETWORK = "network";

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-aat", outputChannel = "incomingQueue")
	public SmsMo transformIncomingMessage(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String msisdn = getParameter(map, MO_MSISDN);
		String smsContent = getParameter(map, MO_MSG);
		String destAddr = getParameter(map, MO_FROM);
		String ref = getParameter(map, MO_REF);

		String mobileNetwork = "Unknown";

		SmsMo smsMO = new SmsMo(msisdn, destAddr, smsContent, new Date(), 
				mobileNetwork);
		smsMO.setReference(ref);

		return smsMO;
	}
	
	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.IN.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig pconfig = new Pconfig(null,"AAT HTTP");
		pconfig.setResource("in-aat");
		return pconfig;
	}
}
