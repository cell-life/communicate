package org.celllife.mobilisr.service.impl;

import java.util.List;

import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ExpectedException;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class ContactGroupServiceImplTests extends AbstractServiceTest {

	@Autowired
	private ContactsService crudContactsService;
	
	@Autowired
	private OrganizationDAO organizationDAO;
	
	/**
	 * The following test simply creates a new group with an organization and saves it in the DB
	 * @throws UniquePropertyException 
	 */
	@Test
	public void testAddNewGroup() throws UniquePropertyException{
		
		//Get the org
		List<Organization> orgList = organizationDAO.searchByPropertyEqual(Organization.PROP_NAME, "test org 0");
		Organization organization = orgList.get(0);
		
		//Create a new group
		ContactGroup contactGroup = new ContactGroup("Dummy Group", "My first dummy group");
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, null, null, false, false);
		
		//Save the group
		contactGroup = crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Check for following
		Assert.assertNotNull(contactGroup.getId());
		Assert.assertEquals(11, organization.getContactGroups().size());
		
	}
	
	/**
	 * This test, saves a group with an org in DB and then tries to create a new group with same name as previous group
	 * with no description and checks if new record gets created or not (It shouldn't) and if the group gets updated or not
	 * (With new decription null, it shouldn't as previous group had a description)
	 * @throws UniquePropertyException 
	 */
	@Test
	@ExpectedException(value=UniquePropertyException.class)
	public void testAddNewUpdateGroupNoDescriptionUpdate() throws UniquePropertyException{
		
		//Get the org
		List<Organization> orgList = organizationDAO.searchByPropertyEqual(Organization.PROP_NAME, "test org 0");
		Organization organization = orgList.get(0);
		
		//Create a new group
		ContactGroup contactGroup = new ContactGroup("Dummy Group", "My first dummy group");
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, null, null, false, false);
		
		//Save the group
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Check for following
		Assert.assertNotNull(contactGroup.getId());
		Assert.assertEquals(11, organization.getContactGroups().size());
		
		//Create a new group with same name as previous one
		ContactGroup newContactGroup = new ContactGroup("Dummy Group", null);
		
		//Save the group
		contactModel.setParentObject(newContactGroup);
		newContactGroup = crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Check for following
		Assert.assertEquals(contactGroup.getId(), newContactGroup.getId());
		Assert.assertEquals(11, organization.getContactGroups().size());
		Assert.assertEquals("My first dummy group", newContactGroup.getGroupDescription());
	}
	
	/**
	 * This test, saves a group with an org in DB and then tries to create a new group with same name as previous group
	 * with no description and checks if new record gets created or not (It shouldn't) and if the group gets updated or not
	 * (With new description update)
	 * @throws UniquePropertyException 
	 */
	@Test
	@ExpectedException(value=UniquePropertyException.class)
	public void testAddNewUpdateGroupDescriptionUpdate() throws UniquePropertyException{
		
		//Get the org
		List<Organization> orgList = organizationDAO.searchByPropertyEqual(Organization.PROP_NAME, "test org 0");
		Organization organization = orgList.get(0);
		
		//Create a new group
		ContactGroup contactGroup = new ContactGroup("Dummy Group", "My first dummy group");
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, null, null, false, false);
		
		//Save the group
		crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Check for following
		Assert.assertNotNull(contactGroup.getId());
		Assert.assertEquals(11, organization.getContactGroups().size());
		
		//Create a new group with same name as previous one
		ContactGroup newContactGroup = new ContactGroup("Dummy Group", "Myyyy Preciousssssss");
		
		//Save the group
		contactModel.setParentObject(newContactGroup);
		newContactGroup = crudContactsService.saveOrUpdateContactGroup(organization, contactModel);
		
		//Check for following
		Assert.assertEquals(contactGroup.getId(), newContactGroup.getId());
		Assert.assertEquals(11, organization.getContactGroups().size());
		Assert.assertEquals("Myyyy Preciousssssss", newContactGroup.getGroupDescription());
	}
	
	/**
	 * This test searches for a particular group name for an organization and ensure
	 * proper results are returned with pagination in place
	 */
	@Test
	public void testSearchListPaginatedGroup(){
		
		//Get the organisation
		List<Organization> orgList = organizationDAO.searchByPropertyEqual(Organization.PROP_NAME, "test org 0");
		Organization organization = orgList.get(0);
		
		//Basic pagination loading config with searchField and SearchFieldValue
		//This should return one result from the DB as GroupName is "Test Group 02"
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 10);
		plc.set("fields", "groupName");
		plc.set("query", "roup 02");
		
		PagingLoadResult<ContactGroup> bplr = crudContactsService.listAllGroupsForOrganization(organization, plc);
		List<ContactGroup> searchResultList = bplr.getData();
		int totalCount = bplr.getTotalLength();
		
		Assert.assertEquals(1, totalCount);
		Assert.assertEquals("Test Group 02", searchResultList.get(0).getGroupName());
	}
	
	/**
	 * This test lists the groups for an organization with pagination
	 */
	@Test
	public void testListPaginatedGroup(){
		
		//Get the organisation
		List<Organization> orgList = organizationDAO.searchByPropertyEqual(Organization.PROP_NAME, "test org 0");
		Organization organization = orgList.get(0);
		
		//Basic pagination loading config with searchField and SearchFieldValue
		//This should return one result from the DB as GroupName is "Test Group 02"
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 5);
		plc.set("fields", null);
		plc.set("query", null);
		
		PagingLoadResult<ContactGroup> bplr = crudContactsService.listAllGroupsForOrganization(organization, plc);
		List<ContactGroup> searchResultList = bplr.getData();
		int totalCount = bplr.getTotalLength();
		
		Assert.assertEquals(10, totalCount);
		Assert.assertEquals(5, bplr.getData().size());
		Assert.assertEquals("Test Group 00", searchResultList.get(0).getGroupName());
		Assert.assertEquals("Test Group 04", searchResultList.get(4).getGroupName());
		
	}
}
