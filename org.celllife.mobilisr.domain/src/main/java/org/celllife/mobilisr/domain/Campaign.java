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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for Campaign
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Entity
@Table(name="campaign", uniqueConstraints=@UniqueConstraint(columnNames={"org_id", "name"}))
public class Campaign extends VoidableEntity implements HasOrganization, Serializable {

	private static final long serialVersionUID = 4863457184291330248L;

	public static final String PROP_TYPE = "type";
	public static final String PROP_STATUS = "status";
	public static final String PROP_START_DATE = "startDate";
	public static final String PROP_END_DATE = "endDate";
	public static final String PROP_WELCOME_MSG = "welcomeMsg";
	public static final String PROP_TIMES_PER_DAY = "timesPerDay";
	public static final String PROP_DURATION = "duration";
	public static final String PROP_NAME = "name";
	public static final String PROP_COST = "cost";
	public static final String PROP_DESCRIPTION = "description";
	public static final String PROP_SENDNOW = "sendNow";
	public static final String PROP_MSGS = "campaignMessages";
	public static final String PROP_MSGTIMES = "contactMsgTimes";
	public static final String PROP_COUNT = "contactCount";
	public static final String PROP_MSGCOUNT = "messageCount";
    public static final String PROP_LINKED_CAMPAIGN_ID = "linkedCampaignId";

	public static final int MAX_SMS_LENGTH = 160;
	public static final double SMS_LENGTH_FOR_MULTI_PART = 153d;
	
	@Version
	@Column(nullable = false)
	private Long version;
	
	@Column(nullable = false, length=20)
	@Index(name="CAMPAIGN_TYPE", columnNames={ "type"})
	@Enumerated(EnumType.STRING)
	private CampaignType type;
	
