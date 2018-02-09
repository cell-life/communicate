package org.celllife.mobilisr.api.messaging;

import java.util.Date;

import org.celllife.mobilisr.constants.DeliveryReceiptState;

public class DeliveryReceipt {

	private String id;
	private String error;
	private Date doneDate;
	private DeliveryReceiptState finalStatus;
	private String sourceAddr;
	
	public DeliveryReceipt(String id, Date doneDate,
			DeliveryReceiptState finalStatus, String error) {
		super();
		this.id = id;
		this.error = error;
		this.doneDate = doneDate;
		this.finalStatus = finalStatus;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public DeliveryReceiptState getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(DeliveryReceiptState finalStatus) {
		this.finalStatus = finalStatus;
	}

	public String getSourceAddr() {
		return sourceAddr;
	}
	
	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliveryReceipt [id=").append(id)
				.append(", sourceAddr=").append(sourceAddr)
				.append(", finalStatus=").append(finalStatus)
				.append(", doneDate=").append(doneDate).append(", error=")
				.append(error).append("]");
		return builder.toString();
	}
}
