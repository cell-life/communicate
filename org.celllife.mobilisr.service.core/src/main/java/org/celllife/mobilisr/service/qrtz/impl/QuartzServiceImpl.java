package org.celllife.mobilisr.service.qrtz.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;
import org.celllife.mobilisr.service.qrtz.BackgroundJobs;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.qrtz.beans.FixedCampJobRunner;
import org.celllife.mobilisr.service.qrtz.beans.RelativeCampJobRunner;
import org.celllife.mobilisr.service.qrtz.beans.ScheduledReportsJobRunner;
import org.celllife.mobilisr.service.qrtz.beans.WelcomeJobRunner;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.util.LogUtil;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.stereotype.Service;

@Service("quartzService")
public class QuartzServiceImpl implements QuartzService {

	private static Logger log = LoggerFactory.getLogger(QuartzServiceImpl.class);

	/**
	 * These names must match the names in mobilisr-serviceContext.xml
	 */
	public static final String BACKGROUND_GROUP = "backgroundServices";
	public static final String WELCOME_JOB_RUNNER = "welcomeJobRunner";
	public static final String FIXED_CAMPAIGN_JOB_RUNNER = "fixedCampaignJobRunner";
	public static final String CAMPAIGN_JOB_GROUP = "campaignJobs";
	public static final String RELATIVE_CAMPAIGN_JOB_RUNNER = "relativeCampaignJobRunner";
	public static final String SCHEDULED_REPORT_GROUP = "scheduledReports";
	public static final String SCHEDULED_REPORT_JOB_RUNNER = "scheduledReportsJobRunner";

	@Autowired
	@Qualifier("qrtzScheduler")
	private Scheduler scheduler;

