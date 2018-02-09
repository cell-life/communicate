package org.celllife.mobilisr.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.trg.search.Filter;
import com.trg.search.Search;

/**
 * Default implementation of the CampaignScheduleService interface
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Service("campaignService")
public class CampaignScheduleServiceImpl implements CampaignScheduleService {

	private static final long serialVersionUID = 4175883818471225737L;

	private static Logger log = LoggerFactory.getLogger(CampaignScheduleServiceImpl.class);
	
	@Autowired
	private QuartzService quartzService;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private MailService mailService;

	@Autowired
	private MessageService messageService;
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void sendTestSMS(Campaign campaign, User user, String number, String smsMsg) throws InsufficientBalanceException, MsisdnFormatException{
		ValidationError error = validatorFactory.validateMsisdn(number);
		if (error != null){
			throw new MsisdnFormatException(number);
		}
		
		if (smsMsg == null || smsMsg.isEmpty()){
			return;
		}
		
		CampaignContact cc = new CampaignContact(campaign, new Contact(number, campaign.getOrganization()));
		List<CampaignContact> batchCampContact = new ArrayList<CampaignContact>();
		batchCampContact.add(cc);
		
		// TODO: don't reserve credit here
		int amountToReserve = MobilisrUtility.calculateMessageCost(smsMsg, 1);
		long reserveRef = userBalanceService.reserveAmount(
				campaign.getOrganization(), amountToReserve,
				campaign.getIdentifierString(), user, "Test sms for campaign: "
						+ campaign.getName());
		
		SmsMt smsMt = new SmsMt(cc.getMsisdn(),
				smsMsg, campaign.getIdentifierString());
		smsMt.setOrganizationId(campaign.getOrganization().getId());
		smsMt.setUserId(user.getId());
		smsMt.setTransactionRef(reserveRef);
		smsMt.setContactId(cc.getContact().getId());
		
		messageService.sendMessage(smsMt);
	}
	
	@Override
	public void sendTestSMS(Long campaignID, User user, String number,
			String smsMsg) throws InsufficientBalanceException, MsisdnFormatException  {
		Campaign campaign = campaignDAO.find(campaignID);
		sendTestSMS(campaign,user,number, smsMsg);
	}
	
	
	@Override
	public void sendWelcomeMessages(Campaign campaign, User user) {
        try {
		    quartzService.scheduleWelcomeJob(campaign, user);
        } catch(MobilisrSchedulingException e) {
            log.warn("Could not schedule welcome message job for campaign " + campaign.getId().toString() + " " + campaign.getName() + ". Reason: " + e.getMessage());
        }
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void scheduleCampaign(Campaign campaign, User user)
					throws InsufficientBalanceException, CampaignStateException {
		switch(campaign.getType()) {
		case FIXED:
			checkFixedCampaignStatus(campaign);
			scheduleFixedCampaign(campaign, user);
			break;
		case DAILY:
			scheduleDailyCampaign(campaign, user);
			break;
		case FLEXI:
			scheduleFlexiCampaign(campaign, user);
			break;	
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_REBUILD_CAMPAIGN_SCHEDULES"})
	public void rescheduleAllRelativeCampaigns(User user){
		Search s = new Search();
		s.addFilterOr(Filter.equal(Campaign.PROP_STATUS, CampaignStatus.ACTIVE),
				Filter.equal(Campaign.PROP_STATUS, CampaignStatus.STOPPING));
		s.addFilterNotEqual(Campaign.PROP_TYPE, CampaignType.FIXED);
		List<Campaign> search = campaignDAO.search(s);
		for (Campaign campaign : search) {
			try {
				log.warn("Rebuilding schedule for campaign [id={}]", campaign.getId());
				scheduleCampaign(campaign, user);
			} catch (Exception e) {
				log.error(LogUtil.getMarker_notifyAdmin(),
						"Error scheduling campaign: " + campaign.getId(),e);
			}
		}
	}

	private void checkFixedCampaignStatus(Campaign campaign) throws CampaignStateException {
		CampaignStatus status = campaignDAO.getCampaignStatus(campaign.getId());
		if (status == null) {
			String message = MessageFormat.format("Unable to get status for {0} campaign",
					campaign.getName());
			throw new CampaignStateException(message);
		} else if (status.isActiveState()) {
			String message = MessageFormat.format(
					"{0} campaign can not be scheduled since it is already scheduled",
					campaign.getName());
			throw new CampaignStateException(message);
		} else if (CampaignStatus.FINISHED.equals(status)){
			String message = MessageFormat.format(
					"{0} campaign can not be re-used",
					campaign.getName());
			throw new CampaignStateException(message);
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public CampaignStatus stopCampaign(Campaign campaign, User user) throws MobilisrSchedulingException{
		switch(campaign.getType()) {
		case FIXED:
			if (CampaignStatus.SCHEDULED.equals(campaign.getStatus())) {
				if(campaign.isSendNow()) {
					throw new MobilisrSchedulingException(
							"Unable to unschedule campaign. Too close to scheduled time.");
				}
				
				List<CampaignMessage> messages = campaignDAO.getAllCampMessages(campaign);
				CampaignMessage message = messages.get(0);
				Date msgTime = MobilisrUtility.combineDateAndTime(message.getMsgDate(),
						message.getMsgTime());
				long diff = msgTime.getTime() - new Date().getTime();
				if (diff < 600000) {
					throw new MobilisrSchedulingException(
							"Unable to unschedule campaign. Too close to scheduled time.");
				}
				
				Long ref = quartzService.getReserveReferenceForFixedCampaign(campaign);
				try {
					if (ref != null) {
						userBalanceService.unreserve(ref, campaign, "Campaign stopped:"
								+ campaign.getName(), user);
					}
				} catch (TransactionNotFoundException e) {
					log.error(LogUtil.getMarker_notifyAdmin(),
							"Error clearing reserved amount with ref=" + ref, e);
				}
				
				
				quartzService.clearScheduleForCampaign(campaign);
				userBalanceService.verifyZeroReserved(campaign,
						"Campaign stopped (" + campaign.getId() + ", " + campaign.getName() + ")",
						user);
				setCampaignStatus(campaign, CampaignStatus.INACTIVE);
			} else {
				log.warn("Can not stop campaign with status {}", campaign.getStatus());
			}
			break;
		case DAILY:
		case FLEXI:
			stopRelativeCampaign(campaign);
			break;
		}
		return campaign.getStatus();
	}

	/**
	 * @param campaign
	 */
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_START_STOP"})
	private void stopRelativeCampaign(Campaign campaign) {
		if (CampaignStatus.ACTIVE.equals(campaign.getStatus())) {
			/* 
			 * set stop date and let background service stop campaign once
			 * 	all contacts have completed campaign
			 */
			campaign.setEndDate(new Date());
			setCampaignStatus(campaign, CampaignStatus.STOPPING);
			processCampaignFinish(campaign);
		} else {
			log.warn("Can not stop campaign with status {}", campaign.getStatus());
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void processCampaignFinish() {
		List<Campaign> campaigns = campaignDAO.getRunningRelativeCampaignsWithEndDate();
		
		for (Campaign campaign : campaigns) {
			processCampaignFinish(campaign);
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void processCampaignFinish(Campaign campaign) {
		log.debug("Processing campaign finish for [campaign={}] [status={}]", campaign.getId(),
				campaign.getStatus());
		
		if (campaign.getEndDate() == null) {
			log.debug("Skipping Campaign {}, no end date set.", campaign.getId());
			return;
		}
		
		//2. For each campaign, check if all contacts processed 
		boolean isAllContactsProcessed = campaignDAO.isAllContactsProcessedForCampaign(campaign);

		if(isAllContactsProcessed) {
			log.info("All contacts processed for Campaign [id={}]."
					+ " Clearing schedules and updating campaign status", campaign.getId());
			quartzService.clearScheduleForCampaign(campaign);
			setCampaignStatus(campaign, CampaignStatus.FINISHED);
		}	
	}

	private void scheduleFixedCampaign(Campaign campaign, User user)
			throws InsufficientBalanceException  {
		Date msgTime = new Date();
		CampaignMessage message = campaignDAO.getAllCampMessages(campaign).get(0);
		if(!campaign.isSendNow()) {
			msgTime = MobilisrUtility.combineDateAndTime(message.getMsgDate(), message.getMsgTime());
		}

		campaign.setStartDate(new Date());
		setCampaignStatus(campaign, CampaignStatus.SCHEDULED);
		
		int totalContacts = campaignDAO.countNumberOfContactsForCampaign(campaign, false);
		int reserveAmount = MobilisrUtility.calculateMessageCost(message.getMessage(), totalContacts);
		String transactionMessage = "Schedule " + campaign.getName() + " campaign ("
			+ campaign.getType() + ") with " + totalContacts + " contacts.";
		Long reference;
		try {
			reference = userBalanceService.reserveAmount(
					campaign.getOrganization(), reserveAmount,
					campaign.getIdentifierString(), user, transactionMessage);
		} catch (InsufficientBalanceException e) {
			mailService.sendBalanceAlert(user, transactionMessage, reserveAmount,
					campaign.getOrganization());
			throw e;
		}

		quartzService.clearScheduleForCampaign(campaign);
		
		try {
			quartzService.scheduleFixedCampaignJob(campaign, user, msgTime, reference);
		} catch (MobilisrSchedulingException e) {
			log.error(LogUtil.getMarker_notifyAdmin(),"Error scheduling campaign",e);
			handleProgramScheduleFailure(campaign, user, reference);	
			throw e;
		}
	}
	
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_START_STOP","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	private void scheduleDailyCampaign(Campaign campaign, User user) {
		campaign.setEndDate(null);
		if (campaign.getStartDate() == null) {
			// only set start date for campaigns that have never been run
			campaign.setStartDate(new Date());
		}
		setCampaignStatus(campaign, CampaignStatus.ACTIVE);
		
		List<ContactMsgTime> triggerTimes = campaignDAO.getMsgTimesForCampaignFromContacts(campaign);
		log.debug("Scheduling relative campaign [{}] with {} trigger times", 
				campaign.getId(), triggerTimes.size());
		
		try {
			quartzService.rebuildRelativeCampaignSchedules(campaign, user, triggerTimes);
		} catch (MobilisrSchedulingException e) {
			log.error(LogUtil.getMarker_notifyAdmin(),"Error scheduling campaign",e);
			quartzService.clearScheduleForCampaign(campaign);
			handleProgramScheduleFailure(campaign, user, null);			
			throw e;
		}
	}

	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_START_STOP","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	private void scheduleFlexiCampaign(Campaign campaign, User user) {
		campaign.setEndDate(null);
		if (campaign.getStartDate() == null) {
			// only set start date for campaigns that have never been run
			campaign.setStartDate(new Date());
		}
		setCampaignStatus(campaign, CampaignStatus.ACTIVE);
		
		List<CampaignMessage> groupedMsgTimes = campaignDAO.findDefaultTimesForRelativeCampaign(campaign);
		
		List<ContactMsgTime> triggerTimes = new ArrayList<ContactMsgTime>();
		for (CampaignMessage campaignMessage : groupedMsgTimes) {
			ContactMsgTime msgTime = new ContactMsgTime();	
			msgTime.setMsgTime(campaignMessage.getMsgTime());
			triggerTimes.add(msgTime);
		}

		try {
			quartzService.rebuildRelativeCampaignSchedules(campaign, user, triggerTimes);
		} catch (MobilisrSchedulingException e) {
			log.error(LogUtil.getMarker_notifyAdmin(),"Error scheduling campaign",e);
			quartzService.clearScheduleForCampaign(campaign);
			handleProgramScheduleFailure(campaign, user, null);			
			throw e;
		}
	}

	/**
     *
     * @param campaign
     * @param user
     * @param reference
     */
	private void handleProgramScheduleFailure(Campaign campaign, User user, Long reference) {
		
		setCampaignStatus(campaign, CampaignStatus.SCHEDULE_ERROR);

		if (reference != null) {
			try {
				userBalanceService.unreserve(reference, campaign,
						"Campaign schedule error: " + campaign.getName(), user);
			} catch (TransactionNotFoundException e) {
				log.error(LogUtil.getMarker_notifyAdmin(),"Error clearing reserved amount with ref=" + reference, e);
			}
		}
		
		mailService.sendSystemAlert("Error scheduling campaign: " + campaign.getName()
				+ "[id=" + campaign.getId() + "]");
	}
	
	private void setCampaignStatus(Campaign campaign, CampaignStatus status) {
		if (!status.equals(campaign.getStatus())) {
			log.info("Changing campaign [id={}] status from [{}] to [{}]", 
					new Object[] {campaign.getId(), campaign.getStatus(), status});
			
			campaign.setStatus(status);
			campaignDAO.saveOrUpdate(campaign);
		}
	}
	
	/*package private*/ void setQuartzService(QuartzService quartzService) {
		this.quartzService = quartzService;
	}

	/*package private*/ void setUserBalanceService(UserBalanceService userBalanceService) {
		this.userBalanceService = userBalanceService;
	}

	/*package private*/ void setClientAlertService(MailService mailService) {
		this.mailService = mailService;
	}

	/*package private*/ void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/*package private*/ void setCampaignDAO(CampaignDAO campaignDAO) {
		this.campaignDAO = campaignDAO;
	}
	
	/*package private*/ void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}
}
