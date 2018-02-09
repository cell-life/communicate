package org.celllife.mobilisr.dao.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class ContactGroupDAOImplTests extends AbstractDBTest {

	@Autowired
	private ContactGroupDAO contactGroupDao;
	
	@Test
	public void testAddGroupToContact_contactAlreadyBelongsToGroup(){
		Search search = new Search(Contact_ContactGroup.class);
		search.addFetch(Contact_ContactGroup.PROP_CONTACT);
		search.addFetch(Contact_ContactGroup.PROP_CONTACT_GROUP);
		@SuppressWarnings("unchecked")
		List<Contact_ContactGroup> contact_groups = getGeneralDao().search(search); 
		
		assert (contact_groups != null && !contact_groups.isEmpty());
		
		Contact_ContactGroup contactContactGroup = contact_groups.get(0);
		Contact contact = contactContactGroup.getContact();
		ContactGroup contactGroup = contactContactGroup.getContactGroup();
		
		int sizeDiff = 0;
		
		testAddContactToGroup(contact.getId(), contactGroup.getId(), sizeDiff);
	}
	
	@Test
	public void testAddGroupToContact_contactDoesntBelongToGroup(){
		Search search = new Search(Contact_ContactGroup.class);
		search.addFetch(Contact_ContactGroup.PROP_CONTACT);
		search.addFetch(Contact_ContactGroup.PROP_CONTACT_GROUP);
		@SuppressWarnings("unchecked")
		List<Contact_ContactGroup> contact_groups = getGeneralDao().search(search); 
		
		assert (contact_groups != null && !contact_groups.isEmpty());
		
		Contact_ContactGroup contactContactGroup = contact_groups.get(0);
		ContactGroup contactGroup = contactContactGroup.getContactGroup();
		search = new Search(Contact.class);
		search.addFilterEmpty(Contact.PROP_CONTACT_GROUPS);
		Contact contact = (Contact) getGeneralDao().search(search).get(0);
		
		int sizeDiff = 1;
		
		testAddContactToGroup(contact.getId(), contactGroup.getId(), sizeDiff);
	}
	
	@Test
	public void testAddDuplicateContactsToGroup(){
		
		//Get a contact group
		Search search = new Search(ContactGroup.class);
		search.addFilterEqual(ContactGroup.PROP_GROUP_NAME, "Test Group 00");
		ContactGroup contactGroup = (ContactGroup) getGeneralDao().searchUnique(search);
		
		//Get all contacts
		List<Contact> contactList = getGeneralDao().findAll(Contact.class);
		
		//Save
		contactGroupDao.addContactsToGroup(contactList, contactGroup);
		
		//Fetch persistedRecords
		search = new Search(Contact_ContactGroup.class);
		search.addFilterEqual(Contact_ContactGroup.PROP_CONTACT_GROUP, contactGroup);
		@SuppressWarnings("unchecked")
		List<Contact_ContactGroup> persistedRecords = getGeneralDao().search(search);
		
		//Check all inserted
		Assert.assertEquals(contactList.size(), persistedRecords.size());
		
		//Create a new contact
		Organization org = getGeneralDao().getReference(Organization.class, 4l);
		Contact contact = new Contact("2425425322", org);
		getGeneralDao().save(contact);
		
		Assert.assertNotNull(contact.getId());
		//Add it to the contactList
		contactList.add(contact);
		
		contactGroupDao.addContactsToGroup(contactList, contactGroup);
		
		search = new Search(Contact_ContactGroup.class);
		search.addFilterEqual(Contact_ContactGroup.PROP_CONTACT_GROUP, contactGroup);
		@SuppressWarnings("unchecked")
		List<Contact_ContactGroup> persistedRecords2 = getGeneralDao().search(search);
		//Check all inserted
		Assert.assertEquals(contactList.size(), persistedRecords2.size());
		
	}

	private void testAddContactToGroup(long contactId, long contactGroupId, int sizeDiff) {
		List<Contact_ContactGroup> before = getGeneralDao().findAll(Contact_ContactGroup.class);
		
		Contact contact = getGeneralDao().find(Contact.class, contactId);
		ContactGroup contactGroup = getGeneralDao().find(ContactGroup.class, contactGroupId);
		//contactGroupDao.addGroupToContact(contact, contactGroup);
		
		List<Contact> contactList = new ArrayList<Contact>();
		contactList.add(contact);
		
		contactGroupDao.addContactsToGroup(contactList, contactGroup);
		
		List<Contact_ContactGroup> after = getGeneralDao().findAll(Contact_ContactGroup.class);
		Assert.assertEquals(before.size()+sizeDiff, after.size());
	}
}
