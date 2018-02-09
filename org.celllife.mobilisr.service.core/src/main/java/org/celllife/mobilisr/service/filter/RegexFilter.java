package org.celllife.mobilisr.service.filter;

import org.celllife.mobilisr.service.action.AbstractConfigurable;
import org.celllife.mobilisr.service.exception.TriggerException;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RegexFilter matches messages that match the supplied regular expression.
 * <br/>
 * The regex is specified in the configuration passed to the
 * {@link #init(String)} method with the key {@link #REGEX}
 * 
 * @author Simon Kelly
 */
@Component("RegexFilter")
public class RegexFilter extends AbstractConfigurable implements Filter {

	private static Logger log = LoggerFactory.getLogger(RegexFilter.class);
	
	public static final String BEAN_NAME = "RegexFilter";
	public static final String REGEX = "regex";

	private Pconfig config;

	public boolean matches(String smsMsg) throws TriggerException {
		String regex = (String) getProperty(REGEX);
		if (regex == null || regex.trim().isEmpty()){
			throw new TriggerException("Empty regex");
		}		
		boolean matches = smsMsg.matches(regex);
		log.trace("Testing regex filter against message. " +
				"regex=[{}], message=[{}], matched=[{}]",
				new Object[] { regex, smsMsg, matches });
		return matches;
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		if (config == null){
			config = new Pconfig(null, "Regex filter");
			config.setResource(BEAN_NAME);
			StringParameter keyword = new StringParameter(REGEX,"Regex:");
			keyword.setTooltip("The regular expression to filter messages with.");
			config.addParameter(keyword);
			
			config.addProperty(RANK, "2");
		}
		
		return config;
	}
}
