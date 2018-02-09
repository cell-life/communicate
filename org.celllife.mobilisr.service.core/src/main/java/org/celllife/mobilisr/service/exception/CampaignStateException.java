package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrException;


public class CampaignStateException extends MobilisrException {

	private static final long serialVersionUID = 3028531759951493962L;

	public CampaignStateException() {
		super();
	}

	public CampaignStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public CampaignStateException(String message) {
		super(message);
	}

	public CampaignStateException(Throwable cause) {
		super(cause);
	}
	
}
