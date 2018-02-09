package org.celllife.mobilisr.service.filter;

import org.celllife.mobilisr.service.action.AbstractConfigurable;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ChannelTrigger matches all messages.
 * 
 * @author Simon Kelly
 */
@Component("MatchAllFilter")
public class MatchAllFilter extends AbstractConfigurable implements Filter{ 
	
	private static Logger log = LoggerFactory.getLogger(MatchAllFilter.class);
	
	public static final String BEAN_NAME = "MatchAllFilter";

	private Pconfig pconfig;

	public boolean matches(String smsMsg) {
		log.trace("MatchAllFilter matching against message. Message=[{}]", smsMsg);
		return true; 
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		if (pconfig == null) {
			pconfig = new Pconfig(null, "Match all filter");
			pconfig.setResource(BEAN_NAME);
			pconfig.addProperty(RANK, "3");
		}
		return pconfig;
	}
}
