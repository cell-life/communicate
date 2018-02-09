package org.celllife.mobilisr.service.wasp;

import java.util.Date;
import java.util.Map;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

/**
 * @author Simon Kelly
 */
@Component("HttpGetApiTransformer")
public class CommunicateHttpInHandler extends HttpTransformer implements ChannelHandler {

	private static final Logger log = LoggerFactory
			.getLogger(CommunicateHttpInHandler.class);

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "in-http", outputChannel = "incomingQueue")
	public SmsMo transformRawMessage(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String sender = getParameter(map, "sender");
		String receiver = getParameter(map, "receiver");
		String text = getParameter(map, "text");
		
		sender = normaliseMsisdn(sender);
		receiver = normaliseMsisdn(receiver);

		SmsMo message = new SmsMo(sender, receiver, text, new Date(), null);
		if (log.isTraceEnabled()) {
			log.trace("Message received via HTTP GET: [{}]", message);
		}
		
		return message;
	}

	private String normaliseMsisdn(String number) {
		if (number.startsWith("+")){
			return number.substring(1);
		}
		return number;
	}
	
	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.IN.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig pconfig = new Pconfig(null,"Communicate HTTP");
		pconfig.setResource("in-http");
		return pconfig;
	}
}
