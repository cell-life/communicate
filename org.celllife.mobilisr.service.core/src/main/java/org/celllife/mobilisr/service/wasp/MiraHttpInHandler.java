package org.celllife.mobilisr.service.wasp;

import java.util.Date;
import java.util.Map;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

/**
 * This class converts a raw HTTP message from MIRA into an SmsMo instance.
 * 
 * @author Simon Kelly
 */
@Component("MiraTransformer")
public class MiraHttpInHandler extends HttpTransformer implements ChannelHandler {

	public static final String MO_MSISDN = "msisdn";
	public static final String MO_FROM = "from";
	public static final String MO_MSG = "msg";
	public static final String MO_REF = "REF";
	public static final String MO_NETWORK = "network";

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-mira", outputChannel = "incomingQueue")
	public SmsMo transformIncomingMessage(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String msisdn = getParameter(map, MO_MSISDN);
		String networkId = getParameter(map, MO_NETWORK);
		String smsContent = getParameter(map, MO_MSG);
		String destAddr = getParameter(map, MO_FROM);
		String ref = getParameter(map, MO_REF);

		String mobileNetwork = MobilisrUtility.obtainMobileNetwork(Integer
				.parseInt(networkId));

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
		Pconfig pconfig = new Pconfig(null,"Mira HTTP");
		pconfig.setResource("in-mira");
		return pconfig;
	}
}
