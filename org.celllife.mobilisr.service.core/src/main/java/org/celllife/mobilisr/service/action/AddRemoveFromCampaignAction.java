package org.celllife.mobilisr.service.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This action differs from {@link AddToCampaignAction} and {@link RemoveFromCampaignAction}
 * in that if the user is already on the campaign they are removed otherwise they are added.
 * 
 * This action is also able to send out messages depending on the result of the action.
 */
@Component("AddRemoveFromCampaignAction")
public class AddRemoveFromCampaignAction extends CampaignManagementCommand  implements Action {
	
	private static Logger log = LoggerFactory.getLogger(AddRemoveFromCampaignAction.class);

	static final String RESTART_EXISTING = "restartExisting";
	static final String BEAN_NAME = "AddRemoveFromCampaignAction";
	static final String SUBSCRIBE = "subscribeMessage";
	static final String UNSUBSCRIBE = "unsubscribeMessage";
	
	@Autowired
	private MessageService messageService;

	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig config = super.getConfigDescriptor(BEAN_NAME, "Add to or remove from Campaign");
		
		StringParameter subscribe = new StringParameter(SUBSCRIBE, "Subscribe message:");
		subscribe.setDisplayType("sms");
		subscribe.setTooltip("If not empty this message will be sent to the contact if they are" +
		" added to the campaign.");
		
		StringParameter unsubscribe = new StringParameter(UNSUBSCRIBE, "Unsubscribe message:");
		unsubscribe.setDisplayType("sms");
		unsubscribe.setTooltip("If not empty this message will be sent to the contact if they are" +
				" removed from the campaign.");
		
		BooleanParameter restartExisting = new BooleanParameter(RESTART_EXISTING, 
			"Restart previously removed contacts?");
		restartExisting.setDefaultValue(true);
		restartExisting.setTooltip("When adding contacts that were previously removed from the campaign " +
			"do you want to restart them from the beginning of the campaign or" +
			" let them continue from where they left off?");

		config.addParameter(subscribe);
		config.addParameter(unsubscribe);
		config.addParameter(restartExisting);
		return config;
	}

	@Override
	void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, Campaign campaign, List<CampaignContact> contactList) throws Exception {
		Boolean restart = (Boolean) context.get(RESTART_EXISTING);
		String subscribeMsg = (String) context.get(SUBSCRIBE);
		String unsubscribeMsg = (String) context.get(UNSUBSCRIBE);

		if (log.isDebugEnabled()) {
			log.debug("Processing {} action for [contact={}] with params: "
					+ "[restart={}] [subscribeMsg={}] [unsubscribeMsg={}]",
					new Object[] { BEAN_NAME, contact.getMsisdn(), restart,
							subscribeMsg, unsubscribeMsg });
		}

		boolean subscribe = false;
		for (CampaignContact campaignContact : contactList) {
			if (campaignContact.isPersisted() && campaignContact.getEndDate() == null) {
				// if they are already on the campaign then 'remove' them
				subscribe = false;
				campaignContact.setEndDate(new Date());
				if (log.isTraceEnabled()) {
					log.trace("Stopping contact on campaign [campaign={}] [contact={}]",
						campaign.getName(), contact.getMsisdn());
				}
                campaignService.saveOrUpdateCampaignContact(campaignContact);
                campaignService.rescheduleRelativeCampaign(campaign, (User) null);

            } else { // otherwise make sure they are active and restart them if necessary

                subscribe = true;

                List<Long> linkedCampaigns = campaignService.getLinkedCampaignsForCampaign(campaign.getId());
                List<Campaign> campaignsToUpdate = new ArrayList<Campaign>();

                // If this campaign has no links, then proceed as normal.
                if (linkedCampaigns.isEmpty()) {

                    campaignContact.setEndDate(null);
                    if (restart != null && restart) {
                        campaignContact.setProgress(0);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("Starting contact on campaign [campaign={}] [contact={}]",
                                campaign.getName(), contact.getMsisdn());
                    }

                    campaignService.saveOrUpdateCampaignContact(campaignContact);
                    if (campaign.isActive()) {
                        campaignService.rescheduleRelativeCampaign(campaign, (User) null);
                        log.info("Rescheduling campaign " + campaign.getId() + " " + campaign.getName());
                    }

                }

                // If this campaign has links to it, proceed as follows.
                else {
                    campaignService.addContactsToCampaignWithLinks(contactList, campaign, null,null);
                }

            }

		String message = subscribe ? subscribeMsg : unsubscribeMsg;
		if (message != null && !message.isEmpty()){
			if (log.isTraceEnabled()){
				log.trace("Sending '{}' message to contact [contact={}]",
					subscribe ? "subscribe" : unsubscribeMsg, contact.getMsisdn());
			}

			MessageFilter filter = (MessageFilter) context.get(FILTER);
			SmsMt smsMt = new SmsMt(smsLog.getMsisdn(), message, filter.getIdentifierString());
			smsMt.setOrganizationId(smsLog.getOrganization().getId());

			messageService.sendMessage(smsMt);
		}
        }
    }

	protected void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
}
