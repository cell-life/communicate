package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQuery;

/**
 * Domain class for CampaignContact
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Entity
@Table(name="campaigncontact", uniqueConstraints=@UniqueConstraint(columnNames={"campaign_id", "msisdn"}))
@NamedQuery(name = "updateContactDayForCampaign", query = "update CampaignContact cc set cc.progress = cc.progress + 1 " +
		"where cc.campaign = :campaign " +
		"and progress < :campDuration " +
		"and cc.joiningDate < :campEndDate")
public class CampaignContact extends AbstractBaseEntity implements Serializable, Messagable{

	private static final long serialVersionUID = 6201097423851343925L;
	
	public static final String PROP_CAMPAIGN = "campaign";
	public static final String PROP_MSISDN = "msisdn";	
	public static final String PROP_MOBILENETWORK = "mobileNetwork";
	public static final String PROP_JOINING_DATE = "joiningDate";
	public static final String PROP_PROGRESS = "progress";
	public static final String PROP_CONTACT_MSG_TIMES = "contactMsgTimes";
	public static final String PROP_CONTACT = "contact";
	public static final String PROP_RECEIVED_WELCOME = "receivedWelcome";
	public static final String PROP_END_DATE = "endDate";
	public static final String PROP_INVALID = "invalid";
    public static final String PROP_DATE_LAST_MESSAGE = "dateLastMessage";
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="campaign_id", nullable = false)
	@ForeignKey(name="fk_campaigncontact_campaign")
	private Campaign campaign;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="contact_id", nullable = false)
	@ForeignKey(name="fk_campaigncontact_contact")
	private Contact contact;
	
	@Column(name="msisdn", nullable = false, length=20)
	@Index(name = "CAMPCONTACT_MSISDN", columnNames={ "msisdn"})
	private String msisdn;
	
	@Column(name="mobileNetwork", length=20)
	private String mobileNetwork;
	
	@Column(name="progress")
	@Index(name = "CAMPCONTACT_PROGRESS", columnNames={ "progress"})
	private int progress;

	@Temporal(value=TemporalType.DATE)
	@Column(name="joinedDate")
	@Index(name="CAMPCONTACT_JOINDATE", columnNames={ "joinedDate"})
	private Date joiningDate;
	
	@Column(name="receivedwelcome")
	private boolean receivedWelcome;
	
	@Column (name="endDate", nullable = true)
	@Index(name="CAMPCONTACT_END_DATE", columnNames={ "endDate"})
	private Date endDate;
	
	@Column(name="invalid", nullable=false)
	private boolean invalid;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "campaignContact")
	@OrderBy(ContactMsgTime.PROP_MSG_SLOT)
	@ForeignKey(name="fk_campaigncontact_contactmsgtime", inverseName="fk_contactmsgtime_campaigncontact")
	private List<ContactMsgTime> contactMsgTimes = new ArrayList<ContactMsgTime>();

    @Temporal(value=TemporalType.DATE)
    @Column(name="dateLastMessage", nullable=true)
    private Date dateLastMessage;
	
	public CampaignContact() {
	}

	public CampaignContact(Campaign campaign, Contact contact) {
		this.campaign = campaign;
		this.msisdn = contact.getMsisdn();
		this.contact = contact;
		this.mobileNetwork = contact.getMobileNetwork();
		this.joiningDate = new Date();
		this.progress = 0;
	}
	
	public CampaignContact(Campaign campaign, Contact contact, String mobileNetwork, int progress, Date joiningDate) {
		super();
		this.campaign = campaign;
		this.msisdn = contact.getMsisdn();
		this.contact = contact;
		this.mobileNetwork = mobileNetwork;
		this.progress = progress;
		this.joiningDate = joiningDate == null ? null : new Date(joiningDate.getTime());
	}

	public CampaignContact(Campaign campaign, Contact contact, List<ContactMsgTime> contactMsgTimes) {
		this.campaign = campaign;
		this.msisdn = contact.getMsisdn();
		this.contact = contact;
		this.joiningDate = new Date();
		this.contactMsgTimes = contactMsgTimes;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	@Override
	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
		this.msisdn = contact.getMsisdn();
	}

	@Override
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

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public Date getJoiningDate() {
		return joiningDate == null ? null : new Date(joiningDate.getTime());
	}

	public void setJoiningDate(Date joiningDate) {
		this.joiningDate = joiningDate == null ? null : new Date(joiningDate.getTime());
	}

	public List<ContactMsgTime> getContactMsgTimes() {
		if (contactMsgTimes == null){
			contactMsgTimes = new ArrayList<ContactMsgTime>();
		}
		return contactMsgTimes;
	}

	public void setContactMsgTimes(List<ContactMsgTime> contactMsgTimes) {
		this.contactMsgTimes = contactMsgTimes;
	}

	public void setReceivedWelcome(boolean receivedWelcome) {
		this.receivedWelcome = receivedWelcome;
	}

	public boolean getReceivedWelcome() {
		return receivedWelcome;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public boolean isInvalid() {
		return invalid;
	}

    public Date getDateLastMessage() {
        return dateLastMessage;
    }

    public void setDateLastMessage(Date dateLastMessage) {
        this.dateLastMessage = dateLastMessage;
	}
}
