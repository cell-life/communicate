package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for Person
 * @author Vikram Bindal
 */
@Entity
@Table(name="contact", uniqueConstraints=@UniqueConstraint(columnNames={"msisdn", "organization_id"}))
public class Contact extends AbstractBaseEntity implements Messagable, HasOrganization, Serializable {

	private static final long serialVersionUID = -5799002893269744732L;

	public static final String PROP_MSISDN = "msisdn";
	public static final String PROP_MOBILE_NETWORK = "mobileNetwork";
	public static final String PROP_FIRST_NAME = "firstName";
	public static final String PROP_LAST_NAME = "lastName";
	public static final String PROP_INVALID = "invalid";
	public static final String PROP_CONTACT_GROUPS = "contactGroups";

	@Column(name="msisdn", nullable = false, length=20)
	@Index(name = "CONTACT_MSISDN", columnNames = { "msisdn" })
	private String msisdn;

	@Column(name="mobilenetwork", length=10)
	@Index(name = "CONTACT_NETWORK", columnNames = { "mobileNetwork" })
	private String mobileNetwork;

	@Column(name="firstname", length=35)
	private String firstName;

	@Column(name="lastname", length=35)
	private String lastName;
	
	@Column(name="invalid", nullable=false)
	private boolean invalid;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id", nullable = false)
	@ForeignKey(name="fk_contact_organization",inverseName="fk_organization_contact")
	private Organization organization;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "contact_contactgroup", joinColumns = { @JoinColumn(name = "contact_id") }, inverseJoinColumns = { @JoinColumn(name = "contactgroup_id") })
	private List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
	
	
	public Contact() {
	}
	
	public Contact(String msisdn, Organization organization) {
		super();
		this.msisdn = msisdn;
		this.organization = organization;
	}

	public Contact(String msisdn, String mobileNetwork, String firstName,
			String lastName) {
		this.msisdn = msisdn;
		this.mobileNetwork = mobileNetwork;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * @return the msisdn
	 */
	@Override
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the mobileNetwork
	 */
	public String getMobileNetwork() {
		return mobileNetwork;
	}

	/**
	 * @param mobileNetwork
	 *            the mobileNetwork to set
	 */
	public void setMobileNetwork(String mobileNetwork) {
		this.mobileNetwork = mobileNetwork;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @param ContactGroups
	 *            the contactgroups to set
	 */
	public void setContactGroups(List<ContactGroup> contactGroups) {
		this.contactGroups = contactGroups;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public List<ContactGroup> getContactGroups() {
		return contactGroups;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Override
	public Contact getContact(){
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Contact: ");
		if (firstName != null) {
			builder.append(firstName).append(" ");
		}
		
		if (lastName != null){
			builder.append(lastName);
		}
		builder.append(" (").append(msisdn).append(")");
		return builder.toString();
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public boolean isInvalid() {
		return invalid;
	}
}
