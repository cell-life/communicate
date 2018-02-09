package org.celllife.mobilisr.exception;

public class ImportException extends MobilisrRuntimeException {

	private static final long serialVersionUID = -946430820787204320L;

	public ImportException() {
		super();
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImportException(String message) {
		super(message);
	}

	public ImportException(Throwable cause) {
		super(cause);
	}
}
