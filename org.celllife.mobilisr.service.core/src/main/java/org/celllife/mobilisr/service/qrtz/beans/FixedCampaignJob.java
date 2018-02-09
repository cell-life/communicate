package org.celllife.mobilisr.service.qrtz.beans;

import java.util.List;
import java.util.UUID;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;

@Component("fixedCampaignJob")
public class FixedCampaignJob {
	
	private static final int DEFAULT_BATCH_SIZE = 50;

	/**
	 * Used to get job from application context so must match component name
	 */
	public static final String NAME = "fixedCampaignJob";
	
	private static final Logger log = LoggerFactory.getLogger(FixedCampaignJob.class);
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private SettingService settingService;
	
	/**
	 * This method obtains all the contacts that are supposed to get a particular message 
	 * for a fixed campaign and then using techniques of bulk processing, processes each
	 * one in batches for sms execution
	 */
	protected void sendMessagesForCampaign(Long campaignId, Long userId, Long transactionRef) {
		Campaign campaign = null;
		try {
			campaign = campaignDAO.find(campaignId);
			
			changeCampaignStatusToRunning(campaign);
			
			int totalNumOfContacts = campaignDAO.countNumberOfContactsForCampaign(campaign, false);
			log.debug("Total contacts for campaign [id={}]: [{}]",campaign.getId(),totalNumOfContacts);	
			
			manageBatchSmsSending(campaign, userId, totalNumOfContacts, transactionRef);
		} catch (Exception e) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error running fixed campaign job. [campaign="+campaignId+"]",e);
			if (campaign != null){
				campaignDAO.updateCampaignStatus(campaign.getId(), CampaignStatus.SCHEDULE_ERROR);
			}
		}
	}

	
	private void manageBatchSmsSending(Campaign campaign, Long userId, final int totalNumOfContacts,
			final Long balReserveReference) throws ChannelStateException {
		int batchNumber = 0;
		List<CampaignContact> batchedContacts;
		
		String smsMsg = campaignDAO.getCampMsgForCampaign(campaign);
		if (smsMsg == null){
			log.warn("Unable to run campaign ({}) message is null", campaign.getName());
			return;
		}
		
		String correlationId = UUID.randomUUID().toString();
		do {
			// execute inside loop to allow adjustment of value during sending (large sends may take a long time)
			Integer batchSize = settingService.getSettingValue(SettingsEnum.MESSAGE_BATCH_SIZE);
			batchSize = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;
			
			PagingLoadConfig pagingLoadConfig = new BasePagingLoadConfig(
					batchNumber*batchSize, batchSize);
			batchedContacts = campaignDAO.getContactsInPaginationForCampaign(
					campaign, pagingLoadConfig);
			log.debug("Batch number {} with size {}",batchNumber, batchedContacts.size());	
			batchNumber++;
			
			if(!batchedContacts.isEmpty()){
				final SmsBatchConfig batchConfig = new SmsBatchConfig(
						correlationId, campaign.getIdentifierString(), smsMsg,
						batchedContacts, totalNumOfContacts,
						balReserveReference, userId, campaign
								.getOrganization().getId(), true);
				messageService.sendMessage(batchConfig);
			}
		}while(!batchedContacts.isEmpty());
	}

	
	private void changeCampaignStatusToRunning(Campaign campaign) {
		campaignDAO.updateCampaignStatus(campaign.getId(), CampaignStatus.RUNNING);
		log.debug("Campaign: {} status change: {}", campaign.getId(), CampaignStatus.RUNNING);
	}
	
	/*package private*/ void setCampaignDAO(CampaignDAO campaignDAO) {
		this.campaignDAO = campaignDAO;
	}

	/*package private*/ void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


	/*package private*/ void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
}
