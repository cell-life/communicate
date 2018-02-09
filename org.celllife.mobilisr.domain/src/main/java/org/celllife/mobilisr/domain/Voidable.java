package org.celllife.mobilisr.domain;


public interface Voidable {
	
	public static final String PROP_VOIDED = "voided";
	
	public void setVoided(Boolean voided);

	public Boolean getVoided();

	public boolean isVoided();

}
