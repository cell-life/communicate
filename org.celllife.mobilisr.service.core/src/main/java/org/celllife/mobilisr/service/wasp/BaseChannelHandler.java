package org.celllife.mobilisr.service.wasp;

import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;

public abstract class BaseChannelHandler implements ChannelHandler{

	@Override
	public void configure(Pconfig config) {
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}
	
	public String getStringParameter(Pconfig config, String name){
		return (String) getParameterValue(config, name);
	}

	private Object getParameterValue(Pconfig config, String name) {
		Parameter<?> parameter = config.getParameter(name);
		if (parameter == null)
			return null;
		
		return parameter.getValue();
	}
	
	public Integer getIntegerParameter(Pconfig config, String name){
		return (Integer) getParameterValue(config, name);
	}
	
	public Boolean getBooleanParameter(Pconfig config, String name){
		return (Boolean) getParameterValue(config, name);
	}

}
