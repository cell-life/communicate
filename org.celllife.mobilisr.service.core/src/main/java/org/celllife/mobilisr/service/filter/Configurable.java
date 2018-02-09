package org.celllife.mobilisr.service.filter;

import java.util.Properties;

import org.celllife.mobilisr.domain.PropertyConfig;

/**
 * Interface for classes that can be configured with a property String
 * 
 * @see Properties
 * @author Simon Kelly
 */
public interface Configurable {
	public void init(PropertyConfig properties);

	public void init(String properties);
	
}
