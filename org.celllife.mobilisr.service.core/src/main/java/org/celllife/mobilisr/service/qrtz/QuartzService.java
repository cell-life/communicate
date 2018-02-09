package org.celllife.mobilisr.service.qrtz;

import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.quartz.SchedulerException;

/**
 * Generic interface related to all the operations on quartz
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
public interface QuartzService {

	/**
	 * This method schedules the job in the quartz scheduler using the particular date
	 * @param campaign			Campaign for which the job must be scheduled
	 * @param user 				the user who is performing the action
	 * @param scheduleDate		Date specifying when an event should fire from the quartz
	 * @param transactionReference the transaction reference for the reserved transaction
	 * @throws MobilisrSchedulingException
	 */
	public void scheduleFixedCampaignJob(Campaign campaign, User user, Date scheduleDate, Long transactionReference) throws MobilisrSchedulingException;

	/**
	 * This method re-builds the campaign triggers adding any new ones and removing old ones.
	 * 
	 * @param campaign			Campaign for which the job must be scheduled
	 * @param user 				the user who is performing the action
	 * @param triggerTimes		a list of ContactMsgTimes which will be used to create the campaign schedule
	 */
	public void rebuildRelativeCampaignSchedules(Campaign campaign, User user, List<ContactMsgTime> triggerTimes) throws MobilisrSchedulingException;

	/**
	 * Clears all the quartz triggers and jobs associated with this campaign
	 * @param campaign		Campaign for which the scheduler must be cleared
	 */
	public void clearScheduleForCampaign(Campaign campaign);

	/**
	 * Schedule the job to send welcome message for this campaign.
	 * 
	 * @param campaign the campaign whose contacts need to receive teh welcome message
	 * @param user the user who is performing the action
	 */
	public void scheduleWelcomeJob(Campaign campaign, User user);

	/**
	 * Get the reserve transaction reference from the job data map for
	 * a fixed campaign.
	 * 
	 * @param campaign
	 * @return null if the property or job can not be found.
	 */
	public Long getReserveReferenceForFixedCampaign(Campaign campaign);

	/**
	 * Fires one of the background jobs immediately
	 * 
	 * @param jobName
	 * @throws SchedulerException
	 */
	void fireBackgroundJob(BackgroundJobs job) throws SchedulerException;

	List<String> getTriggers(String groupName);
	
	public void createTriggerForScheduledReport(ScheduledPconfig config, String cronExp) throws MobilisrSchedulingException;

	public void deleteTriggerForScheduledReport(String reportId);

}
