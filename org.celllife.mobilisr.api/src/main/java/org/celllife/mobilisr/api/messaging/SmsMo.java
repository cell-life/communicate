package org.celllife.mobilisr.api.messaging;

import java.util.Date;

import org.celllife.mobilisr.api.MobilisrDto;

public class SmsMo implements MobilisrDto {

	private static final long serialVersionUID = -2041700349062367937L;

	private String sourceAddr;
	private String destAddr;
	private String message;
	private Date dateReceived;
	private String mobileNetwork;
	private String reference;
	
	public SmsMo() {
	}
	
	public SmsMo(String sourceAddr, String destAddr, String message,
			Date dateReceived, String mobileNetwork) {
		super();
		this.sourceAddr = sourceAddr;
		this.destAddr = destAddr;
		this.message = message;
		this.dateReceived = dateReceived;
		this.mobileNetwork = mobileNetwork;
	}

	public String getDestAddr() {
		return destAddr;
	}

	public void setDestAddr(String destAddr) {
		this.destAddr = destAddr;
	}

	public Date getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}

	public void setSourceAddr(String msisdn) {
		this.destAddr = msisdn;
	}

	public String getSourceAddr() {
		return sourceAddr;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getMobileNetwork() {
		return mobileNetwork;
	}

	public void setMobileNetwork(String mobileNetwork) {
		this.mobileNetwork = mobileNetwork;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SmsMo [sourceAddr=").append(sourceAddr)
				.append(", destAddr=").append(destAddr).append(", message=")
				.append(message).append("]");
		return builder.toString();
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}
}
