package org.celllife.mobilisr.service.integrationtest;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.trg.search.Search;

public class CSVBatchImportIntegrationTests extends AbstractServiceTest {

	@Autowired
	private ContactsService crudContactsService;

	@Autowired
	private OrganizationDAO organizationDAO;

	@Autowired
	private ContactDAO contactDAO;

	@Test(timeout=20000)
	public void testCsvBatchSave() throws Exception {
		Search search = new Search();
		search.addFilterEqual(Organization.PROP_NAME, "test org 0");
		search.addFetch(Organization.PROP_CONTACT_GROUPS);
		Organization organization = organizationDAO.searchUnique(search);
		List<ContactGroup> contactGroups = organization.getContactGroups();
		List<ContactGroup> subList = contactGroups.subList(0, 5);

		Assert.assertEquals(5, subList.size());
		String filePath = this.getClass().getClassLoader().getResource("name-msisdn-gen.csv").getFile();
		int lines = countLines(filePath);
		
		StopWatch sw = new StopWatch();

		List<String> fieldOrder = new ArrayList<String>();
		fieldOrder.add(Contact.PROP_FIRST_NAME);
		fieldOrder.add(Contact.PROP_MSISDN);
		sw.start();
		crudContactsService.saveCSVContacts(fieldOrder, filePath, organization, subList);
		sw.stop();
		
		int count = 0;
		do {
			search = new Search();
			search.addFilterLike(Contact.PROP_FIRST_NAME, "%testCsvBatchSave");
			count = contactDAO.count(search);
			Thread.sleep(1000);
		} while (count < lines - 1);
		
		Assert.assertEquals(lines - 1, count);
		System.out.println(sw.shortSummary());
	}

	public int countLines(String filename) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		int cnt = 0;
		@SuppressWarnings("unused")
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {
		}

		cnt = reader.getLineNumber();
		reader.close();
		return cnt;
	}
}
