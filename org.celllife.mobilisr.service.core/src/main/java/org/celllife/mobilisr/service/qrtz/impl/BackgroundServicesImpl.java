package org.celllife.mobilisr.service.qrtz.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.message.processors.MessageReprocessor;
import org.celllife.mobilisr.service.qrtz.BackgroundServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import com.trg.search.Search;

/**
 * Default implementation for the BackgroundServices interface
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
@Service("backgroundService")
@ManagedResource(objectName="org.celllife.mobilisr:name=BackgroundServices", 
                 description="Background Services manager.")
public class BackgroundServicesImpl implements BackgroundServices{

	private static Logger log = LoggerFactory.getLogger(BackgroundServicesImpl.class);
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private CampaignScheduleService scheduleService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private MessageReprocessor inMessageQueueProcessor;
	
	@Autowired
	private SettingService settingService;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void triggerMessageProcessing(){
		inMessageQueueProcessor.triggerMessageProcessing();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void processCampContactProgress(){
		log.debug("Scheduled processing of campaign contacts");

		/**
		 * LOGIC TRACE:
		 * 1. Fetch campaigns that are not fixed
		 * 2. For each contact in the campaign, contact(progress) += 1 on condition 
		 * 	  that contact(progress) <= campDuration & contact(joiningDate) <= campaign(endDate) [endDate != null i.e]
		 */
		
		//1. Fetch campaigns that are not fixed
		Search search = new Search();
		search.addFilterNotEqual(Campaign.PROP_TYPE, CampaignType.FIXED);
		List<Campaign> campaigns = campaignDAO.search(search);
		
		for (Campaign campaign : campaigns) {
			
			//2. For each contact in the campaign, contact(progress) += 1 on condition 
			//   that contact(progress) <= campDuration & contact(joiningDate) <= campaign(endDate) [endDate != null i.e]
			campaignDAO.updateCampaignContactsProgress(campaign);
		}
	}
	
	@ManagedOperation(description="Update organization blanaces from transaction record")
	@Loggable(LogLevel.TRACE)
	@Override
	public void updateOrganizationBalances(){
		userBalanceService.updateOrgBalances();
	}
	
	@ManagedOperation(description="Process finished Campaigns")
	@Loggable(LogLevel.TRACE)
	@Override
	public void processCampFinish() {
		scheduleService.processCampaignFinish();
	}
	
	@ManagedOperation(description="Process mail queue")
	@Loggable(LogLevel.TRACE)
	@Override
	/**
	 * changes to this method name must also be made in mobilisr-serviceContext.xml 
	 */
	public void processMailQueue() {
		Boolean processQueue = settingService.getSettingValue(SettingsEnum.ENABLE_MAIL_QUEUE_PROCESSING);
		if (processQueue)
			mailService.sendQueuedMail();
	}

	public void checkMessagesDelivered()
	{
		Integer validity = (settingService.getSettingValue(SettingsEnum.MESSAGE_VALIDITY_TIME));
		smsLogDAO.updateUndeliveredMessages(validity);
	}
	
	/*package private*/ void setCampaignDao(CampaignDAO campaignDao) {
		campaignDAO = campaignDao;
	}

	/*package private*/ void setUserBalanceService(UserBalanceService balanceService) {
		userBalanceService = balanceService;
	}

	/*package private*/ void setScheduleService(CampaignScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}
}
