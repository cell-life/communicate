package org.celllife.mobilisr.client;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.client.impl.MobilisrClientImpl;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

@Ignore
public class CampaignServiceIntegrationTest {
	
//	private static final String URL = "http://127.0.0.1:8888";
	private static final String URL = "http://dev.cell-life.org/mobilisr";
	protected static final String USERNAME = "admin";
	protected static final String PASSWORD = "admin";
	private MobilisrClientImpl client;
	
	@Before
	public void setup() throws Exception {
		ValidatorFactoryImpl vfactory = new ValidatorFactoryImpl();
		vfactory.setCountryRules(Arrays.asList(new MsisdnRule("SA", "27", "^27[1-9][0-9]{8}$")));
		client = new MobilisrClientImpl(URL, USERNAME, PASSWORD, vfactory);
		SLF4JBridgeHandler.install();
	}
	
	@Test
	public void testGetCampaigns() {

		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> campaigns = campaignService.getCampaigns();
			Assert.assertNotNull(campaigns);
			System.out.println(campaigns.getTotal());
			for (CampaignDto camp : campaigns.getElements()) {
				System.out.println(camp);
			}
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignsByType() {
		
		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> result = campaignService.getCampaigns(CampaignType.DAILY);
			System.out.println(result);
			Assert.assertNotNull(result);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignsByStatus() {
		
		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> result = campaignService.getCampaigns(CampaignStatus.RUNNING);
			Assert.assertNotNull(result);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignsByTypeAndStatus() {
		
		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> result = campaignService.getCampaigns(CampaignType.FIXED, CampaignStatus.INACTIVE);
			Assert.assertNotNull(result);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignDetails() {
		long campaignId = 6L;
		
		CampaignService campaignService = client.getCampaignService();
		try {
			CampaignDto result = campaignService.getCampaignDetails(campaignId);
			Assert.assertNotNull(result);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testAddContactToCampaign() {
		long campaignId = 73L;
		ContactDto contact = DtoMockFactory._().on(ContactDto.class).create();
		System.out.println(contact.getMsisdn());
		CampaignService campaignService = client.getCampaignService();
		
		try {
			campaignService.addContactToCampaign(campaignId, contact);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testAddContactsToCampaign() {
		long campaignId = 6L;
		List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(10);

		CampaignService campaignService = client.getCampaignService();
		
		try {
			campaignService.addContactsToCampaign(campaignId, contacts);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testRemoveContactFromCampaign() {
		
		long campaignId = 4L;
		
		CampaignService campaignService = client.getCampaignService();
		try {
			campaignService.removeContactFromCampaign(campaignId, "27734567895");
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateNewCampaign() throws RestCommandException {
		
		CampaignDto campaign = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		System.out.println(campaign);
		campaign.setName(UUID.randomUUID().toString().substring(0,30));
		
		CampaignService campaignService = client.getCampaignService();
		try {
			long newCampaignId = campaignService.createNewCampaign(campaign);
			System.out.println(newCampaignId);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}


}