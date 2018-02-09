package org.celllife.mobilisr.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository("contactGroupDAO")
public class ContactGroupDAOImpl extends BaseDAOImpl<ContactGroup, Long> implements ContactGroupDAO{

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addAllContactsToGroup(Organization organization, ContactGroup contactGroup, List<Contact> removedContactList) {
		boolean excludeRemoved = (removedContactList != null) && (!removedContactList.isEmpty());
		
		String queryString = "insert into Contact_ContactGroup (contact, contactGroup) " +
				"select contact, contactGroup " +
				"from Contact contact, ContactGroup contactGroup where " +
				"contactGroup.id = :contactGroup and contact.organization = :organization";
		
		if (excludeRemoved){
				queryString += " and contact.id not in (:removeContactList)";
		}
		
		Query query = getSession().createQuery(queryString);
		query.setParameter("contactGroup", contactGroup.getId());
		query.setParameter("organization", organization);
		
		if (excludeRemoved){
			List<Long> contactIds = new ArrayList<Long>();
			for (Contact contact : removedContactList) {
				contactIds.add(contact.getId());
			}
			query.setParameterList("removeContactList", contactIds);
		}
		query.executeUpdate();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addAllGroupsToContact(Organization organization, Contact contact, List<ContactGroup> removedGroupList) {
		boolean excludeRemoved = (removedGroupList != null) && (!removedGroupList.isEmpty());
		
		String queryString = "insert into Contact_ContactGroup (contact, contactGroup) " +
				"select contact, contactGroup " +
				"from Contact contact, ContactGroup contactGroup where " +
				"contact.id = :contact and contactGroup.organization = :organization";
				
		if (excludeRemoved){
			queryString += " and contactGroup.id not in (:removeGroupList)";
		}
		
		Query query = getSession().createQuery(queryString);
		query.setParameter("contact", contact.getId());
		query.setParameter("organization", organization);
		
		if (excludeRemoved){
			List<Long> groupIds = new ArrayList<Long>();
			for (ContactGroup contactGroup : removedGroupList) {
				groupIds.add(contactGroup.getId());
			}
			query.setParameterList("removeGroupList", groupIds);
		}
		query.executeUpdate();
		
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addGroupsToContact(List<ContactGroup> contactGroupList, Contact contact){
		StringBuffer contactGroups = new StringBuffer("'");
		for (ContactGroup contactGroup : contactGroupList) {
			contactGroups.append(contactGroup.getId()).append("','");
		}
		String contactGroupString = contactGroups.toString();
		contactGroupString = contactGroupString.substring(0, contactGroupString.length() - 2);
		
		Query query = getSession().createSQLQuery("insert into contact_contactgroup (contact_id, contactGroup_id) select :cId, cg.id from contactgroup cg " +
				"where cg.id in (" + contactGroupString + ") on duplicate key update contactGroup_id=values(contactGroup_id);");
		
		query.setParameter("cId", contact.getId());
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public int removeAllGroupFromContact(Contact contact){
		
		int deletedEntities = 0;
		Query query = getSession().createQuery("delete Contact_ContactGroup where contact = :contact");
		query.setParameter("contact", contact);
		deletedEntities = query.executeUpdate();
		return deletedEntities;
		
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public int removeGroupsFromContact(Contact contact, List<ContactGroup> contactGroupList){
		int deletedEntities = 0;
		
		List<Long> groupIds = new ArrayList<Long>();
		for (ContactGroup contactGroup : contactGroupList) {
			groupIds.add(contactGroup.getId());
		}
		
		Query query = getSession().createQuery("delete Contact_ContactGroup where contact.id = :contact and contactGroup.id in (:removeGroupIds)" );
		query.setParameter("contact", contact.getId());
		query.setParameterList("removeGroupIds", groupIds);
		deletedEntities = query.executeUpdate();
		return deletedEntities;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addContactsToGroup(List<Contact> addedContactList, ContactGroup contactGroup) {
		if (addedContactList == null || addedContactList.isEmpty()){
			return;
		}
		
		StringBuffer contacts = new StringBuffer("'");
		for (Contact contact : addedContactList) {
			contacts.append(contact.getId()).append("','");
		}
		String contactString = contacts.toString();
		contactString = contactString.substring(0, contactString.length() - 2);
		
		Query query = getSession().createSQLQuery("insert into contact_contactgroup (contact_id, contactGroup_id) select c.id, :cgId from contact c " +
				"where c.id in (" + contactString + ") on duplicate key update contact_id=values(contact_id);");
		
		query.setParameter("cgId", contactGroup.getId());
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public int removeAllContactsFromGroup(ContactGroup contactGroup) {
		int deletedEntities = 0;
		Query query = getSession().createQuery("delete Contact_ContactGroup where contactGroup = :contactGroup");
		query.setParameter("contactGroup", contactGroup);
		deletedEntities = query.executeUpdate();
		return deletedEntities;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void removeContactsFromGroup(ContactGroup contactGroup, List<Contact> removedContactList) {
		
		List<Long> contactIds = new ArrayList<Long>();
		for (Contact contact : removedContactList) {
			contactIds.add(contact.getId());
		}
		
		Query query = getSession().createQuery("delete Contact_ContactGroup where contactGroup.id = :contactGroup and contact.id in (:removeContactIds)" );
		query.setParameter("contactGroup", contactGroup.getId());
		query.setParameterList("removeContactIds", contactIds);
		query.executeUpdate();
	}
}
