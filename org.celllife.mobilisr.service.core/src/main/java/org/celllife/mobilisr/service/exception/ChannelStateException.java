package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrException;

public class ChannelStateException extends MobilisrException {

	private static final long serialVersionUID = -2629651734360444695L;

	public ChannelStateException() {
		super();
	}

	public ChannelStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelStateException(String message) {
		super(message);
	}

	public ChannelStateException(Throwable cause) {
		super(cause);
	}

	
}
