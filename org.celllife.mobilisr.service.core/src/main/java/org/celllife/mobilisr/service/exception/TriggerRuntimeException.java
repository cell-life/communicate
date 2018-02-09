package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrRuntimeException;

public class TriggerRuntimeException extends MobilisrRuntimeException {

	private static final long serialVersionUID = -6984681734558385694L;

	public TriggerRuntimeException() {
		super();
	}

	public TriggerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TriggerRuntimeException(String message) {
		super(message);
	}

	public TriggerRuntimeException(Throwable cause) {
		super(cause);
	}

}
