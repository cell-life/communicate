package org.celllife.mobilisr.client;

import junit.framework.Assert;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.junit.Test;

public class ContactServiceTest extends BaseHttpTest {

	@Test
	public void testUpdateContactDetails() {

		String originalMsisdn = "+27841234567";
		
		registerHandler("PUT", "/api/"+ApiVersion.getLatest()+"/contacts/" + originalMsisdn, ContactDto.class);

		ContactService contactService = client.getContactService();
		
		try {
			contactService.updateContactDetails(originalMsisdn, new ContactDto());
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
}