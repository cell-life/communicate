package org.celllife.mobilisr.exception;

public class DuplicateTransactionException extends BalanceException {

	private static final long serialVersionUID = 3632474046112861809L;

	public DuplicateTransactionException() {
	}

	public DuplicateTransactionException(String arg0) {
		super(arg0);
	}

	public DuplicateTransactionException(Throwable arg0) {
		super(arg0);
	}

	public DuplicateTransactionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
