package org.celllife.mobilisr.service.impl;

import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;

public class ContactGroupAndContactTests extends AbstractServiceTest{

	@Autowired
	private ContactsService crudContactsService;

	@Autowired
	private ContactDAO contactDAO;
	
	@Autowired
	private ContactGroupDAO contactGroupDAO;
	
	@Autowired
	private OrganizationDAO organizationDAO;
	
	private Organization organization;
	
	@Before
	public void init(){
		Search search = new Search();
		search.addFilterEqual(Organization.PROP_NAME, "test org 0");
		organization = organizationDAO.searchUnique(search);
	}
	
	private Contact getContactById(Long id){
		Search search = new Search();
		search.addFilterEqual(Contact.PROP_ID, id);
		search.addFetch(Contact.PROP_CONTACT_GROUPS);
		Contact contact = contactDAO.searchUnique(search);
		return contact;
	}
	
	private ContactGroup getContactGroupById(Long id){
		Search search = new Search();
		search.addFilterEqual(ContactGroup.PROP_ID, id);
		search.addFetch(ContactGroup.PROP_CONTACTS);
		ContactGroup contactGroup = contactGroupDAO.searchUnique(search);
		return contactGroup;
	}
	
	/**
	 * 	The following test checks to ensure the following:
	 *  1. Records can be stores in the contact_contact group table
	 *  2. Duplicate records throw exception, hence not saved
	 */
	@Test(expected=ConstraintViolationException.class)
	public void testAddGroupToCustomContactTable(){
		
		Contact contact = contactDAO.findAll().get(0);
		
		ContactGroup contactGroup = new ContactGroup("Dummy 0", null);
		contactGroup.setOrganization(organization);
		contactGroupDAO.save(contactGroup);
		
		Contact_ContactGroup contactContactGroup = new Contact_ContactGroup(contact, contactGroup);
		getGeneralDao().save(contactContactGroup);
		
		contactContactGroup = new Contact_ContactGroup(contact, contactGroup);
		getGeneralDao().save(contactContactGroup);
		
		
	}
	
