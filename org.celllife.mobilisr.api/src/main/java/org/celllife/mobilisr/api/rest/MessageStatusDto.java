package org.celllife.mobilisr.api.rest;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.SmsStatus;

@XmlRootElement(name = "messagestatus")
@XmlType(name = "MessageStatus", propOrder = { "id", "msisdn", "datetime",
		"status", "failreason", "message" })
public class MessageStatusDto implements MobilisrDto {

	private static final long serialVersionUID = 2375558673411698001L;

	private Long id;
	private String msisdn;
	private Date datetime;
	private SmsStatus status;
	private String failreason;
	private String message;

	public Date getDatetime() {
		return datetime;
	}

	public String getFailreason() {
		return failreason;
	}

	public Long getId() {
		return id;
	}
	
	public String getMessage() {
		return message;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public SmsStatus getStatus() {
		return status;
	}

	@XmlElement(name = "datetime", required = true)
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	@XmlElement(name = "failreason", required = false)
	public void setFailreason(String failreason) {
		this.failreason = failreason;
	}

	@XmlElement(name = "id", required = true)
	public void setId(Long id) {
		this.id = id;
	}

	@XmlElement(name = "message", required = false)
	public void setMessage(String message) {
		this.message = message;
	}

	@XmlElement(name = "msisdn", required = true)
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	
	@XmlElement(name = "status", required = true)
	public void setStatus(SmsStatus status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageStatusDto [id=").append(id).append(", msisdn=")
				.append(msisdn).append(", datetime=").append(datetime)
				.append(", status=").append(status).append(", failreason=")
				.append(failreason).append("]");
		return builder.toString();
	}

}
