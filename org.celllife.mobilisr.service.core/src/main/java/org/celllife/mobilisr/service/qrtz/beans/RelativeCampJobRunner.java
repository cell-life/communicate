package org.celllife.mobilisr.service.qrtz.beans;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Simon Kelly
 */
public class RelativeCampJobRunner extends QuartzJobBean{
	
	public static final String PROP_CAMPAIGN_ID = "campaignId";
	public static final String PROP_USER_ID = "userId";
	public static final String PROP_MSGSLOT = "msgSlot";
	public static final String PROP_MSGTIME = "msgTime";

	private Long campaignId;
	private Long userId;
	private Integer msgSlot;
	private Date msgTime;
	private ApplicationContext applicationContext;
	
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext)throws JobExecutionException {
		RelativeCampaignJob job = (RelativeCampaignJob) applicationContext.getBean(RelativeCampaignJob.NAME);
		job.sendMessagesForCampaign(campaignId, userId, msgSlot, msgTime);
	}
	
	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setMsgSlot(Integer msgSlot) {
		this.msgSlot = msgSlot;
	}
	
	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime;
	}
}
