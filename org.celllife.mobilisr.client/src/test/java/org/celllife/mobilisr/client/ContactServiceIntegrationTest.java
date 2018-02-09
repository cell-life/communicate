package org.celllife.mobilisr.client;

import java.util.Arrays;

import junit.framework.Assert;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.client.impl.MobilisrClientImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ContactServiceIntegrationTest {
	
	private static final String URL = "http://127.0.0.1:8888";
//	private static final String URL = "http://dev.cell-life.org/mobilisr";
	protected static final String USERNAME = "admin";
	protected static final String PASSWORD = "admin";
	private MobilisrClientImpl client;
	
	@Before
	public void setup() throws Exception {
		ValidatorFactoryImpl vfactory = new ValidatorFactoryImpl();
		vfactory.setCountryRules(Arrays.asList(new MsisdnRule("SA", "27", "^27[1-9][0-9]{8}$")));
		client = new MobilisrClientImpl(URL, USERNAME, PASSWORD, vfactory);
	}
	
	@Test
	public void testUpdateContact() {

		ContactService service = client.getContactService();
		try {
			ContactDto dto = new ContactDto();
			dto.setMsisdn("27722310095");
			service.updateContactDetails("27722310096", dto);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
}