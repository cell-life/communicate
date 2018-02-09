package org.celllife.mobilisr.exception;

/**
 * Thrown when an Organization's balance has gotten out of synch. This should be picked up during a periodic
 * check of the Organization balance against the TransactionSummary.
 *
 * Note: It should not occur under normal situations.
 */
public class BalanceOutOfSyncException extends BalanceException {

	private static final long serialVersionUID = 8840428783551107760L;

	private int balance, balance2;
	private int reserved, reserved2;
	private long organizationId;

	public BalanceOutOfSyncException() {
		super();
	}

	public BalanceOutOfSyncException(long organizationId, int balance, int balance2, int reserved, int reserved2) {
		super("Organization balances out of sync [orgId="+organizationId+"]"
				+ " [oldBalance="+balance+"] [newBalance="+balance2+"]"
				+ " [oldReserved="+reserved+"] [newReserved="+reserved2+"]");
		setOrganizationId(organizationId);
		setBalance(balance);
		setBalance2(balance);
		setReserved(reserved);
		setReserved2(reserved2);
	}

	public BalanceOutOfSyncException(String message, Throwable cause) {
		super(message, cause);
	}

	public BalanceOutOfSyncException(String message) {
		super(message);
	}

	public BalanceOutOfSyncException(Throwable cause) {
		super(cause);
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getBalance2() {
		return balance2;
	}

	public void setBalance2(int balance2) {
		this.balance2 = balance2;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public int getReserved2() {
		return reserved2;
	}

	public void setReserved2(int reserved2) {
		this.reserved2 = reserved2;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
}