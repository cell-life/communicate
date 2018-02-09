package org.celllife.mobilisr.service.qrtz.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.qrtz.beans.RelativeCampJobRunner;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.gwttime.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.internal.matchers.Contains;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class QuartzServiceImplTest extends AbstractServiceTest {

	@Ignore
	private class DummyJob extends QuartzJobBean {

		@Override
		protected void executeInternal(JobExecutionContext context)
				throws JobExecutionException {
			// test job
		}
	}
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Autowired
	QuartzService quartzService;
	
	@Autowired
	@Qualifier("qrtzScheduler")
	private Scheduler scheduler;
	
	@Test
	public void testClearScheduleForCampaign() throws SchedulerException, ParseException{
		Campaign campaign = new Campaign();
		campaign.setName("test camp");
		campaign.setId(13L);
		
		scheduleArtificialJob(campaign, true);
		scheduleArtificialJob(campaign, true);
		
		quartzService.clearScheduleForCampaign(campaign);
		
		String group = campaign.getIdentifierString();
		List<String> triggers = quartzService.getTriggers(group);
		Assert.assertEquals(0, triggers.size());
	}
	
	@Test
	public void testClearScheduleForCampaign_anotherCampaign() throws SchedulerException, ParseException{
		Campaign campaign = new Campaign();
		campaign.setName("test camp");
		campaign.setId(13L);
		
		Campaign campaign2 = new Campaign();
		campaign2.setName("test camp2");
		campaign2.setId(11L);
		
		scheduleArtificialJob(campaign, true);
		scheduleArtificialJob(campaign, true);
		
		quartzService.clearScheduleForCampaign(campaign2);
		
		String group = campaign.getIdentifierString();
		List<String> triggers = quartzService.getTriggers(group);
		Assert.assertEquals(2, triggers.size());
		
		// cleanup
		quartzService.clearScheduleForCampaign(campaign);
	}
	
	@Test
	public void testRebuildRelativeCampaignSchedules(){
		Campaign campaign = new Campaign();
		campaign.setName("test camp");
		campaign.setId(13L);
		
		User user = new User();
		user.setId(3L);
		
		int num = 3;
		List<ContactMsgTime> triggerTimes = getMsgTimes(num, 0, true);
		
		quartzService.rebuildRelativeCampaignSchedules(campaign, user, triggerTimes);
		
		String group = campaign.getIdentifierString();
		List<String> triggers = quartzService.getTriggers(group);
		Assert.assertEquals(num, triggers.size());
		for (int i = 0; i < num; i++){
			collector.checkThat(triggers.get(i), new Contains("[slot="+i+"]"));
		}
		
		// cleanup
		quartzService.clearScheduleForCampaign(campaign);
	}
	
	@Test
	public void testRebuildRelativeCampaignSchedules_removeOld(){
		Campaign campaign = new Campaign();
		campaign.setName("test camp");
		campaign.setId(13L);
		
		User user = new User();
		user.setId(3L);
		
		int num = 3;
		List<ContactMsgTime> triggerTimes = getMsgTimes(num, 0, true);
		quartzService.rebuildRelativeCampaignSchedules(campaign, user, triggerTimes);
		String group = campaign.getIdentifierString();
		List<String> triggers = quartzService.getTriggers(group);
		Assert.assertEquals(num, triggers.size());
		
		int slotOffset = 5;
		List<ContactMsgTime> newTriggerTimes = getMsgTimes(num, slotOffset, true);
		quartzService.rebuildRelativeCampaignSchedules(campaign, user, newTriggerTimes);
		List<String> newTriggers = quartzService.getTriggers(group);
		Assert.assertEquals(num, newTriggers.size());
		
		for (int i = 0; i < num; i++){
			collector.checkThat(newTriggers.get(i), new Contains("[slot="+(slotOffset+i)+"]"));
		}
		
		// cleanup
		quartzService.clearScheduleForCampaign(campaign);
	}

	private List<ContactMsgTime> getMsgTimes(int num, int slotOffset, boolean includeMsgSlot) {
		List<ContactMsgTime> triggerTimes = new ArrayList<ContactMsgTime>();
		for (int i = 0; i < num; i++){
			ContactMsgTime msgTime = new ContactMsgTime();	
			msgTime.setMsgTime(new DateTime().plusHours(1).toDate());
			if (includeMsgSlot)
				msgTime.setMsgSlot(slotOffset + i);
			triggerTimes.add(msgTime);
		}
		return triggerTimes;
	}
	
	private String scheduleArtificialJob(Campaign campaign, boolean includeMsgSLot) throws SchedulerException, ParseException {
		
		String name = campaign.getName() +":"+ UUID.randomUUID();
		JobDetailBean jobDetail = new JobDetailBean();
		jobDetail.setName(name);
		jobDetail.setGroup(campaign.getIdentifierString());
		jobDetail.setJobClass(DummyJob.class);
		jobDetail.setVolatility(false);
		jobDetail.setDurability(true);
		jobDetail.setRequestsRecovery(true);
		
		Map<String, Object> jobMap = new HashMap<String, Object>();
		if (includeMsgSLot) {
			jobMap.put(RelativeCampJobRunner.PROP_MSGSLOT, 4);
		}
		
		CronTrigger trigger = new CronTrigger(name, campaign.getIdentifierString());
		String cron = "0 0 0 ? * *";
		trigger.setCronExpression(cron );
		trigger.setJobDataMap(new JobDataMap(jobMap));
		scheduler.scheduleJob(jobDetail, trigger);
		
		return cron;
	}
}
