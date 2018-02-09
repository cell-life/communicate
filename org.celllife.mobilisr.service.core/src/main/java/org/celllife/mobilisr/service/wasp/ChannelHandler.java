package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.pconfig.model.Pconfig;

public interface ChannelHandler {
	
	public boolean supportsChannelType(ChannelType type);
	
	/**
	 * Must contain a non-null resource name
	 * @return
	 */
	Pconfig getConfigDescriptor();

	/**
	 * @param config
	 * @throws IllegalArgumentException if configuration fails
	 */
	void configure(Pconfig config);
	
	public void start();
	
	public void stop();

}
