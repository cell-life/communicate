package org.celllife.mobilisr.service.exception;

import org.celllife.mobilisr.exception.MobilisrRuntimeException;

public class MobilisrSchedulingException extends MobilisrRuntimeException {

	private static final long serialVersionUID = 5829285363087234472L;

	public MobilisrSchedulingException() {
		super();
	}

	public MobilisrSchedulingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MobilisrSchedulingException(String arg0) {
		super(arg0);
	}

	public MobilisrSchedulingException(Throwable arg0) {
		super(arg0);
	}
}
