package org.celllife.mobilisr.service.impl;

import java.util.List;

import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;

public class ContactGroupAndContactBatchTests extends AbstractServiceTest {

	@Autowired
	private ContactsService crudContactsService;

	@Autowired
	private OrganizationDAO organizationDAO;

	@Test
	public void testBulkAddAllGroupToNewContact() throws UniquePropertyException, MsisdnFormatException {

		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACT_GROUPS);
		Organization organization = organizationDAO.searchUnique(search);

		List<ContactGroup> contactGroupList = organization.getContactGroups();
		int numOfGroups = contactGroupList.size();

		// Create a new contact
		int numOfTimes = 5;

		for (int i = 0; i < numOfTimes; i++) {

			Contact contact = new Contact("2778456129" + i, null, null, null);

			ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
					contact, null, null, true, false);
			// Save the contact with the group configuration
			contact = crudContactsService.saveOrUpdateContact(organization,	contactModel);

			Search s = new Search(Contact_ContactGroup.class);
			s.addFilterEqual(Contact_ContactGroup.PROP_CONTACT, contact);
			int count = getGeneralDao().count(s);
			Assert.assertEquals(numOfGroups, count);
		}
	}

	@Test
	public void testBulkAddAllWithRemoveGroupToNewContact() throws UniquePropertyException, MsisdnFormatException {

		Search search = new Search();
		search.addFilterEqual("name", "test org 0");
		search.addFetch(Organization.PROP_CONTACT_GROUPS);
		Organization organization = organizationDAO.searchUnique(search);

		List<ContactGroup> contactGroupList = organization.getContactGroups();
		int numOfGroups = contactGroupList.size();

		Contact contact = new Contact("27198877666", null, null, null);

		PagingLoadConfig loadConfig = new BasePagingLoadConfig(0, 20);
		PagingLoadResult<ContactGroup> contactListResult = crudContactsService
				.listAllGroupsForOrganization(organization, loadConfig);
		List<ContactGroup> removeContactGroupList = contactListResult.getData();

		// Save the contact with the group configuration
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
				contact, null, removeContactGroupList, true, false);

		contact = crudContactsService.saveOrUpdateContact(organization,	contactModel);

		stopWatch.stop();
		System.out.println("Total time :" + stopWatch.shortSummary());

		Search s = new Search(Contact_ContactGroup.class);
		s.addFilterEqual(Contact_ContactGroup.PROP_CONTACT, contact);
		int count = getGeneralDao().count(s);
		Assert.assertEquals(numOfGroups-removeContactGroupList.size(), count);
	}
}
