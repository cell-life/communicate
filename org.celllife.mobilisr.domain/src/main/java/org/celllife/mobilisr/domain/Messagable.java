package org.celllife.mobilisr.domain;

public interface Messagable {

	/**
	 * The {@link Contact}. May be null
	 * @return
	 */
	public abstract Contact getContact();

	public abstract String getMsisdn();

}