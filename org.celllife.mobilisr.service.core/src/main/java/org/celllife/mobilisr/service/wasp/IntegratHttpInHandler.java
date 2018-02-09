package org.celllife.mobilisr.service.wasp;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.service.utility.HexUtil;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

/**
 * @author Simon Kelly
 */
@Component("IntegratTransformer")
public class IntegratHttpInHandler extends HttpTransformer implements ChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(IntegratHttpInHandler.class);
	
	public static final String MO_REGEXP_FROMADDR = "FromAddr=\"(\\d+)\"";
	public static final String MO_REGEXP_TOADDR = "ToAddr=\"(\\d+)\"";
	public static final String MO_REGEXP_NETWORKID = "NetworkID=\"(\\d+)\"";
	public static final String MO_REGEXP_USERMSG = "<Content Type=\"TEXT\">(.*)</Content>";
    public static final String MO_REGEXP_USERMSG_HEX = "<Content Type=\"HEX\">(.*)</Content>";
	
	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-integrat", outputChannel = "incomingQueue")
	public SmsMo transformIncomingMessage(RawMessage rawMessage) throws ChannelProcessingException {
		String waspMOData = rawMessage.getBody();
		
		if (waspMOData == null || waspMOData.isEmpty()){
			throw new ChannelProcessingException("Message body is empty");
		}

		Matcher fromAddrMatch = Pattern.compile(MO_REGEXP_FROMADDR).matcher(waspMOData);
		fromAddrMatch.find();
		String msisdn = fromAddrMatch.group(1);

		Matcher smsRxNetworkMatch = Pattern.compile(MO_REGEXP_NETWORKID).matcher(waspMOData);
		smsRxNetworkMatch.find();
		String networkId = smsRxNetworkMatch.group(1);

        String smsContent = "";
        try {
		Matcher smsRxMsgMatch = Pattern.compile(MO_REGEXP_USERMSG, Pattern.DOTALL).matcher(waspMOData);
		smsRxMsgMatch.find();
		smsContent = smsRxMsgMatch.group(1);
        } catch (IllegalStateException e) {
            smsContent = "";
            log.debug("No text content found in SMS. Inspecting for hex content.");
        }

        if (smsContent.isEmpty()) {
        try {
            Matcher smsRxMsgMatch = Pattern.compile(MO_REGEXP_USERMSG_HEX, Pattern.DOTALL).matcher(waspMOData);
            smsRxMsgMatch.find();
            smsContent = HexUtil.convertHexToString(smsRxMsgMatch.group(1));
        } catch (IllegalStateException e) {
            smsContent = "";
            log.debug("No hex content found in SMS: " + waspMOData + " Message body marked empty.");
        }
        }

		Matcher smsRxToMatch = Pattern.compile(MO_REGEXP_TOADDR, Pattern.DOTALL).matcher(waspMOData);
		smsRxToMatch.find();
		String destAddr = smsRxToMatch.group(1);

		String mobileNetwork = null;
		try {
			mobileNetwork = MobilisrUtility.obtainMobileNetwork(Integer
					.parseInt(networkId));
		} catch (NumberFormatException e) {
			log.error("Unable to parse mobile network from network id [{}]", networkId);
		}

		smsContent = StringEscapeUtils.unescapeHtml(smsContent);
		SmsMo smsMO = new SmsMo(msisdn,destAddr,smsContent,new Date(), mobileNetwork);

		return smsMO;
	}

	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.IN.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig pconfig = new Pconfig(null,"Integrat HTTP");
		pconfig.setResource("in-integrat");
		return pconfig;
	}
}
