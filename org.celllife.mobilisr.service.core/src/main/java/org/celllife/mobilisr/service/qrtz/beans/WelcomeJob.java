package org.celllife.mobilisr.service.qrtz.beans;

import java.text.MessageFormat;
import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("welcomeJob")
public class WelcomeJob {

	/**
	 * Used to get job from application context so must match component name
	 */
	public static final String NAME = "welcomeJob";
	
	private static final Logger log = LoggerFactory.getLogger(WelcomeJob.class);
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private CampaignDAO campaignDao;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	MessageService messageService;
	
	public void sendWelcomeMessages(Long campaignId, Long userId) {
		Campaign campaign = campaignDao.find(campaignId);
		String welcomeMsg = campaign.getWelcomeMsg();
		
		User user = null;
		if (userId != null) {
			user = userDAO.find(userId);
		}
		
		List<CampaignContact> contacts = campaignDao.getCampaignContactsNeedingWelcomeMessage(campaign);
		
		if (contacts.isEmpty()){
			log.info("No contacts needing welcome message for campaign [id={}]", campaignId);
			return;
		}
		
		if (welcomeMsg != null && !welcomeMsg.isEmpty()){
		
			for (CampaignContact campaignContact : contacts) {
				try {
					processContactsForSmsSending(campaign, user, campaignContact, welcomeMsg);
				} catch (InsufficientBalanceException e) {
					mailService.sendBalanceAlert(user, e.getTransactionMessage(), e.getAmountRequested(),
							campaign.getOrganization());
					return;
				}
			}
		} else {
			log.debug("No welcome message for campaign [id={}]",campaignId);
		}
		
		campaignDao.markCampaignContactsAsReceivedWelcomeMessage(contacts);
	}
	
	private void processContactsForSmsSending(Campaign campaign, User user, CampaignContact campaignContact, String message) throws InsufficientBalanceException {
		
		// TODO: don't reserve credit here
		int reserveAmount = MobilisrUtility.calculateNumberOfMessages(message);
		String transactionMessage = MessageFormat.format(
				"Welcome message for contact '{0}' on campaign '{1}'",
				new Object[] { campaignContact.getMsisdn(), campaign.getName() });
		
		Long reference = userBalanceService.reserveAmount(
				campaign.getOrganization(), reserveAmount,
				campaign.getIdentifierString(), user, transactionMessage);
		
		SmsMt smsMt = new SmsMt(campaignContact.getMsisdn(),
				message, campaign.getIdentifierString());
		smsMt.setOrganizationId(campaign.getOrganization().getId());
		if (user != null) {
			smsMt.setUserId(user.getId());
		}
		smsMt.setTransactionRef(reference);
		smsMt.setContactId(campaignContact.getContact().getId());
		
		messageService.sendMessage(smsMt);
	}
	
	/*package private*/ void setCampaignDao(CampaignDAO campaignDao) {
		this.campaignDao = campaignDao;
	}

	/*package private*/ void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/*package private*/ void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	
	/*package private*/ void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	/*package private*/ void setClientAlertService(MailService mailService) {
		this.mailService = mailService;
	}
	
	/*package private*/ void setUserBalanceService(UserBalanceService userBalanceService) {
		this.userBalanceService = userBalanceService;
	}
}

