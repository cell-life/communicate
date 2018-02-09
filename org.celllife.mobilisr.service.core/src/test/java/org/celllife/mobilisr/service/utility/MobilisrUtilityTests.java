package org.celllife.mobilisr.service.utility;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.pconfig.model.RepeatInterval;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.gwttime.time.DateTime;
import org.junit.Test;
import org.quartz.CronExpression;

public class MobilisrUtilityTests {

	@Test
	public void testCalculateNumberOfMessages_zeroLength(){
		int amount = MobilisrUtility.calculateNumberOfMessages(0);
		Assert.assertEquals(0, amount);
	}
	
	@Test
	public void testCalculateNumberOfMessages_underMaxLength(){
		int amount = MobilisrUtility.calculateNumberOfMessages(159);
		Assert.assertEquals(1, amount);
	}
	
	@Test
	public void testCalculateNumberOfMessages_atMaxLength(){
		int amount = MobilisrUtility.calculateNumberOfMessages(160);
		Assert.assertEquals(1, amount);
	}
	
	@Test
	public void testCalculateNumberOfMessages_overMaxLenth(){
		int amount = MobilisrUtility.calculateNumberOfMessages(161);
		Assert.assertEquals(2, amount);
	}
	
	@Test
	public void testCalculateNumberOfMessages_multiPart(){
		int amount = MobilisrUtility.calculateNumberOfMessages(306);
		Assert.assertEquals(2, amount);
	}
	
	@Test
	public void testCalculateNumberOfMessages_multiPart2(){
		int amount = MobilisrUtility.calculateNumberOfMessages(307);
		Assert.assertEquals(3, amount);
	}
	
	@Test
	public void testCalculateTempReservedAmount(){
		double amount = MobilisrUtility.calculateMessageCost("123", 7);
		Assert.assertEquals(7d, amount);
	}
	
	@Test
	public void testGetHostName(){
		String hostname = MobilisrUtility.getHostname();
		Assert.assertFalse(hostname.isEmpty());
	}
	
	@Test
	public void testFindValuesForRegExp(){
		List<String> list = MobilisrUtility.findValuesForRegExp("123 456 789", "(\\d+)");
		Assert.assertEquals("123", list.get(0));
		Assert.assertEquals("456", list.get(1));
		Assert.assertEquals("789", list.get(2));
	}
	
	@Test
	public void testGetCronExpressionForScheduledReport_daily() throws ParseException{
		ScheduledPconfig sp = new ScheduledPconfig();
		sp.setIntervalCount(3);
		sp.setRepeatInterval(RepeatInterval.Daily);
		String cron = MobilisrUtility.getCronExpression(sp);
		CronExpression cronExpression = new CronExpression(cron);
		Assert.assertNotNull(cronExpression);
	}
	
	@Test
	public void testGetCronExpressionForScheduledReport_weekly() throws ParseException{
		ScheduledPconfig sp = new ScheduledPconfig();
		sp.setIntervalCount(3);
		sp.setRepeatInterval(RepeatInterval.Weekly);
		String cron = MobilisrUtility.getCronExpression(sp);
		CronExpression cronExpression = new CronExpression(cron);
		Assert.assertNotNull(cronExpression);
	}
	
	@Test
	public void testGetCronExpressionForScheduledReport_monthly() throws ParseException{
		ScheduledPconfig sp = new ScheduledPconfig();
		sp.setIntervalCount(3);
		sp.setRepeatInterval(RepeatInterval.Monthly);
		sp.setStartDate(new Date());
		String cron = MobilisrUtility.getCronExpression(sp);
		CronExpression cronExpression = new CronExpression(cron);
		Assert.assertNotNull(cronExpression);
	}
	
	@Test
	public void testRecalculateCampaignCostAndDuration() throws Exception{
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.FLEXI,
				CampaignStatus.INACTIVE, 10, 0, null);
		campaign.setWelcomeMsg("Welcome");
		
		CampaignMessage m1 = new CampaignMessage("Hello", new Date(), new Date(), campaign );
		m1.setMsgDay(1);
		
		CampaignMessage m2 = new CampaignMessage("Hello", new Date(), new Date(), campaign );
		m2.setMsgDay(10);
		
		MobilisrUtility.recalculateCostAndDuration(campaign, Arrays.asList(new CampaignMessage[]{m1,m2}));
		
		Assert.assertEquals(10, campaign.getDuration());
		Assert.assertEquals(3, campaign.getCost());
	}
	
	@Test
	public void testCronExprForDailyOccurence(){
		String cron = MobilisrUtility.cronExprForDailyOccurence(new DateTime(2011,12,9,17,6,19).toDate());
		Assert.assertEquals("19 6 19 ? * *", cron);
	}
}
