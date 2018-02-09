package org.celllife.mobilisr.utilbean;

/**
 * Bean used to store transaction summary result
 * 
 * @author Simon Kelly
 */
public class TransactionSummary {
	
	public static final String PROP_COST = "cost";
	public static final String PROP_RESERVED = "reserved";
	public static final String PROP_TRANSACTION_COUNT = "transactionCount";

	private Integer cost = 0;
	private Integer reserved = 0;
	private Integer transactionCount = 0;

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	public Integer getReserved() {
		return reserved;
	}

	public void setReserved(Integer reserved) {
		this.reserved = reserved;
	}

	public void setTransactionCount(Integer transactionCount) {
		this.transactionCount = transactionCount;
	}
	
	public Integer getTransactionCount() {
		return transactionCount;
	}
	
	public void makeNullsZero(){
		if (cost == null){
			cost = 0;
		}
		if (reserved == null){
			reserved = 0;
		}
		
		if (transactionCount == null){
			transactionCount = 0;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransactionSummary [cost=").append(cost)
				.append(", reserved=").append(reserved)
				.append(", transactionCount=").append(transactionCount)
				.append("]");
		return builder.toString();
	}
	
}
