package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for CampaignMessage
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Entity
@Table(name="campaignmessage")
public class CampaignMessage extends AbstractBaseEntity implements Serializable {

	private static final long serialVersionUID = -645779877226813453L;

	public static final String PROP_MESSAGE = "message";
	public static final String PROP_MSG_DATE = "msgDate";
	public static final String PROP_MSG_TIME = "msgTime";
	public static final String PROP_CAMPAIGN = "campaign";
	
	public static final String PROP_MSG_SLOT = "msgSlot";
	public static final String PROP_MSG_DAY = "msgDay";
	
	@Column(name="message", columnDefinition="LONGTEXT", nullable=false)
	@Index(name="CAMPAIGN_MSG")
	private String message;
	
	@Column(name="msgdate", nullable=false)
	@Temporal(TemporalType.DATE)
	@Index(name="CAMPAIGN_MSGDATE")
	private Date msgDate;
	
	@Column(name="msgtime")
	@Temporal(TemporalType.TIME)
	@Index(name="CAMPAIGN_MSGTIME")
	private Date msgTime;
	
	@Column(name="msgSlot")
	private int msgSlot;
	
	@Column(name="msgDay")
	@Index(name="CAMPAIGN_MSGDAY")
	private int msgDay;
	
	@Transient
	private transient Number msgLength;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id", nullable = false)
	@ForeignKey(name="fk_campaignmessage_campaign",inverseName="fk_campaign_campaignmessage")
	private Campaign campaign;
	
	@Transient
	private boolean markedForDeletion = false;

	@Transient
	public void setMarkedForDeletion(boolean b){
		markedForDeletion = b;
	}
	
	@Transient
	public boolean isMarkedForDeletion(){
		return markedForDeletion; 
	}

	public CampaignMessage() {
	}

	public CampaignMessage(String campaignMessage, Date msgDate, Date msgTime, Campaign campaign) {
		super();
		this.message = campaignMessage;
		this.msgDate = msgDate == null ? null : new Date(msgDate.getTime());
		this.msgTime =  msgTime == null ? null : new Date(msgTime.getTime());
		this.campaign = campaign;
	}
	
	public CampaignMessage(String campaignMessage, Date msgDate, Date msgTime, int msgSlot, Campaign campaign) {
		this.message = campaignMessage;
		this.msgDate = msgDate == null ? null : new Date(msgDate.getTime());
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
		this.msgSlot = msgSlot;
		this.campaign = campaign;
	}

	public CampaignMessage(String campaignMessage, int msgDay, Date msgTime, int msgSlot, Campaign campaign) {
		this.message = campaignMessage;
		this.setMsgDay(msgDay);
		this.msgDate = new Date();
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
		this.msgSlot = msgSlot;
		this.campaign = campaign;
	}

	
	public CampaignMessage(String campaignMessage, int msgDay, Date msgTime, Campaign campaign) {
		this.message = campaignMessage;
		this.setMsgDay(msgDay);
		this.msgDate = new Date();
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
		this.campaign = campaign;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getMsgDate() {
		return msgDate == null ? null : new Date(msgDate.getTime());
	}

	public void setMsgDate(Date msgDate) {
		this.msgDate = msgDate == null ? null : new Date(msgDate.getTime());
	}

	public Date getMsgTime() {
		return msgTime == null ? null : new Date(msgTime.getTime());
	}

	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
	}
	
	public int getMsgSlot() {
		return msgSlot;
	}

	public void setMsgSlot(int msgSlot) {
		this.msgSlot = msgSlot;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public void setMsgDay(int msgDay) {
		this.msgDay = msgDay;
	}

	public int getMsgDay() {
		return msgDay;
	}
	
	/**
	 * Transient field only used when calculating campaign cost
	 * 
	 * @see org.celllife.mobilisr.dao.impl.CampaignDAOImpl.getCampaignMessageLengthsAndDay(Long)
	 * @return
	 */
	public Number getMsgLength() {
		if (msgLength != null)
			return msgLength;
		
		if (message != null)
			return message.length();
		
		return -1;
	}
	
	public void setMsgLength(Number msgLength) {
		this.msgLength = msgLength;
	}

	@Override
	public String toString() {
		return "CampaignMessage [message=" + message + ", msgDate=" + msgDate
				+ ", msgTime=" + msgTime + ", msgSlot=" + msgSlot + ", msgDay="
				+ msgDay + "]";
	}
}
