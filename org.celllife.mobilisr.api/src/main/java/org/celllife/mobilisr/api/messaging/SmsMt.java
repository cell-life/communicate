package org.celllife.mobilisr.api.messaging;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.SmsStatus;

public class SmsMt extends BaseMT implements MobilisrDto {

	private static final long serialVersionUID = -8851974908368432650L;
	
	private String msisdn;
	private String mobileNetwork;
	private SmsStatus status;
	private String messageTrackingNumber;
	private String errorMessage;
	private Integer sendingAttempts;
	private Long contactId;

	private Long smsLogId;

	private boolean invalidNumber = false;


	public SmsMt() {
	}

	public SmsMt(String msisdn, String message, String createdFor) {
		super();
		this.msisdn = msisdn;
		setMessage(message);
		setCreatedFor(createdFor);
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getMobileNetwork() {
		return mobileNetwork;
	}

	public void setMobileNetwork(String mobileNetwork) {
		this.mobileNetwork = mobileNetwork;
	}
	
	public void setStatus(SmsStatus status) {
		this.status = status;
	}

	public SmsStatus getStatus() {
		return status;
	}
	
	public void setMessageTrackingNumber(String messageTrackingNumber) {
		this.messageTrackingNumber = messageTrackingNumber;
	}
	
	public String getMessageTrackingNumber() {
		return messageTrackingNumber;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setSendingAttempts(Integer sendingAttempts) {
		this.sendingAttempts = sendingAttempts;
	}
	
	public Integer getSendingAttempts() {
		return sendingAttempts;
	}
	
	public Long getContactId() {
		return contactId;
	}
	
	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public void setMessageLogId(Long smsLogId) {
		this.smsLogId = smsLogId;
	}
	
	public Long getMessageLogId() {
		return smsLogId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("\nSmsMt [msisdn=").append(msisdn).append(", status=")
				.append(status).append(", messageTrackingNumber=")
				.append(messageTrackingNumber).append(", errorMessage=")
				.append(errorMessage).append(", contactId=").append(contactId)
				.append(", smsLogId=").append(smsLogId).append("]");
		return builder.toString();
	}

	public void setInvalidNumber(boolean invalidNumber) {
		this.invalidNumber = invalidNumber;
	}
	
	public boolean isInvalidNumber() {
		return invalidNumber;
	}
}