	@Column(nullable = false, length=20)
	@Index(name="CAMPAIGN_STATUS", columnNames={ "status"})
	@Enumerated(EnumType.STRING)
	private CampaignStatus status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="startDate")
	private Date startDate;
	
	@Column
	private int cost;
	
	@Column(nullable = false, unique=true, length=35)
	@Index(name="CAMPAIGN_NAME", columnNames={ "name"})
	private String name;
	
	@Column
	private String description = "";
	
	@Column
	private int timesPerDay = 0;
	
	@Column
	private int duration = 0;
	
	@Column(name="welcomemessage", columnDefinition="LONGTEXT")
	private String welcomeMsg = "";
	
	@Column(name="endDate")
	private Date endDate;
	
	@Column(name="sendNow")
	private boolean sendNow;
	
	@Column(name="contactCount")
	private int contactCount = 0;
	
	@Column(name="messageCount")
	private int messageCount = 0;

    @Column(name="linkedCampaignId", nullable=true)
    @ForeignKey(name = "fk_campaignId_linkedCampaignId")
    private Long linkedCampaignId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="org_id", nullable = false)
	@ForeignKey(name="fk_campaign_org",inverseName="fk_org_campaign")
	private Organization organization;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "campaign")
	@ForeignKey(name="fk_campaign_campaignmessage", inverseName="fk_campaignmessage_campaign")
	private List<CampaignMessage> campaignMessages = new ArrayList<CampaignMessage>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "campaign")
	@ForeignKey(name="fk_campaign_contactmsgtime", inverseName="fk_contactmsgtime_campaign")
	private List<ContactMsgTime> contactMsgTimes = new ArrayList<ContactMsgTime>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "campaign")
	@ForeignKey(name="fk_campaign_campaigncontact", inverseName="fk_campaigncontact_campaign")
	private List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();	

	/**
	 * Variable used to indicate that the campaign messages should be
	 * regenerated before saving the campaign.
	 */
	@Transient
	private boolean rebuildMessages;

	/**
	 * List of message times used to rebuild the campaign messages.
	 */
	@Transient
	private List<Date> messageTimes;
	
	public Campaign() {
	}

	/**
	 * Constructor used for Fixed Campaigns mostly
	 * @param type			@see CampaignType.xxx
	 * @param status		@see CampaignStatus.xxx
	 * @param startDate		Start Date of the campaign
	 * @param name			Name of the campaign
	 * @param description	Description about the campaign, can be null
	 * @param organization	Organisation the campaign belongs to, cannot be null
	 */
	public Campaign(CampaignType type, CampaignStatus status, Date startDate, String name,
			String description, Organization organization) {
		super();
		this.type = type;
		this.status = status;
		this.startDate = startDate == null ? null : new Date(startDate.getTime());
		this.name = name;
		this.description = description;
		this.organization = organization;
	}

	/**
	 * Constructor used for Relative/Generic campaigns mostly
	 * @param name				Name of the campaign, cannot be null
	 * @param description		Description of the campaign, can be null
	 * @param type				@see CampaignType.xxx
	 * @param status			@see CampaignStatus.xxx
	 * @param campaignDuration	Duration of the campaign, e.g: 30 days, 60 days etc
	 * @param timesPerDay		How many messages does the campaign send per day, e.g: 2times/day etc
	 * @param organization		Organisation the campaign belongs to, cannot be null
	 */
	public Campaign(String name, String description, CampaignType type, CampaignStatus status, 
					int campaignDuration, int timesPerDay, Organization organization) {
		this.type = type;
		this.status = status;
		this.startDate = new Date();
		this.duration = campaignDuration;
		this.name = name;
		this.description = description;
		this.organization = organization;
		this.timesPerDay = timesPerDay;

	}

	public CampaignType getType() {
		return type;
	}

	public void setType(CampaignType type) {
		this.type = type;
	}

	public CampaignStatus getStatus() {
		return status;
	}

	public void setStatus(CampaignStatus status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate == null ? null : new Date(startDate.getTime());
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate == null ? null : new Date(startDate.getTime());;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTimesPerDay() {
		return timesPerDay;
	}

	public void setTimesPerDay(int timesPerDay) {
		this.timesPerDay = timesPerDay;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<CampaignMessage> getCampaignMessages() {
		return campaignMessages;
	}

	public void setCampaignMessages(List<CampaignMessage> campaignMessages) {
		this.campaignMessages = campaignMessages;
	}

	public List<ContactMsgTime> getContactMsgTimes() {
		return contactMsgTimes;
	}

	public void setContactMsgTimes(List<ContactMsgTime> contactMsgTimes) {
		this.contactMsgTimes = contactMsgTimes;
	}

	public List<CampaignContact> getCampaignContacts() {
		return campaignContacts;
	}

	public void setCampaignContacts(List<CampaignContact> campaignContacts) {
		this.campaignContacts = campaignContacts;
	}

	public boolean isSendNow() {
		return sendNow;
	}

	public void setSendNow(boolean sendNow) {
		this.sendNow = sendNow;
	}

	public String getWelcomeMsg() {
		return welcomeMsg;
	}

	public void setWelcomeMsg(String welcomeMsg) {
		this.welcomeMsg = welcomeMsg;
	}

	public Date getEndDate() {
		return endDate == null ? null : new Date(endDate.getTime());
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate == null ? null : new Date(endDate.getTime());
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void setRebuildMessages(boolean rebuildMessages) {
		this.rebuildMessages = rebuildMessages;
	}
	
	public boolean isRebuildMessages() {
		return rebuildMessages;
	}
	
	public void setMessageTimes(List<Date> messageTimes) {
		this.messageTimes = messageTimes;
	}
	
	public List<Date> getMessageTimes() {
		return messageTimes;
	}

	public boolean isActive() {
		return CampaignStatus.ACTIVE.equals(status);
	}
	
	public boolean isRunning() {
		return CampaignStatus.RUNNING.equals(status);
	}

	public boolean isScheduled() {
		return CampaignStatus.SCHEDULED.equals(status);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Campaign: ").append(name);
		return builder.toString();
	}
	
	public int getContactCount() {
		return this.contactCount;
	}
	
	public void setContactCount(int contactCount) {
		this.contactCount = contactCount;
	}
	
	public int getMessageCount() {
		return this.messageCount;
	}
	
	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

    public Long getLinkedCampaignId() {
        return linkedCampaignId;
    }

    public void setLinkedCampaignId(long linkedCampaignId) {
        if (linkedCampaignId == 0) {
           // do nothing, it should never be zero
        } else {
           this.linkedCampaignId = linkedCampaignId;
        }
	}
}
