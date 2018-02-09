package org.celllife.mobilisr.service.message.processors;

import java.util.Date;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.message.route.BatchStats;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * This class manages the processing of batch stats.
 * 
 * @author Simon Kelly
 */
@Component("BatchStatsProcessor")
public class BatchStatsProcessor {

	private static final Logger log = LoggerFactory.getLogger(BatchStatsProcessor.class);
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private UserDAO userDao;

	@Autowired
	private CampaignDAO campaignDAO;

	/**
	 * This method is called once all batches are processed. This method could
	 * do something like perform billing for the sent messages.
	 *  
	 * @param stats
	 */
	@ServiceActivator(inputChannel="batchStats")
	public void processStats(BatchStats stats){
		log.info("==> All batches processed for {} with correlationId {}",
				stats.getCreatedFor(), stats.getBatchCorrelationId());
		if (log.isTraceEnabled())
			log.trace(stats.toString());
		
		String billingMsg = "SMS Sent for '" + stats.getCreatedFor()
				+ "' ( Recipients : " + stats.getTotalCount() + " , Success : "
				+ stats.getTotalSuccess() + " )";
		
		int messageCost = MobilisrUtility.calculateNumberOfMessages(stats.getMessageLength());
		int amountToDebit = stats.getTotalSuccess() * messageCost;
		int refAmntAdjustment = (stats.getTotalCount() - stats.getTotalEmptyTransaction()) * messageCost;
		
		User user = null;
		if (stats.getUserId() != null) {
			user = userDao.find(stats.getUserId());
		}
		Long transactionRef = stats.getTransactionRef();
		
		if (transactionRef != null){
			try {
				String createdFor = stats.getCreatedFor();
				userBalanceService.debitOrgBalance(amountToDebit, refAmntAdjustment,
						transactionRef,
						createdFor, stats.getTotalSuccess() + " messages sent", billingMsg, user);
			} catch (TransactionNotFoundException e) {
				log.error(LogUtil.getMarker_notifyAdmin(),
						"Unable to debit account for batches " + stats, e);
			}
		} else {
			if (amountToDebit != 0){
				log.error(LogUtil.getMarker_notifyAdmin(),
						"Non-zero amount to debit but no reserve transaction. " +
						"[debitAmt={}] [billingMsg={}]",amountToDebit, billingMsg);
			}
		}
		
		if (stats.isProcessCampaignCompletion()){
			Long campaignId = MobilisrUtility.getIdFromIdentifier(stats.getCreatedFor());
			Campaign campaign = campaignDAO.find(campaignId);
			
			if (campaign == null){
				log.warn("Requested process campaign completion but no "
						+ "campaign found for identifier [{}]",
						stats.getCreatedFor());
				return;
			}
			
			if(campaign.getType().equals(CampaignType.FIXED)){
				log.info("Campaign {} changing status to {}", campaign.getId(),CampaignStatus.FINISHED);
				
				campaign.setStatus(CampaignStatus.FINISHED);
				campaign.setEndDate(new Date());
				campaignDAO.merge(campaign);
				
				userBalanceService.verifyZeroReserved(campaign,"Campaign completed.", user);
			}
		}
	}
	
	/*package private*/ void setCampaignDAO(CampaignDAO campaignDAO) {
		this.campaignDAO = campaignDAO;
	}
	
	/*package private*/ void setUserBalanceService(UserBalanceService userBalanceService) {
		this.userBalanceService = userBalanceService;
	}
	
	/*package private*/ void setUserDao(UserDAO userDao) {
		this.userDao = userDao;
	}
}
