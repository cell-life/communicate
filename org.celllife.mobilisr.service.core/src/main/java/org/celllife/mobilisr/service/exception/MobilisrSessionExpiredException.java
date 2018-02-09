package org.celllife.mobilisr.service.exception;

public class MobilisrSessionExpiredException extends MobilisrSecurityException {

	private static final long serialVersionUID = -3665232960142126402L;

	public MobilisrSessionExpiredException() {
	}

	public MobilisrSessionExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public MobilisrSessionExpiredException(String message) {
		super(message);
	}

	public MobilisrSessionExpiredException(Throwable cause) {
		super(cause);
	}
}
