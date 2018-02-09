package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;

public class ContactsServiceImplTests extends AbstractServiceTest {

	@Autowired
	private ContactsService crudContactsService;

	@Autowired
	private ContactDAO contactDAO;
	
	@Autowired
	private OrganizationDAO organizationDAO;
	
	@Autowired
	private MobilisrGeneralDAO genDAO;

	private Organization getOrgForTest(String orgName){
		Search search = new Search();
		search.addFilterEqual(Organization.PROP_NAME, orgName);
		search.addFetch(Organization.PROP_CONTACTS);
		Organization organization = organizationDAO.searchUnique(search);
		return organization;
	}
	
	@Test
	public void testListAllContactsOrderAsc() {

		PagingLoadConfig plc = new BasePagingLoadConfig(0, 10);

		Organization organization = getOrgForTest("test org 0");
		PagingLoadResult<Contact> list = crudContactsService.listAllContactsForOrganization(organization, plc);
		Assert.assertEquals(10, list.getTotalLength());
		Assert.assertEquals(10, list.getData().size());
		List<Contact> contactList = list.getData();
		Assert.assertEquals("test org 00", contactList.get(0).getFirstName());
		Assert.assertEquals("test org 09", contactList.get(9).getFirstName());
		Assert.assertEquals(10, list.getTotalLength());
		Assert.assertEquals(10, list.getData().size());

	}
	
	@Test
	public void testListContactsByGroup() {

		PagingLoadConfig plc = new BasePagingLoadConfig(0, 10);

		Organization organization = getOrgForTest("test org 0");
		Contact c1 = new Contact("27821231234",organization);
		Contact c2 = new Contact("27821231235",organization);
		Contact c3 = new Contact("27821231236",organization);
		
		genDAO.save(c1);
		genDAO.save(c2);
		genDAO.save(c3);
		
		ContactGroup g1 = new ContactGroup("Group 1", "group 1 desc");
		g1.setOrganization(organization);
		
		List<Contact> contactsForGroup1 = new ArrayList<Contact>();
		
		contactsForGroup1.add(c3);
		contactsForGroup1.add(c2);
		
		g1.setPersons(contactsForGroup1);
		
		genDAO.save(g1);
		
		
		PagingLoadResult<Contact> list = crudContactsService.listAllContactsForGroup(g1,plc);

		Assert.assertEquals(list.getTotalLength(),2);
		// check actual data including ordering
		Assert.assertEquals(c2.getMsisdn(), list.getData().get(0).getMsisdn());
		Assert.assertEquals(c3.getMsisdn(), list.getData().get(1).getMsisdn());

		
		
	}

	/**
	 * Test transaction isolation (should be able to find contact after saving
	 * it if transactions are not isolated)
	 */
	@Test
	public void testAddAndFindContact() {
		Organization organization = getOrgForTest("test org 0");

		Contact contact = new Contact("078512010d11", null, "Vikram123","Bindal");
		contact.setOrganization(organization);
		contactDAO.save(contact);
		Long id = contact.getId();

		Contact find = contactDAO.find(id);
		Assert.assertNotNull(find);
	}

	@Test
	public void testSaveContactDuplicateMSISDNNotInOrg() throws UniquePropertyException, MsisdnFormatException {

		Organization organization = getOrgForTest("test org 0");

		Search s = new Search(Contact.class);
		s.addFilterNotEqual(Contact.PROP_ORGANIZATION, organization);
		Contact tempContact = (Contact) contactDAO.search(s).get(0);
		tempContact.setMsisdn("27785121212");
		contactDAO.saveOrUpdate(tempContact);
		
		Contact contact = new Contact("27785121212", null, "Vikram123","Bindal");
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);

