package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;

public interface ContactGroupDAO extends BaseDAO<ContactGroup, Long> {

	void addAllGroupsToContact(Organization organization, Contact contact, List<ContactGroup> removeGroupList);

	void addGroupsToContact(List<ContactGroup> contactGroupList, Contact contact);

	/**
	 * The following method removes all the groups for a particular contact
	 * @param contact	Contact object from which the groups must be removed
	 * @return			Number of entities that have been affected a result of remove operation
	 */
	int removeAllGroupFromContact(Contact contact);

	/**
	 * The following method removes all the contacts for a particular group
	 * @param contactGroup	ContactGroup object from which the contacts must be removed
	 * @return				Number of entities that have been affected a result of remove operation
	 */
	int removeAllContactsFromGroup(ContactGroup contactGroup);
	
	/**
	 * The following method removes selected groups from a particular contact
	 * @param contact			Contact object from which the group must be removed
	 * @param contactGroupList	List of ContactGroup objects that must be removed from that contact
	 * @return					Number of entities that have been affected by the result of the operation
	 */
	int removeGroupsFromContact(Contact contact, List<ContactGroup> contactGroupList);

	public abstract void addAllContactsToGroup(Organization organization, ContactGroup contactGroup, List<Contact> removedContactList);

	public abstract void addContactsToGroup(List<Contact> addedContactList, ContactGroup contactGroup);

	public abstract void removeContactsFromGroup(ContactGroup contactGroup, List<Contact> removedContactList);
}
