package org.celllife.mobilisr.service.qrtz.beans;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Simon Kelly
 */
public class FixedCampJobRunner extends QuartzJobBean{
	
	public static final String PROP_CAMPAIGN_ID = "campaignId";
	public static final String PROP_USER_ID = "userId";
	public static final String PROP_TRANSACTION_REF = "transactionRef";

	private Long campaignId;
	private Long userId;
	private Long transactionRef;
	private ApplicationContext applicationContext;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext)throws JobExecutionException {
		FixedCampaignJob job = (FixedCampaignJob) applicationContext.getBean(FixedCampaignJob.NAME);
		job.sendMessagesForCampaign(campaignId, userId, transactionRef);
	}
	
	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public void setTransactionRef(Long transactionRef){
		this.transactionRef = transactionRef;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
