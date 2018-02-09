package org.celllife.mobilisr.domain.mock;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.util.MobilisrDomainUtility;
import org.junit.Test;

public class MockPopulatorTest {
	
	@Test
	public void testCampaignMessage(){
		List<CampaignMessage> create = DomainMockFactory._().on(CampaignMessage.class).create(7);
		Assert.assertEquals(1, create.get(0).getMsgSlot());
		Assert.assertEquals(2, create.get(1).getMsgSlot());
		Assert.assertEquals(3, create.get(2).getMsgSlot());
		Assert.assertEquals(1, create.get(3).getMsgSlot());
		Assert.assertEquals(2, create.get(4).getMsgSlot());
		Assert.assertEquals(3, create.get(5).getMsgSlot());
		Assert.assertEquals(1, create.get(6).getMsgSlot());
		
		Date now = new Date();
		Assert.assertEquals(0, MobilisrDomainUtility.getDaysBetween(now, create.get(0).getMsgDate()));
		Assert.assertEquals(0, MobilisrDomainUtility.getDaysBetween(now, create.get(1).getMsgDate()));
		Assert.assertEquals(0, MobilisrDomainUtility.getDaysBetween(now, create.get(2).getMsgDate()));
		Assert.assertEquals(1, MobilisrDomainUtility.getDaysBetween(now, create.get(3).getMsgDate()));
		Assert.assertEquals(1, MobilisrDomainUtility.getDaysBetween(now, create.get(4).getMsgDate()));
		Assert.assertEquals(1, MobilisrDomainUtility.getDaysBetween(now, create.get(5).getMsgDate()));
		Assert.assertEquals(2, MobilisrDomainUtility.getDaysBetween(now, create.get(6).getMsgDate()));
	}

}
