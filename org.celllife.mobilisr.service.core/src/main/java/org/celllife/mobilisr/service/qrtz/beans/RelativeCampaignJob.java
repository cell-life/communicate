package org.celllife.mobilisr.service.qrtz.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Job runner for sending messages for relative campaigns
 * 
 * @author Simon Kelly
 */
@Component("relativeCampaignJob")
public class RelativeCampaignJob {

	/**
	 * Used to get job from application context so must match component name
	 */
	public static final String NAME = "relativeCampaignJob";

	private static final Logger log = LoggerFactory
			.getLogger(RelativeCampaignJob.class);

	@Autowired
	private CampaignDAO campaignDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private MessageService messageService;

	/**
	 * @param campaignId
	 * @param userId
	 * @param msgSlot
	 * @param msgTime
	 */
	public void sendMessagesForCampaign(Long campaignId, Long userId,
			Integer msgSlot, Date msgTime) {
		Campaign campaign = null;
		try {
			campaign = campaignDAO.find(campaignId);
			User user = null;
			if (userId != null) {
				user = userDAO.find(userId);
			}

			switch (campaign.getType()) {
			case DAILY:
				processDailyCampaign(campaign, user, campaignId, msgSlot,
						msgTime);
				break;
			case FLEXI:
				processFlexiCampaign(campaign, user, msgTime);
				break;
			default:
				throw new MobilisrSchedulingException(
						"Trying to run fixed campaign with relative campaign job");
			}
		} catch (Exception e) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error running relative campaign job. [campaign="
							+ campaignId + "]", e);
			if (campaign != null) {
				campaignDAO.updateCampaignStatus(campaign.getId(),
						CampaignStatus.SCHEDULE_ERROR);
			}
		}
	}

	/**
	 * This method obtains the contacts that are supposed to be processed for a
	 * given msg time and slot and then in turn gets the message for each of the
	 * contact based on the contact's current day. It then invokes the send sms
	 * routine procedure which manages sms sending for each contact
	 */
	private void processDailyCampaign(Campaign campaign, User user,
			Long campaignId, Integer msgSlot, Date msgTime) {

		List<CampaignContact> campaignContacts = campaignDAO
				.getContactsToProcessForDailyCampaign(campaign, msgTime,
						msgSlot, new Date());

		int totalContacts = campaignContacts.size();
		log.info("sending messages for relative campaign: [id={}], [msgSlot={}], [msgTime={}], [totalContacts={}]",
				new Object[] { campaignId, msgSlot, msgTime, totalContacts });

		for (CampaignContact campaignContact : campaignContacts) {
			
			CampaignMessage campaignMessage = campaignDAO
					.getCampMsgForDailyCampaign(campaign, msgSlot,
							campaignContact.getProgress());
			
			if (campaignMessage != null) {
				processContactsForSmsSending(campaign, user, campaignContact,
						campaignMessage);
			} else {
				log.info("No message for campaign: [id={}], [msgSlot={}], [msgDay={}]",
						new Object[] { campaignId, msgSlot,
								campaignContact.getProgress() });
			}
		}
	}

	/**
	 * This method obtains the msgs for a given campaign based on the msg time.
	 * It then calculates the progress of the message looking at campaign start
	 * date and message date. Obtaining the progress it then obtains the list of
	 * contacts to be processed whose progress matches the calculated progress
	 * and send the operation for sms sending
	 */
	private void processFlexiCampaign(Campaign campaign, User user,
			Date msgTime) {
		List<CampaignMessage> campaignMessages = campaignDAO
				.getCampMsgForFlexiCampaign(campaign, msgTime);
		log.info("sending messages for generic campaign: [id={}], [msgTime={}]",
				campaign.getId(), msgTime);

		for (CampaignMessage campaignMessage : campaignMessages) {
			int msgDay = campaignMessage.getMsgDay();
			List<CampaignContact> campaignContacts = campaignDAO
					.getContactsToProcessForFlexiCampaign(campaign, msgDay, new Date());
			log.debug("Num contacts for [msgDay={}], [msgTime={}], [num={}], [campaignId={}]",
					new Object[] { msgDay, msgTime, campaignContacts.size(),
							campaign.getId() });

			processContactsForSmsSending(campaign, user, campaignContacts,
					campaignMessage);
		}
	}

	private void processContactsForSmsSending(Campaign campaign, User user,
			CampaignContact campaignContact, CampaignMessage campaignMessage) {

		SmsMt smsMt = new SmsMt(campaignContact.getMsisdn(),
				campaignMessage.getMessage(), campaign.getIdentifierString());
		smsMt.setOrganizationId(campaign.getOrganization().getId());
		if (user != null) {
			smsMt.setUserId(user.getId());
		}
		smsMt.setContactId(campaignContact.getContact().getId());

		messageService.sendMessage(smsMt);

        List<CampaignContact> contactsArray = new ArrayList<CampaignContact>();
        contactsArray.add(campaignContact);
        this.campaignDAO.setCampaignContactsDateLastMessage(campaign,contactsArray);
	}

	private void processContactsForSmsSending(Campaign campaign, User user,
			List<CampaignContact> campaignContacts,
			CampaignMessage campaignMessage) {

		if (campaignContacts.isEmpty()) {
			return;
		}

		final SmsBatchConfig batchConfig = new SmsBatchConfig();
		batchConfig.setCreatedFor(campaign.getIdentifierString());
		batchConfig.setMessage(campaignMessage.getMessage());
		batchConfig.setTotalContacts(campaignContacts.size());
		batchConfig.setRecipients(campaignContacts);
		batchConfig.setOrganizationId(campaign.getOrganization().getId());
		if (user != null) {
			batchConfig.setUserId(user.getId());
		}

		messageService.sendMessage(batchConfig);
        this.campaignDAO.setCampaignContactsDateLastMessage(campaign,campaignContacts);
	}

	/* package private */void setCampaignDAO(CampaignDAO campaignDAO) {
		this.campaignDAO = campaignDAO;
	}

	/* package private */void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

}
