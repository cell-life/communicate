package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name="mailmessage")
public class MailMessage extends AbstractBaseEntity implements HasOrganization, Serializable {

	private static final long serialVersionUID = -2861380882365976364L;
	
	public static final String PROP_ADDRESS = "address";
	public static final String PROP_SUBJECT = "subject";
	public static final String PROP_TEXT = "text";
	public static final String PROP_DATETIME = "datetime";
	public static final String PROP_TYPE = "type";
	public static final String PROP_EMAILED = "emailed";
	
	@Column(name="address", nullable = false)
	private String address;
	
	@Column(name="subject")
	private String subject;

	@Column(name="text", nullable = false,  columnDefinition="TEXT")
	private String text;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="datetime", nullable = false)
	@Index(name = "ALERT_DATETIME", columnNames = { "datetime" })
	private Date datetime;
	
	@Column(name="type", nullable = false, length=20)
	@Index(name = "ALERT_TYPE", columnNames = { "type" })
	@Enumerated(EnumType.STRING)
	private AlertType type;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="organization_id")
	@ForeignKey(name="fk_alert_organization",inverseName="fk_organization_alert")
	private Organization organization;

	@Column(name="attachments", nullable = true, length = 510)
	private String attachments;

	private boolean emailed;

	public MailMessage(){
	}
	
	public MailMessage(String address, String subject, String text,
			AlertType type, Organization organization) {
		super();
		this.address = address;
		this.subject = subject;
		this.text = text;
		this.type = type;
		this.organization = organization;
		this.datetime = new Date();
	}

	public MailMessage(String address, String text) {
		this.address = address;
		this.text = text;
		this.datetime = new Date();
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getDatetime() {
		return datetime == null ? null : new Date(datetime.getTime());
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime == null ? null : new Date(datetime.getTime());
	}

	public boolean isEmailed() {
		return emailed;
	}

	public void setEmailed(boolean emailed) {
		this.emailed = emailed;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public String getAttachments() {
		return attachments;
	}
	
	public boolean hasAttachements(){
		return attachments != null && !attachments.isEmpty();
	}
	
	public List<String> getAttachmentList(){
		List<String> attachmentList = new ArrayList<String>();
		if (attachments == null || attachments.isEmpty()){
			return attachmentList;
		}
		String[] attachmentArray = attachments.split(",");
		for(String attachment: attachmentArray){
			attachmentList.add(attachment);
		}
		
		return attachmentList;
	}
	
	public void setAttachmentList(List<String> attachmentList){
		if (attachmentList.isEmpty()){
			this.attachments = null;
			return;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		for(String attachment: attachmentList){
			stringBuffer.append(attachment);
			stringBuffer.append(",");
		}
		
		this.attachments = stringBuffer.substring(0, stringBuffer.length()-1);
	}
}