	@Loggable(LogLevel.TRACE)
	@Override
	public void scheduleFixedCampaignJob(Campaign campaign, User user, Date scheduleDate, Long transactionReference) throws MobilisrSchedulingException {

		Map<String, Object> jobDataMap = new HashMap<String, Object>();
		jobDataMap.put(FixedCampJobRunner.PROP_CAMPAIGN_ID, campaign.getId());
		jobDataMap.put(FixedCampJobRunner.PROP_TRANSACTION_REF, transactionReference);
		if (user != null) {
			jobDataMap.put(FixedCampJobRunner.PROP_USER_ID, user.getId());
		}

		String name = MessageFormat.format("{0}-[datetime={1,date,medium}]", campaign.getName(), scheduleDate);

		SimpleTriggerBean trigger = new SimpleTriggerBean();
		trigger.setRepeatCount(0);
		trigger.setStartTime(scheduleDate);
		trigger.setName(name);
		trigger.setGroup(campaign.getIdentifierString());
		trigger.setJobDataAsMap(jobDataMap);
		trigger.setJobName(FIXED_CAMPAIGN_JOB_RUNNER);
		trigger.setJobGroup(CAMPAIGN_JOB_GROUP);
		trigger.setVolatility(false);

		try {
			Date scheduledDate = scheduler.scheduleJob(trigger);
			log.debug("Campaign: {} scheduled to run at {}", campaign.getId(), scheduledDate);
		}
		catch (SchedulerException e) {
			throw new MobilisrSchedulingException("Error scheduling campaign. Cause: " + e.getMessage(), e);
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void rebuildRelativeCampaignSchedules(Campaign campaign, User user, List<ContactMsgTime> triggerTimes) throws MobilisrSchedulingException {

		String group = campaign.getIdentifierString();
		List<String> triggers = getTriggers(group);

		for (ContactMsgTime msgTime : triggerTimes) {
			Date triggerTime = msgTime.getMsgTime();
			int msgSlot = msgTime.getMsgSlot();

			String name = getNameForRelativeCampaignTrigger(campaign, triggerTime, msgSlot);

			if (triggers.contains(name)) {
				log.debug("Trigger [{}] already exists for campaign [{}]", name, campaign.getId());
				triggers.remove(name);
			}
			else {
				log.debug("MsgTime [{}] for campaign [{}] for msgSlot [{}]", new Object[] { triggerTime, campaign.getId(), msgSlot });
				createTriggerForRelativeCampaign(campaign, user, triggerTime, msgSlot);
			}
		}

		for (String oldTrigger : triggers) {
			try {
				log.debug("Removing old trigger for campaign [id={}] [trigger={}]", campaign.getId(), oldTrigger);
				scheduler.unscheduleJob(oldTrigger, campaign.getIdentifierString());
			}
			catch (SchedulerException e) {
				throw new MobilisrSchedulingException("Error removing trigger for campaign. Cause: " + e.getMessage(), e);
			}
		}
	}

	public void createTriggerForRelativeCampaign(Campaign campaign, User user, Date msgTime, Integer msgSlot) throws MobilisrSchedulingException {

		Map<String, Object> jobMap = new HashMap<String, Object>();

		jobMap.put(RelativeCampJobRunner.PROP_CAMPAIGN_ID, campaign.getId());
		jobMap.put(RelativeCampJobRunner.PROP_MSGTIME, msgTime);
		if (user != null) {
			jobMap.put(RelativeCampJobRunner.PROP_USER_ID, user.getId());
		}

		if (msgSlot != null) {
			jobMap.put(RelativeCampJobRunner.PROP_MSGSLOT, msgSlot);
		}

		String name = getNameForRelativeCampaignTrigger(campaign, msgTime, msgSlot);
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setName(name);
		trigger.setJobDataAsMap(jobMap);
		trigger.setJobName(RELATIVE_CAMPAIGN_JOB_RUNNER);
		trigger.setJobGroup(CAMPAIGN_JOB_GROUP);
		trigger.setVolatility(false);
		trigger.setGroup(campaign.getIdentifierString());

		try {
			String cronExpr = MobilisrUtility.cronExprForDailyOccurence(msgTime);
			trigger.setCronExpression(new CronExpression(cronExpr));
			scheduler.scheduleJob(trigger);
			log.debug("Campaign: {} scheduled with cron expression {}", campaign.getId(), cronExpr);

		}
		catch (Exception e) {
			throw new MobilisrSchedulingException("Error scheduling campaign. Cause: " + e.getMessage(), e);
		}
	}

	public void createTriggerForScheduledReport(ScheduledPconfig config, String cronExpr) throws MobilisrSchedulingException {

		Map<String, Object> jobMap = new HashMap<String, Object>();
		jobMap.put(ScheduledReportsJobRunner.PROP_REPORT_ID, config.getId());

		String triggerName = "scheduledReport-" + config.getId();

		List<String> triggers = getTriggers(SCHEDULED_REPORT_GROUP);

		if (triggers.contains(triggerName)) {
			log.debug("Trigger [{}] already exists for report [{}]", triggerName, config.getId());
			try {
				scheduler.unscheduleJob(triggerName, SCHEDULED_REPORT_GROUP);
			}
			catch (SchedulerException e) {
				throw new MobilisrSchedulingException("Unable to delete existing trigger for report", e);
			}
		}

		log.debug("Report [{}] ", new Object[] { config.getId() });

		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setName(triggerName);
		trigger.setJobDataAsMap(jobMap);
		trigger.setJobName(SCHEDULED_REPORT_JOB_RUNNER);
		trigger.setJobGroup(SCHEDULED_REPORT_GROUP);
		trigger.setVolatility(false);
		trigger.setGroup(SCHEDULED_REPORT_GROUP);
		trigger.setStartTime(config.getStartDate());

		if (config.getEndDate() != null) {
			trigger.setEndTime(config.getEndDate());
		}

		try {
			trigger.setCronExpression(new CronExpression(cronExpr));
			scheduler.scheduleJob(trigger);
			log.debug("Reports: {} scheduled with cron expression {}", 0, cronExpr);
		}
		catch (Exception e) {
			throw new MobilisrSchedulingException("Error scheduling campaign. Cause: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteTriggerForScheduledReport(String reportId) {

		String triggerName = "scheduledReport-" + reportId;

		List<String> triggers = getTriggers(SCHEDULED_REPORT_GROUP);

		if (triggers.contains(triggerName)) {
			try {
				scheduler.unscheduleJob(triggerName, SCHEDULED_REPORT_GROUP);
			}
			catch (SchedulerException e) {
				throw new MobilisrSchedulingException("Unable to delete existing trigger for report", e);
			}
		}
	}

	/**
	 * @param campaign
	 * @param msgTime
	 * @param msgSlot
	 * @return
	 */
	private String getNameForRelativeCampaignTrigger(Campaign campaign, Date msgTime, Integer msgSlot) {

		String name = MessageFormat.format("{0}-[slot={1}]-[time={2,time,medium}]", campaign.getName(), msgSlot, msgTime);
		return name;
	}

	@Override
	public void clearScheduleForCampaign(Campaign campaign) {

		try {
			String group = campaign.getIdentifierString();
			List<String> triggers = getTriggers(group);
			for (String trigger : triggers) {
				boolean unscheduleJob = scheduler.unscheduleJob(trigger, campaign.getIdentifierString());
				if (unscheduleJob) {
					log.debug("Trigger : [{}] deleted for group : [{}]", trigger, campaign.getIdentifierString());
				}
				else {
					log.error("Trigger : [{}] NOT deleted for group : [{}]", trigger, campaign.getIdentifierString());
				}
			}
		}
		catch (SchedulerException e) {
			log.error(LogUtil.getMarker_notifyAdmin(), "Error clearing schedule for campaign id=" + campaign.getId(), e);
		}
	}

	@Override
	public Long getReserveReferenceForFixedCampaign(Campaign campaign) {

		try {
			JobDetail jobDetail = scheduler.getJobDetail(campaign.getIdentifierString(), campaign.getIdentifierString());
			if (jobDetail == null) {
				return null;
			}
			return jobDetail.getJobDataMap().getLong(FixedCampJobRunner.PROP_TRANSACTION_REF);
		}
		catch (SchedulerException e) {
			return null;
		}

	}

	@Override
	public List<String> getTriggers(String groupName) {

		// String group = campaign.getIdentifierString();
		ArrayList<String> list = new ArrayList<String>();

		try {
			String[] triggers = scheduler.getTriggerNames(groupName);
			CollectionUtils.addAll(list, triggers);
			return list;
		}
		catch (SchedulerException e) {
			log.error(LogUtil.getMarker_notifyAdmin(), "Error checking triggers [group=" + groupName + "]", e);
		}
		return list;
	}

	private boolean doesTriggerExist(String triggerGroup, String triggerName) {

		try {
			Trigger triggerNames = scheduler.getTrigger(triggerName, triggerGroup);
			return triggerNames != null;
		}
		catch (SchedulerException e) {
			log.error(LogUtil.getMarker_notifyAdmin(), "Error checking triggers [name=" + triggerName + "] [group=" + triggerGroup + "]", e);
		}

		return false;
	}

	@Override
	public void scheduleWelcomeJob(Campaign campaign, User user) {

		String name = MessageFormat.format("{0}-welcome", campaign.getName());

		Map<String, Object> jobDataMap = new HashMap<String, Object>();
		jobDataMap.put(WelcomeJobRunner.CAMPAIGN_ID, campaign.getId());
		if (user != null) {
			jobDataMap.put(WelcomeJobRunner.USER_ID, user.getId());
		}

		SimpleTriggerBean trigger = new SimpleTriggerBean();
		trigger.setVolatility(true);
		trigger.setRepeatCount(0);
		trigger.setStartTime(new Date());
		trigger.setName(name);
		trigger.setJobDataAsMap(jobDataMap);
		trigger.setJobName(WELCOME_JOB_RUNNER);
		trigger.setJobGroup(CAMPAIGN_JOB_GROUP);
		trigger.setGroup(campaign.getIdentifierString());

		try {
			Date scheduledDate = scheduler.scheduleJob(trigger);
			log.debug("WelcomeJob for campaign [id={}] scheduled to run at [{}]", campaign.getId(), scheduledDate);
		}
		catch (SchedulerException e) {
			throw new MobilisrSchedulingException("Error scheduling campaign. Cause: " + e.getMessage(), e);
		}
	}

	@Override
	public void fireBackgroundJob(BackgroundJobs job) throws SchedulerException {

		JobDetail jobDetail = scheduler.getJobDetail(job.getJobName(), BACKGROUND_GROUP);
		if (jobDetail == null) {
			throw new SchedulerException("No job found for name: " + job.getJobName());
		}

		String triggerName = "manualFire" + job.getJobName();
		boolean exists = doesTriggerExist(BACKGROUND_GROUP, triggerName);
		if (!exists) {
			log.debug("Manual fire of job [{}]", job);
			Trigger trigger = new SimpleTrigger(triggerName, BACKGROUND_GROUP, new Date());
			trigger.setJobName(job.getJobName());
			trigger.setVolatility(true);
			trigger.setJobGroup(BACKGROUND_GROUP);

			scheduler.scheduleJob(trigger);
		}
	}

}
