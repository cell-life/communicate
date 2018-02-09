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
 * When a message comes into your account. We’ll push it to that URL via HTTP GET with the following variables.

    to - The number that the inbound message was sent to.
    from - The number that the inbound message was sent from.
    message - The message content.
    charset - The character set of the message (default is UTF-8, but 8-BIT and UTF-16BE Unicode are supported too)
    code - If the number the messages was sent to has a group code, it will be set here.
 *
 * See: http://www.panaceamobile.com/faq/how-can-i-forward-sms-to-my-app/
 */
@Component("PanceaMobileTransformer")
public class PanceaMobileHttpInHandler extends HttpTransformer implements ChannelHandler {

	public static final String MO_MSISDN = "from";
	public static final String MO_TO = "to";
	public static final String MO_MSG = "message";
	public static final String MO_REF = "code";

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-panceamobile", outputChannel = "incomingQueue")
	public SmsMo transformIncomingMessage(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String msisdn = getParameter(map, MO_MSISDN);
		msisdn = msisdn.replace("+", "");

		String smsContent = getParameter(map, MO_MSG);
		String destAddr = getParameter(map, MO_TO);
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
		Pconfig pconfig = new Pconfig(null,"Pancea Mobile HTTP");
		pconfig.setResource("in-panceamobile");
		return pconfig;
	}
}
