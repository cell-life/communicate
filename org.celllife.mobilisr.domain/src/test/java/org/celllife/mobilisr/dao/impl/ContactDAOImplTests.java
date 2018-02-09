package org.celllife.mobilisr.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.mobilisr.utilbean.ContactExportSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class ContactDAOImplTests extends AbstractDBTest {
	
	@Autowired
	private ContactDAO contactDao;
	
	@Autowired
	private ContactGroupDAO contactGroupDao;
	
	private Organization organization;

	private List<Contact> allContacts;

	private List<ContactGroup> allGroups;

	private int contactsInGroups;

	@Before
	public void beforeTest(){
		organization = getGeneralDao().findAll(Organization.class).get(1);
		allContacts = contactDao.searchByPropertyEqual(Contact.PROP_ORGANIZATION, organization);
		allGroups = contactGroupDao.searchByPropertyEqual(ContactGroup.PROP_ORGANIZATION, organization);
		HashSet<Contact> uniqueContactsInGroups = new HashSet<Contact>();
		for (ContactGroup group : allGroups) {
			uniqueContactsInGroups.addAll(group.getContacts());
		}
		contactsInGroups = uniqueContactsInGroups.size();
	}
	
	@Test
	public void testcountContactsInGroups_NullGroupList(){
		Long count = contactDao.countContactsInGroups(null);
		Assert.assertEquals(0, count.intValue());
	}
	
	@Test
	public void testcountContactsInGroups_EmptyGroupList(){
		Long count = contactDao.countContactsInGroups(new ArrayList<ContactGroup>());
		Assert.assertEquals(0, count.intValue());
	}
	
	@Test
	public void testcountContactsInGroups_NonEmptyGroupList(){
		List<ContactGroup> groupList = allGroups.subList(0, 1);
		Long count = contactDao.countContactsInGroups(groupList);
		Assert.assertEquals(groupList.get(0).getContacts().size(), count.intValue());
	}
	
	@Test
	public void testcountContactsInGroups_NonEmptyGroupList1(){
		List<ContactGroup> groupList = allGroups;
		Long count = contactDao.countContactsInGroups(groupList);
		Assert.assertEquals(contactsInGroups, count.intValue());
	}
	
	@Test
	public void testSaveContactForOrganization_newContact() throws Exception{
		ContactDAOImpl dao = TestUtils.getTargetObject(contactDao, ContactDAOImpl.class);
		Contact contact = dao.saveContactForOrganization(organization, new Contact("123", "MTN", "Bob", "Beach"));
		Assert.assertNotNull(contact.getId());
		Assert.assertEquals(organization, contact.getOrganization());
	}
	
	@Test
	public void testSaveContactForOrganization_existingContactDifferentOrg() throws Exception{
		ContactDAOImpl dao = TestUtils.getTargetObject(contactDao, ContactDAOImpl.class);
		Contact contact = getContact(organization, false,0);
		Assert.assertNotNull(contact);
		
		Contact savedContact = dao.saveContactForOrganization(organization, new Contact(contact.getMsisdn(), "MTN", "Bob", "Beach"));
		Assert.assertNotNull(savedContact.getId());
		Assert.assertFalse(contact.getId().equals(savedContact.getId()));
	}
	
	@Test
	public void testSaveContactForOrganization_existingContactSameOrg() throws Exception{
		ContactDAOImpl dao = TestUtils.getTargetObject(contactDao, ContactDAOImpl.class);
		Contact contact = getContact(organization, true,0);
		Assert.assertNotNull(contact);
		
		Contact savedContact = dao.saveContactForOrganization(organization, new Contact(contact.getMsisdn(), "MTN", "Bob", "Beach"));
		Assert.assertNotNull(savedContact.getId());
		Assert.assertTrue(contact.getId().equals(savedContact.getId()));
		
		contact = dao.searchByPropertyEqual(Contact.PROP_ID, contact.getId()).get(0);
		Assert.assertEquals(savedContact.getFirstName(), contact.getFirstName());
		Assert.assertEquals(savedContact.getLastName(), contact.getLastName());
		Assert.assertEquals(savedContact.getMobileNetwork(), contact.getMobileNetwork());
	}
	
	@Test
	public void testSaveContactForOrganization_newContactSameMSISDN() throws Exception{
		ContactDAOImpl dao = TestUtils.getTargetObject(contactDao, ContactDAOImpl.class);
		Contact contact = getContact(organization, true,0);
		Assert.assertNotNull(contact);
		
		Contact savedContact = dao.saveContactForOrganization(organization, new Contact(contact.getMsisdn(),organization));
		Assert.assertNotNull(savedContact.getId());
	}
	
	@Test
	public void testBatchSaveContact_allNewContacts() {
		List<ContactGroup> contactGroupList = allGroups.subList(0, 1);

		List<Contact> contactList = new ArrayList<Contact>();

		int numContacts = 1000;
		for (int i = 0; i < numContacts; i++) {
			Contact contact = new Contact("VIK " + i, null, null, null);
			contactList.add(contact);
		}

		contactDao.batchSaveContact(organization, contactList, contactGroupList);
		Search search = new Search();
		search.addFilterLike(Contact.PROP_MSISDN, "VIK%");
		int count = contactDao.count(search);
		Assert.assertEquals(numContacts, count);
	}
	
	@Test
	public void testBatchSaveContact_allCases() {
		List<ContactGroup> contactGroupList = new ArrayList<ContactGroup>();
		ContactGroup contactGroup = new ContactGroup("batchSaveContactTestGroup", "");
		contactGroup.setOrganization(organization);
		contactGroupDao.save(contactGroup);
		contactGroupList.add(contactGroup);
		
		List<Contact> contactList = new ArrayList<Contact>();

		contactList.add(getContact(organization, true,0));
		contactList.add(getContact(organization, false,0));
		contactList.add(new Contact(getContact(organization, false,1).getMsisdn(), null));
		contactList.add(new Contact(getContact(organization, true,1).getMsisdn(), null));
		contactList.add(new Contact(getContact(organization, true,2).getMsisdn(), organization));
		contactList.add(new Contact(getContact(organization, false,2).getMsisdn(), organization));
		// duplicate of previous
		contactList.add(new Contact(getContact(organization, false,2).getMsisdn(), organization));
		contactList.add(new Contact("2132456789", organization));
		contactList.add(new Contact("4657898798", null));
		// duplicate of previous
		contactList.add(new Contact("4657898798", null));
		
		contactDao.batchSaveContact(organization, contactList, contactGroupList);
		
		Search search = new Search();
		search.addFilterEqual(ContactGroup.PROP_GROUP_NAME, "batchSaveContactTestGroup");
		search.addFetch(ContactGroup.PROP_CONTACTS);
		
		ContactGroup group = contactGroupDao.searchUnique(search);
		List<Contact> contacts = group.getContacts();
		int duplicates = 2;
		Assert.assertEquals(contactList.size()-duplicates, contacts.size());
	}
	
	@Test
	public void testSearchByOrganizationAndMSISDN(){
		
		List<ContactGroup> contactGroupList = new ArrayList<ContactGroup>();
		ContactGroup contactGroup = new ContactGroup("testSearchByOrganizationAndMSISDN", "");
		contactGroup.setOrganization(organization);
		contactGroupDao.save(contactGroup);
		contactGroupList.add(contactGroup);
		
		List<Contact> contactList = new ArrayList<Contact>();
		contactList.add(new Contact("2732456789", organization));
		contactList.add(new Contact("2757898798", organization));
		
		contactDao.batchSaveContact(organization, contactList, contactGroupList);
		
		Contact searchedContact = contactDao.searchByOrganizationAndMSISDN(organization, "2732456789");		
		Assert.assertNotNull(searchedContact);
		
	}
	
	@Test
	public void testSearchByOrganizationAndMSISDN_nonexistant(){		
		
		Contact searchedContact = contactDao.searchByOrganizationAndMSISDN(organization, "2732456788");		
		Assert.assertNull(searchedContact);
		
	}
	
	@Test
	public void testSaveOrUpdate(){
		Long id = allContacts.get(0).getId();
		Contact contact = contactDao.find(id);
		contact.setMsisdn("new msisdn");
		contactDao.saveOrUpdate(contact);
		
		Contact saved = contactDao.find(id);
		Assert.assertEquals("new msisdn", saved.getMsisdn());
	}
	
	@Test
	public void testGetExportContacts() {
		List<ContactExportSummary> contacts = contactDao.getExportContactsForOrganization(organization, 0, 10);
		Assert.assertEquals("10 results returned", 10, contacts.size());
		ContactExportSummary contact = contacts.get(0);
		Assert.assertEquals("Firstname", "test org 00", contact.getFirstName());
		Assert.assertEquals("Lastname", "contact lName0", contact.getLastName());
		Assert.assertEquals("MSISDN", "MSISDN 0", contact.getMsisdn());
		Assert.assertEquals("Campaigns", "Program 0", contact.getCampaigns());
	}

	private Contact getContact(Organization org, boolean doesBelongToOrg, int firstResult) {
		Search search = new Search();
		if (doesBelongToOrg){
			search.addFilterEqual(Contact.PROP_ORGANIZATION, org);
		} else {
			search.addFilterNotEqual(Contact.PROP_ORGANIZATION, org);	
		}
		search.setMaxResults(1);
		search.setFirstResult(firstResult);
		List<Contact> contacts = contactDao.search(search);
		Contact contact = contacts.get(0);
		return contact;
	}

    @Test
    public void testGetExportContactsForGroup() {
        List<ContactExportSummary> contacts = contactDao.getExportContactsForGroup(allGroups.get(1), 0, 10);
        Assert.assertEquals("2 results returned", 2, contacts.size());
    }

    @Test
    public void testFindCampaignsForContact() {
        List<Long> campaignContacts = contactDao.findCampaignsForContact("MSISDN 0");
        Assert.assertEquals(1,campaignContacts.size());
    }

}
