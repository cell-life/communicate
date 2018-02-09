package org.celllife.mobilisr.exception;


public class InsufficientBalanceException extends BalanceException {

	private static final long serialVersionUID = -8830975632158624777L;
	
	private int amountRequested;
	private int currentBalance;
	private String transactionMessage;

	public InsufficientBalanceException() {
		super();
	}

	public InsufficientBalanceException(int amountRequested, int currentBalance, String transactionMessage) {
		super("Insufficient balance to process transaction."
				+ " Amount requested='" + amountRequested + "'."
				+ " Available balance='" + currentBalance + "'");
		this.transactionMessage = transactionMessage;
		this.setAmountRequested(amountRequested);
		this.setCurrentBalance(currentBalance);
	}

	public InsufficientBalanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public InsufficientBalanceException(String message) {
		super(message);
	}

	public InsufficientBalanceException(Throwable cause) {
		super(cause);
	}

	public void setAmountRequested(int amountRequested) {
		this.amountRequested = amountRequested;
	}

	public int getAmountRequested() {
		return amountRequested;
	}

	public void setCurrentBalance(int currentBalance) {
		this.currentBalance = currentBalance;
	}

	public int getCurrentBalance() {
		return currentBalance;
	}
	
	public String getTransactionMessage() {
		return transactionMessage;
	}

}