	/**
	 * The following test checks for the following:
	 * 1. User selected groups (from UI) simulated here as list of all groups can be saved for a new contact
	 * 2. Checks all the groups that were selected are added for that contact
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testAddGroupsToNewContact() throws UniquePropertyException, MsisdnFormatException{
		
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
				
		//Get list of all the groups
		List<ContactGroup> contactGroupList = contactGroupDAO.findAll();
		//Get the size we check for the size equality later on
		int numOfGroups = contactGroupList.size();
		
		//Create a new Contact Object and save it with particular organization
		Contact contact = new Contact("27785121212", null, null, null);
		
		//Create the contactModel obj with various attributes
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, contactGroupList, null, false, false);
		
		//Save the groups for the contact
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Fetch the contact
		Contact savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
	}
	
	@Test
	public void testListGroupsForContact() throws UniquePropertyException, MsisdnFormatException{
		testAddAllGroupsToNewContact();
		
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 5);
		
		Contact contact = contactDAO.searchByPropertyEqual(Contact.PROP_MSISDN, "27785121212").get(0);
		//Organization organization = getOrgForTest("test org 0");
		
		PagingLoadResult<ContactGroup> loadResult = crudContactsService.listAllGroupsForContact(organization, contact, plc);
		List<ContactGroup> data = loadResult.getData();
		
		Assert.assertEquals(5, data.size());
	}
	
	/**
	 * The following test checks for the following:
	 * 1. User selected groups (from UI) simulated here as list of all groups can be saved for a new contact
	 * 2. Some removed groups (from UI) simulated here as sublist of the group list
	 * 2. Checks all the groups that were selected are added for that contact
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testAddGroupsWithRemovedListToNewContact() throws UniquePropertyException, MsisdnFormatException{
		
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
				
		//Get list of all the groups
		List<ContactGroup> addGroupList = contactGroupDAO.findAll();
		//Get the size we check for the size equality later on
		int numOfAddedGroups = addGroupList.size();
		
		//Create a sub list that acts as removed list
		List<ContactGroup> removeGroupList = addGroupList.subList(0, 3);
		int numOfRemovedGroups = removeGroupList.size();
		
		//Create a new Contact Object and save it with particular organization
		Contact contact = new Contact("27785121212", null, null, null);
		
		//Create the contactModel obj with various attributes
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, addGroupList, removeGroupList, false, false);
		
		//Save the groups for the contact
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Fetch the contact
		Contact savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals((numOfAddedGroups-numOfRemovedGroups), savedContact.getContactGroups().size());
	}
	
	@Test(expected=UniquePropertyException.class)
	public void testCreateDuplicateContact() throws UniquePropertyException, MsisdnFormatException{
		Contact contact = contactDAO.findAll().get(0);
		contact.setMsisdn("27785121212");
		contactDAO.saveOrUpdate(contact);
		
		Search search = new Search();
		search.addFilterEqual(Contact.PROP_ID, contact.getId());
		search.addFetch(Contact.PROP_ORGANIZATION);
		contact = contactDAO.searchUnique(search);
		
		Contact copyContact = new Contact();
		String newFirstName = "new contact with same msisdn";
		copyContact.setFirstName(newFirstName);
		copyContact.setMsisdn(contact.getMsisdn());
		copyContact.setOrganization(contact.getOrganization());

		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(copyContact, null, null, false, false);
		crudContactsService.saveOrUpdateContact(contact.getOrganization(), contactModel);
	}
	
	/**
	 * The following test checks for the following:
	 * 1. Update contact by adding duplicate and new groups to it
	 * 2. Check that duplicate groups aren't added twice in the contact record
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testUpdateAddGroupsToContact() throws UniquePropertyException, MsisdnFormatException{
		
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
		
		List<ContactGroup> contactGroupList = contactGroupDAO.findAll();
		int numOfGroups = contactGroupList.size();
		
		//Create a new Contact
		Contact contact = new Contact("27785121212", null, null, null);
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, contactGroupList, null, false, false);
		
		//Save the groups for the contact
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Test
		//Fetch the contact
		Contact savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
		 
		//Saving second time round - shouldnt save it
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Test
		//Fetch the contact
		savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
		
		//Now we add a new group to the group list
		ContactGroup contactGroup = new ContactGroup("Dev Test 999", null);
		contactGroup.setOrganization(organization);
		contactGroupDAO.save(contactGroup);
		
		//Add the new group list to the added list
		contactGroupList.add(contactGroup);
				
		//Update contact model
		contactModel.setAddedChildList(contactGroupList);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Test
		//Fetch the contact
		savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals((numOfGroups+1), savedContact.getContactGroups().size());
	}
	
	/**
	 * The following test checks to ensure all the groups can be removed from the contact
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testRemoveAllGroupFromContact() throws UniquePropertyException, MsisdnFormatException{
		
		//Instead of coding everything from beginning, we invoke a test
		//that will save the groups for a particular contact
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
				
		//Get list of all the groups
		List<ContactGroup> contactGroupList = contactGroupDAO.findAll();
		//Get the size we check for the size equality later on
		int numOfGroups = contactGroupList.size();
		
		//Create a new Contact Object and save it with particular organization
		Contact contact = new Contact("27785121212", null, null, null);
		
		//Create the contactModel obj with various attributes
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, contactGroupList, null, false, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Fetch the contact
		Contact savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
		
		contactModel.setParentObject(savedContact);
		contactModel.setAddedChildList(null);
		contactModel.setRemovedChildList(null);
		contactModel.setAddAll(false);
		contactModel.setRemoveAll(true);
		//contactModel = new ContactModel(contact, null, null, false, true);
		
		//crudContactsService.addGroupsToContact(organization, contactModel);
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		savedContact = getContactById(contact.getId());
		
		Assert.assertEquals(0, savedContact.getContactGroups().size());
		
	}

	/**
	 * The following test checks to ensure all groups are removed from the contact
	 * and the new contacts that are to be added are still saved for the contact
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testRemoveAllGroupWithSomeNewAddedToContact() throws UniquePropertyException, MsisdnFormatException{
		//Instead of coding everything from beginning, we invoke a test
		//that will save the groups for a particular contact
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
				
		//Get list of all the groups
		List<ContactGroup> contactGroupList = contactGroupDAO.findAll();
		//Get the size we check for the size equality later on
		int numOfGroups = contactGroupList.size();
		
		//Create a new Contact Object and save it with particular organization
		Contact contact = new Contact("27785121212", null, null, null);
		
		//Create the contactModel obj with various attributes
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, contactGroupList, null, false, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		//Fetch the contact
		Contact savedContact = getContactById(contact.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
		
		List<ContactGroup> newAddList = contactGroupList.subList(0, 3);
		
		contactModel = new ContactContactGroupViewModel(contact, newAddList, null, false, true);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		savedContact = getContactById(contact.getId());
		
		Assert.assertEquals(newAddList.size(), savedContact.getContactGroups().size());
	}
	
	/**
	 * The following test checks to see if addAll is selected, then all the groups that exist in the system
	 * are added to the contact.
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testAddAllGroupsToNewContact() throws UniquePropertyException, MsisdnFormatException{
		
		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACT_GROUPS);
		Organization organization = organizationDAO.searchUnique(search);
		
		//Get all the groups
		List<ContactGroup> contactGroupList = organization.getContactGroups();
		int numOfGroups = contactGroupList.size();
		
		Contact contact = new Contact("27785121212", null, null, null);
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, true, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		List<Contact> savedContactList = contactDAO.searchByPropertyEqual(Contact.PROP_ID, contact.getId());
		Contact savedContact = savedContactList.get(0);
		
		Assert.assertNotNull(savedContact.getId());
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
	}
	
	/**
	 * The following test checks to see if addAll is selected, then all the groups that exist in the system
	 * are added to the contact. Doesnt take into account for a particular user !!
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	@Test
	public void testAddAllGroupsWithSomeRemovedToNewContact() throws UniquePropertyException, MsisdnFormatException{
		
		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACT_GROUPS);
		Organization organization = organizationDAO.searchUnique(search);
		
		//Get all the groups
		List<ContactGroup> contactGroupList = organization.getContactGroups();
		int numOfGroups = contactGroupList.size();
		
		Contact contact = new Contact("27785121212", null, null, null);
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, true, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		List<Contact> savedContactList = contactDAO.searchByPropertyEqual(Contact.PROP_ID, contact.getId());
		Contact savedContact = savedContactList.get(0);
		
		Assert.assertNotNull(savedContact.getId());
		Assert.assertEquals(numOfGroups, savedContact.getContactGroups().size());
		
		//Fetch some groups to remove
		List<ContactGroup> removedGroupList = contactGroupList.subList(0, 5);
		int numOfRemoved = removedGroupList.size();
		
		contactModel = new ContactContactGroupViewModel(contact, null, removedGroupList, true, false);
		
		crudContactsService.saveOrUpdateContact(organization, contactModel);
		//crudContactsService.addGroupsToContact(organization, contactModel);
		//To the already saved contact, we now want only some of the groups to be there, not all of them
		//crudContactsService.saveOrUpdateContact(organization, contactModel);
		
		search = new Search();
		search.addFilterEqual(Contact.PROP_ID, contact.getId());
		search.addFetch(Contact.PROP_CONTACT_GROUPS);
		savedContact = contactDAO.searchUnique(search);
		
		Assert.assertEquals(contact.getId(),savedContact.getId());
		Assert.assertEquals((numOfGroups-numOfRemoved), savedContact.getContactGroups().size());
	}
	
	/**
	 * The following test checks to see if all contacts for a given organization can be added to a group or not
	 * @throws UniquePropertyException 
	 */
	@Test
	public void testAddAllContactsToGroup() throws UniquePropertyException{
		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACTS);
		Organization organization = organizationDAO.searchUnique(search);
		
