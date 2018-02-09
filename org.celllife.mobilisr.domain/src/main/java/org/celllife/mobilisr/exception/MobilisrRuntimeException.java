package org.celllife.mobilisr.exception;

public class MobilisrRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7013204133472244557L;

	public MobilisrRuntimeException() {
		super();
	}

	public MobilisrRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MobilisrRuntimeException(String message) {
		super(message);
	}

	public MobilisrRuntimeException(Throwable cause) {
		super(cause);
	}

	
}
