package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrException;

/**
 * This exception is thrown when a user tries to access data
 * that does not exist.
 * 
 * @author Simon Kelly <simon@cell-life.org>
 *
 */
public class ObjectNotFoundException extends MobilisrException {

	private static final long serialVersionUID = 4819294271622788114L;

	public ObjectNotFoundException() {
		super();
	}

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
