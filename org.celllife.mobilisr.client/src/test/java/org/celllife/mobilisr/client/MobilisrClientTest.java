package org.celllife.mobilisr.client;

import junit.framework.Assert;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.celllife.mobilisr.test.BasicAuthHandler;
import org.celllife.mobilisr.test.RestRequestHandler;
import org.junit.Test;

public class MobilisrClientTest extends BaseHttpTest {

	@Test
	public void testWithAuthentication() {
		String requestUrl = "/api/"+ApiVersion.getLatest()+"/campaigns/3";

		BasicAuthHandler handler = new BasicAuthHandler(new RestRequestHandler(requestUrl,
				CampaignDto.class));
		handler.setAcceptedAuthToken(USERNAME + ":" + PASSWORD);
		getLocalServer().register(requestUrl, handler);

		CampaignService service = client.getCampaignService();
	
		try {
			CampaignDto campaign = service.getCampaignDetails(3L);
			Assert.assertNotNull(campaign);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

}