package org.celllife.mobilisr.service.filter;

import org.apache.commons.chain.Command;
import org.celllife.pconfig.model.Pconfig;

public interface Action extends Command {

	public static final String FILTER = "filter";
	public static final String SMS_LOG = "smslog";
	
	public Pconfig getConfigDescriptor();

}
