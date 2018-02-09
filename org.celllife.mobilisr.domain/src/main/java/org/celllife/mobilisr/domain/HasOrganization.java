package org.celllife.mobilisr.domain;

/**
 * Entity objects implementing this interface must have a many-to-one field
 * called organization.
 * 
 * @author Simon Kelly
 */
public interface HasOrganization {
	
	public static final String PROP_ORGANIZATION = "organization";

	public Organization getOrganization();
	
	public void setOrganization(Organization organization);
}
