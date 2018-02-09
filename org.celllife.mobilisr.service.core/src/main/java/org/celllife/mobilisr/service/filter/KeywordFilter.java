package org.celllife.mobilisr.service.filter;

import org.celllife.mobilisr.service.exception.TriggerException;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * KeywordFilter matches messages that start with the specified keyword.
 * <br/>
 * The keyword is specified in the configuration passed to the
 * {@link #init(String)} method with the key {@link #KEYWORD}
 * 
 * @author Simon Kelly
 */
@Component("KeywordFilter")
public class KeywordFilter extends RegexFilter implements Filter {
	
	private static Logger log = LoggerFactory.getLogger(KeywordFilter.class);

	public static final String BEAN_NAME = "KeywordFilter";
	public static final String KEYWORD = "keyword";

	private Pconfig config;

	public boolean matches(String smsMsg) throws TriggerException {
		String kw = (String) getProperty(KEYWORD);
		if (kw == null || kw.trim().isEmpty()){
			throw new TriggerException("Empty keyword");
		}
		
		log.trace("Testing keyword filter against message. keyword={}, message={}", kw, smsMsg);
		
		String rxSms = smsMsg.trim().toUpperCase();
		
		String keywords = kw.trim().replace("," , "|");				
		setProperty(REGEX, "^"+"("+keywords.toUpperCase()+")"+"(\\W.*)?$");
		
		return super.matches(rxSms);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		if (config == null){
			config = new Pconfig(null, "Keyword filter");
			config.setResource(BEAN_NAME);
			StringParameter keyword = new StringParameter(KEYWORD,"Keywords:");
			keyword.setTooltip("The keywords to filter messages with, comma-seperated please.");
			keyword.setRegex("[a-zA-Z0-9@$_!\"#%&'()+\\-./:;<=>?*\\^\\[\\]{}~|\\\\]*" +
					"(,[a-zA-Z0-9@$_!\"#%&'()+\\-./:;<=>?*\\^\\[\\]{}~|\\\\]*)*");
			keyword.setErrorMessage("Keywords can only use the following" +
				" characters: a-z, A-Z, 0-9, @ $ _ ! \" # % & ' ( ) + - . /" +
				" : ; < = > ? * ^ [ ] { } ~ | \\");
			config.addParameter(keyword);
			
			config.addProperty(RANK, "1");
		}
		return config;
	}
}