		organization = getOrgForTest("test org 0");
		Assert.assertEquals(11, organization.getContacts().size());
	}
	
	@Test
	public void testUpdateContactWithNewOrgForContact() throws UniquePropertyException, MsisdnFormatException{
	
		Organization organization = getOrgForTest("test org 0");
		
		Contact contact = contactDAO.findAll().get(0);
		contact.setMsisdn("27785121212");
		contactDAO.saveOrUpdate(contact);
		contact.setFirstName("Updated MSISDN");
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		Search savedSearch = new Search();
		savedSearch.addFilterEqual(Contact.PROP_MSISDN, "27785121212");
		Contact savedContact = contactDAO.searchUnique(savedSearch);
		
		organization = getOrgForTest("test org 0");
		Assert.assertEquals(10, organization.getContacts().size());
		Assert.assertEquals("Updated MSISDN", savedContact.getFirstName());
	}
	
	@Test
	public void testUpdateContactWithExistingOrgForContact() throws UniquePropertyException, MsisdnFormatException{
	
		Organization organization = getOrgForTest("test org 0");

		int numofContacts = organization.getContacts().size();
		
		Contact contact = contactDAO.findAll().get(0);
		contact.setMsisdn("27785121212");
		contactDAO.saveOrUpdate(contact);
		contact.setFirstName("Updated MSISDN");
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		
		Search savedSearch = new Search();
		savedSearch.addFilterEqual(Contact.PROP_MSISDN, "27785121212");
		Contact savedContact = contactDAO.searchUnique(savedSearch);
		
		Assert.assertNotNull(savedContact);
		Assert.assertEquals(numofContacts, organization.getContacts().size());
		Assert.assertEquals("Updated MSISDN", savedContact.getFirstName());
	}
	
	@Test(expected=UniquePropertyException.class)
	public void testNewDuplicateContactForSameOrg() throws UniquePropertyException, MsisdnFormatException{
	
		//Get the organization
		Organization organization = getOrgForTest("test org 0");

		//Create a new contact and save it for the organization
		Contact contact = new Contact("27785120101", null, "Dummy", null);
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		organization = getOrgForTest("test org 0");
		Long firstSavedContactId = contact.getId();
		Assert.assertEquals(11, organization.getContacts().size());
		Assert.assertNotNull(firstSavedContactId);
		
		//Create a new contact with the same mobile number and save it
		contact = new Contact("27785120101", null, "Vikram", "Bindal");
		contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		contact = crudContactsService.saveOrUpdateContact(organization, contactModel);
	}
	
	/**
	 * This test adds the same contact with same details to two different orgs.
	 * It then updates contact detail of one org and checks if other is also updated or not.
	 * The other one shouldn't be updated !!.
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testAddUpdateContactSameDetailToDiffOrg() throws UniquePropertyException, MsisdnFormatException{
		
		//Get the organization
		Organization org1 = getOrgForTest("test org 0");

		int numOfContactsOrg1 = org1.getContacts().size();
		
		//Get the organization
		Organization org2 = getOrgForTest("test org 5");

		int numOfContactsOrg2 = org2.getContacts().size();
		
		//Contact objects with same detail, cant use the same object otherwise
		//It will have an id for the first saved and then this won't work... as it 
		//will not simulate real example
		Contact contact1 = new Contact("27837995985", null, "John", null);
		Contact contact2 = new Contact("27837995985", null, "John", null);

		//Save the contact for each organization
		ContactContactGroupViewModel contactModel1 = new ContactContactGroupViewModel(contact1, null, null, false, false);
		Contact contactForOrg1 = crudContactsService.saveOrUpdateContact(org1, contactModel1);
		
		org1 = getOrgForTest("test org 0");
		Assert.assertNotNull(contactForOrg1.getId());
		Assert.assertEquals(numOfContactsOrg1+1, org1.getContacts().size());
		
		ContactContactGroupViewModel contactModel2 = new ContactContactGroupViewModel(contact2, null, null, false, false);
		Contact contactForOrg2 = crudContactsService.saveOrUpdateContact(org2, contactModel2);
		
		org2 = getOrgForTest("test org 5");
		Assert.assertNotNull(contactForOrg2.getId());
		Assert.assertEquals(numOfContactsOrg2+1, org2.getContacts().size());
		
		//Check if two different inserts were made
		Assert.assertNotSame(contactForOrg1.getId(), contactForOrg2.getId());
		
		//Update one contact
		contactForOrg2.setLastName("Doe");
		ContactContactGroupViewModel contactModel3 = new ContactContactGroupViewModel(contactForOrg2, null, null, false, false);
		Contact updatedContact = crudContactsService.saveOrUpdateContact(org2, contactModel3);

		//Check, if org1 still has 41 contacts
		Assert.assertEquals(numOfContactsOrg2+1, org2.getContacts().size());
		//Check is basic details are correctly updated
		Assert.assertEquals(contactForOrg2.getId(), updatedContact.getId());
		Assert.assertEquals("John", updatedContact.getFirstName());
		Assert.assertEquals("Doe", updatedContact.getLastName());
		Assert.assertEquals(contactForOrg2.getMsisdn(), updatedContact.getMsisdn());
		
	}
	
	@Test
	public void testNewContactAdd() throws UniquePropertyException, MsisdnFormatException{
		
		Organization organization = getOrgForTest("test org 5");

		int numOfContacts = organization.getContacts().size();
		
		Contact contact = new Contact("27785120101", null, null, null);
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		organization = getOrgForTest("test org 5");
		
		Assert.assertNotNull(contact.getId());
		Assert.assertEquals(numOfContacts+1, organization.getContacts().size());
	}

    @Test
    public void testListAllCampaignsForContact() {

        List<Long> campaignContacts = contactDAO.findCampaignsForContact("27784564509");
        Assert.assertEquals(2,campaignContacts.size());

        campaignContacts = contactDAO.findCampaignsForContact("27724194157");
        Assert.assertEquals(0,campaignContacts.size());

	}
}
