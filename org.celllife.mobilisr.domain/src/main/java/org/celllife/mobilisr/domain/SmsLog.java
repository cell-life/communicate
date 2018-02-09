package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.celllife.mobilisr.constants.SmsStatus;
import org.hibernate.annotations.Index;

/**
 * Domain class for SmsLog
 * @author Vikram Bindal
 */
@Entity
@Table(name="smslog")
public class SmsLog extends VoidableEntity implements HasOrganization, Serializable {

	public static final String SMS_DIR_IN = "IN";
	public static final String SMS_DIR_OUT = "OUT";

	public static final String PROP_DIR = "dir";
	public static final String PROP_MSISDN = "msisdn";
	public static final String PROP_MOBILE_NETWORK = "mobileNetwork";
	public static final String PROP_MESSAGE = "message";
	public static final String PROP_CREATEDFOR = "createdfor";
	public static final String PROP_STATUS = "status";
	public static final String PROP_TRACKING_NUM = "trackingnumber";
	public static final String PROP_FAIL_REASON = "failreason";
	public static final String PROP_DATE_TIME = "datetime";
	public static final String PROP_CHANNEL = "channel";
	public static final String PROP_WASP_STATUS = "waspStatus";
	public static final String PROP_ATTEMPTS = "attempts";
	public static final String PROP_CONTACT = "contact";

	private static final long serialVersionUID = -2624001759729369156L;

	@Column(nullable = false, length=10)
	@Index(name = "SMSLOG_DIR", columnNames = { "dir" })
	private String dir;

	@Column(nullable = false, length=20)
	@Index(name = "SMSLOG_MSISDN", columnNames = { "msisdn" })
	private String msisdn;

	@Column(name="mobilenetwork", length=10)
	@Index(name = "SMSLOG_NETWORK", columnNames = { "mobilenetwork" })
	private String mobileNetwork;

	@Column(name="message", nullable = false,  columnDefinition="LONGTEXT")
	@Index(name = "SMSLOG_MESSAGE", columnNames = { "message" })
	private String message;

	@Column(name="createdfor", length=60, nullable=false)
	@Index(name = "SMSLOG_CREATEDFOR", columnNames = { "createdfor" })
	private String createdfor;

	@Column(name="status", length=20, nullable=false)
	@Index(name = "SMSLOG_STATUS", columnNames = { "status" })
	@Enumerated(EnumType.STRING)
	private SmsStatus status;

	@Column(name="trackingnumber", length=255)
	@Index(name = "SMSLOG_TRACKINGNUM", columnNames = { "trackingnumber" })
	private String trackingnumber;

	@Column(name="failreason", length=510)
	private String failreason;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="datetime", nullable = false)
	@Index(name = "SMSLOG_DATETIME", columnNames = { "datetime" })
	private Date datetime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="channel_id")
	private Channel channel;

	@Column(name="waspstatus", length=20)
	private String waspStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="organization_id", nullable=true)
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="contact_id", nullable=true)
	private Contact contact;

	@Column(name="attempts")
	private int attempts;

	public SmsLog() {
	}

	public SmsLog(String msisdn, String mobileNetwork,
			String smsMsg, String createdFor, SmsStatus smsStatus, String smsTrackingNumber, String smsfailreason, Date msgDateTime,
			Channel channel, Organization organization) {
		this.dir = SmsLog.SMS_DIR_IN;
		this.msisdn = msisdn;
		this.mobileNetwork = mobileNetwork;
		this.message = smsMsg;
		this.createdfor = createdFor;
		this.status = smsStatus;
		this.trackingnumber = smsTrackingNumber;
		this.failreason = smsfailreason;
		this.datetime = msgDateTime == null ? null : new Date(msgDateTime.getTime());
		this.channel = channel;
		this.organization = organization;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreatedfor() {
		return createdfor;
	}

	public void setCreatedfor(String createdfor) {
		this.createdfor = createdfor;
	}

	public SmsStatus getStatus() {
		return status;
	}

	public void setStatus(SmsStatus status) {
		this.status = status;
	}

	public String getTrackingnumber() {
		return trackingnumber;
	}

	public void setTrackingnumber(String trackingnumber) {
		this.trackingnumber = trackingnumber;
	}

	public String getFailreason() {
		return failreason;
	}

	public void setFailreason(String failreason) {
		this.failreason = failreason;
	}

	public Date getDatetime() {
		return datetime == null ? null : new Date(datetime.getTime());
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime == null ? null : new Date(datetime.getTime());
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getWaspStatus() {
		return waspStatus;
	}

	public void setWaspStatus(String waspStatus) {
		this.waspStatus = waspStatus;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
}
