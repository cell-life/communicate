package org.celllife.mobilisr.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="organization")
public class Organization extends VoidableEntity {
	
	private static final long serialVersionUID = -6239075866963070599L;

	public static final String PROP_NAME = "name";
	public static final String PROP_ADDRESS = "address";
	public static final String PROP_CONTACT_PERSON = "contactName";
	public static final String PROP_CONTACT_NUMBER = "contactNumber";
	public static final String PROP_CONTACT_EMAIL = "contactEmail";
	public static final String PROP_BALANCE = "balance";
	public static final String PROP_RESERVED = "reserved";
	public static final String PROP_CONTACT_GROUPS = "contactGroups";
	public static final String PROP_CONTACTS = "contacts";
	public static final String PROP_USERS = "users";
	public static final String PROP_SMS_LOGS = "smsLogs";
	public static final String PROP_SMS_TRIGGERS = "smsTriggers";
	public static final String PROP_BALANCE_THRESHOLD = "balanceThreshold";

	/*
	 * Transient properties
	 */
	public static final String PROP_AVAILABLE_BALANCE = "availableBalance";

	@Column(nullable = false, unique = true, length=35)
	private String name;
	
	@Column(length=255)
	private String address;
	
	@Column(name="contactname", length=70)
	private String contactName;
	
	@Column(name="contactnumber", length=20)
	private String contactNumber;
	
	@Column(name="contactemail", length=255)
	private String contactEmail;
	
	@Column(name = "balance", nullable = false)
	private int balance;
	
	@Column(name = "reserved", nullable = false)
	private int reserved;
	
	@Column(name = "balancethreshold", nullable = false)
	private int balanceThreshold = 0;

    @Column(name="retriesBeforeInvalid")
    private Integer retriesBeforeInvalid;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	@ForeignKey(name="fk_organization_user", inverseName="fk_user_organization")
	private List<User> users = new ArrayList<User>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	@ForeignKey(name="fk_organization_contactgroup", inverseName="fk_contactgroup_organization")
	private List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	@ForeignKey(name="fk_organization_contact", inverseName="fk_contact_organization")
	private List<Contact> contacts = new ArrayList<Contact>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<SmsLog> smsLogs = new ArrayList<SmsLog>();
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "organization_channel", joinColumns = { @JoinColumn(name = "organization_id") }, inverseJoinColumns = { @JoinColumn(name = "channel_id") })
	@ForeignKey(name="fk_organization_channel", inverseName="fk_channel_organization")
	private List<Channel> channels = new ArrayList<Channel>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<Campaign> campaigns = new ArrayList<Campaign>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<MessageFilter> smsTriggers = new ArrayList<MessageFilter>();
	
	/**
	 * @return the balance
	 */
	public int getBalance() {
		return balance;
	}
	
	/**
	 * @param balance
	 *            the balance to set
	 */
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	/**
	 * @return the reserved
	 */
	public int getReserved() {
		return reserved;
	}
	
	/**
	 * @param reserved
	 *            the reserved to set
	 */
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	
	/**
	 * @param contactGroups
	 *            the groups to set
	 */
	public void setContactGroup(List<ContactGroup> contactGroups) {
		this.contactGroups = contactGroups;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	/**
	 * @return the channels
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	/**
	 * @param channels
	 *            the channels to set
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	
	public void removeChannel(Channel c) {
		getChannels().remove(c);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public List<ContactGroup> getContactGroups() {
		return contactGroups;
	}

	public void setContactGroups(List<ContactGroup> contactGroups) {
		this.contactGroups = contactGroups;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public void addContact(Contact c){
		contacts.add(c);
	}

	@Override
	public String toString() {
		return name;
	}

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public int getAvailableBalance() {
		return balance - reserved;
	}
	
	public List<SmsLog> getSmsLogs() {
		return smsLogs;
	}

	public void setSmsLogs(List<SmsLog> smsLogs) {
		this.smsLogs = smsLogs;
	}

	public void setSmsTriggers(List<MessageFilter> smsTriggers) {
		this.smsTriggers = smsTriggers;
	}

	public List<MessageFilter> getSmsTriggers() {
		return smsTriggers;
	}

	public void setBalanceThreshold(int balanceThreshold) {
		this.balanceThreshold = balanceThreshold;
	}

	public int getBalanceThreshold() {
		return balanceThreshold;
	}

    public int getRetriesBeforeInvalid() {
        return retriesBeforeInvalid;
    }

    public void setRetriesBeforeInvalid(int retriesBeforeInvalid) {
        this.retriesBeforeInvalid = retriesBeforeInvalid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Organization other = (Organization) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
