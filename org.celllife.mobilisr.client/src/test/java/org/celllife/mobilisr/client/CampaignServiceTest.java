package org.celllife.mobilisr.client;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.celllife.mobilisr.test.ListRequestHandler;
import org.celllife.mobilisr.test.RestRequestHandler;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

public class CampaignServiceTest extends BaseHttpTest {

	@Test
	public void testGetCampaigns() {
		registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns", CampaignDto.class);

		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> campaigns = campaignService.getCampaigns();
			Assert.assertNotNull(campaigns);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignsByType() {
		registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns?type=FIXED", CampaignDto.class);
		
		CampaignService campaignService = client.getCampaignService();
		try {
			PagedListDto<CampaignDto> result = campaignService.getCampaigns(CampaignType.FIXED);
			Assert.assertNotNull(result);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetCampaignsByStatus() {
		registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns?status=RUNNING", CampaignDto.class);
		
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
		registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns?type=FIXED&status=INACTIVE", CampaignDto.class);
		
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
		long campaignId = 13L;
		registerHandler("/api/"+ApiVersion.getLatest()+"/campaigns/" + campaignId, CampaignDto.class);
		
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
		long campaignId = 7L;
		ContactDto contact = DtoMockFactory._().on(ContactDto.class).create();

		ListRequestHandler handler = registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns/" + campaignId + "/contacts/", ErrorDto.class);
		handler.setReturnSize(0);
		handler.setSuccessCode(Status.OK.getStatusCode());
		handler.setExpectedMethod("POST");
		
		CampaignService campaignService = client.getCampaignService();
		
		try {
			campaignService.addContactToCampaign(campaignId, contact);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testAddContactToCampaign_badMsisdn() {
		long campaignId = 7L;
		ContactDto contact = DtoMockFactory._().on(ContactDto.class).create();
		contact.setMsisdn("bad msisdn");
		
		ListRequestHandler handler = registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns/" + campaignId + "/contacts/", ErrorDto.class);
		handler.setReturnSize(1);
		handler.setSuccessCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
		handler.setExpectedMethod("POST");
		
		CampaignService campaignService = client.getCampaignService();
		
		try {
			campaignService.addContactToCampaign(campaignId, contact);
			Assert.fail("Expected exception");
		} catch (RestCommandException e) {
			PagedListDto<ErrorDto> errors = e.getErrors();
			Assert.assertNotNull(errors);
			Assert.assertEquals(1, errors.size());
		}
	}
	
	@Test
	public void testAddContactsToCampaign() {
		long campaignId = 7L;
		List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(10);

		ListRequestHandler handler = registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns/" + campaignId + "/contacts/", ErrorDto.class);
		handler.setReturnSize(0);
		handler.setSuccessCode(Status.OK.getStatusCode());
		handler.setExpectedMethod("POST");
		
		CampaignService campaignService = client.getCampaignService();
		
		try {
			campaignService.addContactsToCampaign(campaignId, contacts);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	

	@Test
	public void testRemoveContactFromCampaign() {
		
		long campaignId = 12L;
		ContactDto contact = DtoMockFactory._().on(ContactDto.class).create();
		
		RestRequestHandler handler = registerHandler("/api/"+ApiVersion.getLatest()+"/campaigns/" + campaignId + "/contacts/" + contact.getMsisdn(), ContactDto.class);
		handler.setExpectedMethod("DELETE");
		handler.setSuccessCode(HttpStatus.SC_NO_CONTENT);

		CampaignService campaignService = client.getCampaignService();
		try {
			campaignService.removeContactFromCampaign(campaignId, contact.getMsisdn());
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateNewCampaign() throws RestCommandException {
		long campaignId = 12L;
		CampaignDto campaign = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		campaign.setName(UUID.randomUUID().toString().substring(0,30));
		
		ListRequestHandler handler = registerListHandler("/api/"+ApiVersion.getLatest()+"/campaigns", ErrorDto.class);
		handler.setExpectedMethod("POST");
		handler.setSuccessCode(Status.CREATED.getStatusCode());
		handler.setReturnSize(0);
		handler.addReturnHeader("Location", "http://localhost/campaigns/" + campaignId);
		
		CampaignService campaignService = client.getCampaignService();
		try {
			long createdCampaignId = campaignService.createNewCampaign(campaign);
			Assert.assertEquals(campaignId, createdCampaignId);
		} catch (RestCommandException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}


}