		ContactGroup cg = new ContactGroup("CodeGroup 101", null);
		cg.setOrganization(organization);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(cg, null, null, true, false);
		
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(cg.getId());
		
		Assert.assertEquals(organization.getContacts().size(), savedGroup.getContacts().size());
		
		
	}

	@Test
	public void testAddAllContactsWithSomeRemovedToNewGroup() throws UniquePropertyException{
		
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
				
		//Get list of all the groups
		Search search = new Search();
		search.addFilterEqual(Contact.PROP_ORGANIZATION, organization);
		List<Contact> contactList = contactDAO.search(search);
		//Get the size we check for the size equality later on
		int numOfAddedContacts = contactList.size();
		
		//Create a sub list that acts as removed list
		List<Contact> removeContactList = contactList.subList(0, 5);
		int numOfRemovedContacts = removeContactList.size();
		
		//Create a new Contact Object and save it with particular organization
		ContactGroup cg = new ContactGroup("DevTest 303", null);
		cg.setOrganization(organization);
		
		//Create the contactModel obj with various attributes
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(cg, null, removeContactList, true, false);
		
		//Save the groups for the contact
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Fetch the contact
		ContactGroup contactGroup = getContactGroupById(cg.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals((numOfAddedContacts-numOfRemovedContacts), contactGroup.getContacts().size());
	}

	/**
	 * Following test checks to see if all the contacts for the group can be removed or not
	 * @throws UniquePropertyException 
	 */
	@Test
	public void testRemoveAllContactsFromGroup() throws UniquePropertyException{
		
		//First we add all contacts to a group
		testAddAllContactsToGroup();
		
		Search search = new Search();
		search.addFilterEqual(ContactGroup.PROP_GROUP_NAME, "CodeGroup 101");
		ContactGroup contactGroup = contactGroupDAO.searchUnique(search);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, null, null, false, true);
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(contactGroup.getId());
		
		Assert.assertEquals(0, savedGroup.getContacts().size());
		
	}

	@Test
	public void testRemoveAllContactsFromGroupWithSomeNewAddedToGroup() throws UniquePropertyException{
		
		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACTS);
		Organization organization = organizationDAO.searchUnique(search);
		
		ContactGroup cg = new ContactGroup("CodeGroup 101", null);
		cg.setOrganization(organization);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(cg, null, null, true, false);
		
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(cg.getId());
		
		List<Contact> totalContactList = savedGroup.getContacts();
		List<Contact> addedContactList = totalContactList.subList(0, 5);
		
		contactModel = new ContactGroupContactViewModel(savedGroup, addedContactList, null, false, true);
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		savedGroup = getContactGroupById(cg.getId());
		
		Assert.assertEquals(addedContactList.size(), savedGroup.getContacts().size());
	}

	@Test
	public void testAddContactsToGroup() throws UniquePropertyException{
		
		//Organization organization = getOrgForTest("test org 0");
		
		List<Contact> contactList = contactDAO.findAll();
		
		ContactGroup contactGroup = new ContactGroup("DevTest 404", null);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, contactList, null, false, false);
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(contactGroup.getId());
		
		Assert.assertNotNull(savedGroup.getId());
		Assert.assertEquals(contactList.size(), savedGroup.getContacts().size());
		
	}
	
	@Test
	public void testRemoveContactsFromGroup() throws UniquePropertyException{
		
		testAddAllContactsToGroup();
		
		List<Contact> removeContactList = contactDAO.findAll();
		
		Search search = new Search();
		search.addFilterEqual(ContactGroup.PROP_GROUP_NAME, "CodeGroup 101");
		ContactGroup contactGroup = contactGroupDAO.searchUnique(search);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, null, removeContactList, false, false);
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(contactGroup.getId());
		
		Assert.assertEquals(0, savedGroup.getContacts().size());
	}
	
	@Test
	public void testAddUpdateContactsToGroup() throws UniquePropertyException{
		
		//Get the org
		//Organization organization = getOrgForTest("test org 0");
		
		List<Contact> contactList = contactDAO.findAll();
		int numOfContacts = contactList.size();
		
		ContactGroup cg = new ContactGroup("DevTest 505", null);
		
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(cg, contactList, null, false, false);
		
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		ContactGroup savedGroup = getContactGroupById(cg.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfContacts, savedGroup.getContacts().size());
		 
		//Saving second time round - shouldnt save it
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		savedGroup = getContactGroupById(cg.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals(numOfContacts, savedGroup.getContacts().size());
		
		//Now we add a new group to the group list
		Contact contact = new Contact("0785121212", null, null, null);
		contact.setOrganization(organization);
		contactDAO.save(contact);
		
		//Add the new group list to the added list
		contactList.add(contact);
				
		//Update contact model
		contactModel.setAddedChildList(contactList);
		
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Test
		//Fetch the contact
		savedGroup = getContactGroupById(cg.getId());
		
		//Check to ensure that all the records chosen were added for the contact
		Assert.assertEquals((numOfContacts+1), savedGroup.getContacts().size());
	}
}
