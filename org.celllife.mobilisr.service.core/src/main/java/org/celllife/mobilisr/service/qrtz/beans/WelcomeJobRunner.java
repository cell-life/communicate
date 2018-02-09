package org.celllife.mobilisr.service.qrtz.beans;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Job runner responsible for sending campaign welcome messages.
 * 
 * @author Simon Kelly
 */
public class WelcomeJobRunner extends QuartzJobBean {

	public static final String CAMPAIGN_ID = "campaignId";
	public static final String USER_ID = "userId";

	private Long campaignId;
	public Long userId;
	private ApplicationContext applicationContext;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		WelcomeJob job = (WelcomeJob) applicationContext.getBean(WelcomeJob.NAME);
		
		job.sendWelcomeMessages(campaignId, userId);
	}
	
	public void setCampaignId(Long campaignId){
		this.campaignId = campaignId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}
}
