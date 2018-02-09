package org.celllife.mobilisr.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="contact_contactgroup", uniqueConstraints=@UniqueConstraint(columnNames={"contact_id", "contactGroup_id"}))
public class Contact_ContactGroup extends AbstractBaseEntity {

	private static final long serialVersionUID = -7303505151744254007L;

	public static final String PROP_CONTACT_GROUP = "contactGroup";
	public static final String PROP_CONTACT = "contact";

	@ManyToOne
	@JoinColumn(name="contact_id", nullable = false)
	@ForeignKey(name="fk_contactcontactgroup_contact",inverseName="fk_contact_contactcontactgroup")
	private Contact contact;
	
	@ManyToOne
	@JoinColumn(name="contactGroup_id", nullable = false)
	@ForeignKey(name="fk_contactcontactgroup_contactgroup",inverseName="fk_contactgroup_contactcontactgroup")
	private ContactGroup contactGroup;

	
	
	public Contact_ContactGroup() {
	}

	public Contact_ContactGroup(Contact contact, ContactGroup contactGroup) {
		super();
		this.contact = contact;
		this.contactGroup = contactGroup;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	public void setContactGroup(ContactGroup contactGroup) {
		this.contactGroup = contactGroup;
	}
	
	
	
}
