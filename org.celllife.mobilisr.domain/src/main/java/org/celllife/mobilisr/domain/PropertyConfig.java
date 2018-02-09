package org.celllife.mobilisr.domain;

/**
 * This interface allows generalised treatment of entity objects that have
 * configuration properties. These properties are generally stored as a YAML
 * string.
 */
public interface PropertyConfig {

	public String getProperties();

	/**
	 * Set the string representation of the properties.
	 * 
	 * @param properties
	 */
	void setProperties(String properties);
}
