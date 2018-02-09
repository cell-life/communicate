package org.celllife.mobilisr.api;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignType;
import org.junit.Test;

public class VersionsTest {
	
	@Test
	public void testCampaignType_apiValueOf_flexi(){
		CampaignType type = CampaignType.apiValueOf("GENERIC");
		Assert.assertEquals(CampaignType.FLEXI, type);
	}
	
	@Test
	public void testCampaignType_apiName_flexi(){
		String name = CampaignType.FLEXI.apiName(ApiVersion.v1);
		Assert.assertEquals("GENERIC", name);
		
		name = CampaignType.FLEXI.apiName(ApiVersion.getLatest());
		Assert.assertEquals(CampaignType.FLEXI.name(), name);
	}
	
	@Test
	public void testCampaignType_apiValueOf_daily(){
		CampaignType type = CampaignType.apiValueOf("RELATIVE");
		Assert.assertEquals(CampaignType.DAILY, type);
	}
	
	@Test
	public void testCampaignType_apiName_daily(){
		String name = CampaignType.DAILY.apiName(ApiVersion.v1);
		Assert.assertEquals("RELATIVE", name);
		
		name = CampaignType.DAILY.apiName(ApiVersion.getLatest());
		Assert.assertEquals(CampaignType.DAILY.name(), name);
	}

}
