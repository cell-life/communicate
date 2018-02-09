package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrRuntimeException;

public class MobilisrSecurityException extends MobilisrRuntimeException {

	private static final long serialVersionUID = -2396166057282228386L;

	public MobilisrSecurityException() {
		super();
	}

	public MobilisrSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public MobilisrSecurityException(String message) {
		super(message);
	}

	public MobilisrSecurityException(Throwable cause) {
		super(cause);
	}
	
}
