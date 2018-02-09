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

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name="contactmsgtime")
public class ContactMsgTime extends AbstractBaseEntity implements Serializable {

	private static final long serialVersionUID = 3895633183636019578L;
	
	public static final String PROP_MSG_TIME = "msgTime";
	public static final String PROP_CAMPAIGN_CONTACT = "campaignContact";
	public static final String PROP_CAMPAIGN = "campaign";
	public static final String PROP_MSG_SLOT = "msgSlot";
	
	@Temporal(value=TemporalType.TIME)
	@Column(name="msgTime")
	@Index(name="CAMPCONTACT_TIME", columnNames={ "msgtime"})
	private Date msgTime;
	
	@Column
	private int msgSlot;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="campcontact_id", nullable = false)
	@ForeignKey(name="fk_contactmsgtime_campaigncontact")
	private CampaignContact campaignContact;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="campaign_id", nullable = false)
	@ForeignKey(name="fk_contactmsgtime_campaign")
	private Campaign campaign;
	
	
	
	public ContactMsgTime() {
	}

	public ContactMsgTime(Date msgTime, int msgSlot,
			CampaignContact campaignContact, Campaign campaign) {
		super();
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
		this.msgSlot = msgSlot;
		this.campaignContact = campaignContact;
		this.campaign = campaign;
	}

	public Date getMsgTime() {
		return msgTime == null ? null : new Date(msgTime.getTime());
	}

	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime == null ? null : new Date(msgTime.getTime());
	}

	public CampaignContact getCampaignContact() {
		return campaignContact;
	}

	public void setCampaignContact(CampaignContact campaignContact) {
		this.campaignContact = campaignContact;
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
}
