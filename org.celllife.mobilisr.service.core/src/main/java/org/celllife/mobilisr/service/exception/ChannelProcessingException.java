package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrException;

public class ChannelProcessingException extends MobilisrException {

	private static final long serialVersionUID = -3831968368361131669L;

	public ChannelProcessingException() {
	}

	public ChannelProcessingException(String message) {
		super(message);
	}

	public ChannelProcessingException(Throwable cause) {
		super(cause);
	}

	public ChannelProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

}
