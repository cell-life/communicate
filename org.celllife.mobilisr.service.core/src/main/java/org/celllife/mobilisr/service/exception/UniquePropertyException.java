package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrException;

public class UniquePropertyException extends MobilisrException {

	private static final long serialVersionUID = 6468118988273241723L;

	public UniquePropertyException() {
		super();
	}

	public UniquePropertyException(String msg) {
		super(msg);
	}

	public UniquePropertyException(Throwable arg0) {
		super(arg0);
	}

	public UniquePropertyException(String msg, Throwable arg1) {
		super(msg, arg1);
	}

}
