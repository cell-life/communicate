package org.celllife.jdk.utilities.exception;

public class InvalidVersionException extends Exception {
	private static final long serialVersionUID = -6210392006180026204L;

	public InvalidVersionException() {
	}

	public InvalidVersionException(String message) {
		super(message);
	}
}