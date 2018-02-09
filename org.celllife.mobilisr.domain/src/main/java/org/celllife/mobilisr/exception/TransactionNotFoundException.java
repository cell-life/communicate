package org.celllife.mobilisr.exception;

public class TransactionNotFoundException extends BalanceException {

	private static final long serialVersionUID = -7904639018567111498L;

	public TransactionNotFoundException() {
	}

	public TransactionNotFoundException(String arg0) {
		super(arg0);
	}

	public TransactionNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public TransactionNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
