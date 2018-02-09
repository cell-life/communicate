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

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for Group
 * @author Vikram Bindal
 */
@Entity
@Table(name="contactgroup", uniqueConstraints=@UniqueConstraint(columnNames={"org_id", "groupname"}))
public class ContactGroup extends AbstractBaseEntity implements HasOrganization, Serializable {

	private static final long serialVersionUID = -2315549841379720090L;

	public static final String PROP_GROUP_NAME = "groupName";
	public static final String PROP_GROUP_DESCRIPTION = "groupDescription";
	public static final String PROP_CONTACTS = "contacts";

	@Column(name="groupname", nullable = false, length=35)
	@Index(name = "GROUP_NAME")
	private String groupName;

	@Column(name="groupdescription")
	private String groupDescription;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="org_id", nullable = false)
	@ForeignKey(name="fk_contactgroup_organization",inverseName="fk_organization_contactgroup")
	private Organization organization;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "contact_contactgroup", joinColumns = { @JoinColumn(name = "contactgroup_id") }, inverseJoinColumns = { @JoinColumn(name = "contact_id") })
	@ForeignKey(name="fk_contactgroup_contact",inverseName="fk_contact_contactgroup")
	private List<Contact> contacts = new ArrayList<Contact>();

	public ContactGroup() {
	}

	public ContactGroup(String groupName, String groupDescription) {
		this.groupName = groupName;
		this.groupDescription = groupDescription;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the groupDescription
	 */
	public String getGroupDescription() {
		return groupDescription;
	}

	/**
	 * @param groupDescription
	 *            the groupDescription to set
	 */
	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	
	
	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * The list returned from here cannot be modified
	 * @return	Unmodified list
	 */
	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	/**
	 * @return the persons
	 */
	public List<Contact> getPersons() {
		return contacts;
	}

	/**
	 * @param contacts
	 *            the persons to set
	 */
	public void setPersons(List<Contact> contacts) {
		this.contacts = contacts;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
