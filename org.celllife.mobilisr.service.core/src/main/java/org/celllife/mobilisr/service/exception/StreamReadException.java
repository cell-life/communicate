package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrRuntimeException;

public class StreamReadException extends MobilisrRuntimeException {

	private static final long serialVersionUID = -5767293063414254033L;

	public StreamReadException() {
		super();
	}

	public StreamReadException(String message) {
		super(message);
	}

	public StreamReadException(Throwable cause) {
		super(cause);
	}

	public StreamReadException(String message, Throwable cause) {
		super(message, cause);
	}

}
