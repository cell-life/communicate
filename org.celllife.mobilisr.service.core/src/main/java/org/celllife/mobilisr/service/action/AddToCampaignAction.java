package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("AddToCampaignAction")
public class AddToCampaignAction extends CampaignManagementCommand implements Action {

	static final String RESTART_EXISTING = "restartExisting";
	static final String BEAN_NAME = "AddToCampaignAction";

	@Override
	public Pconfig getConfigDescriptor() {
		Pconfig config = super.getConfigDescriptor(BEAN_NAME, "Add sender to Campaign");

		BooleanParameter restartExisting = new BooleanParameter(RESTART_EXISTING,
				"Restart previously removed contacts?");
		restartExisting.setDefaultValue(true);
		restartExisting.setTooltip("When adding contacts that were previously removed from the campaign " +
				"do you want to restart them from the beginning of the campaign or" +
				" let them continue from where they left off?");

		config.addParameter(restartExisting);
		return config;
	}

	@Override
	void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, Campaign campaign, List<CampaignContact> contactList) throws Exception {
		Boolean restart = (Boolean) context.get(RESTART_EXISTING);

        List<Long> linkedCampaigns = campaignService.getLinkedCampaignsForCampaign(campaign.getId());

        // If this campaign has no links, then proceed as normal.
        if (linkedCampaigns.isEmpty()) {
            addContactToCampaign(contactList, restart, campaign);
        } else {  // If this campaign has links to it, proceed as follows.
            campaignService.addContactsToCampaignWithLinks(contactList, campaign, null, null);
        }

    }

    protected void addContactToCampaign(List<CampaignContact> contactList, Boolean restart, Campaign campaign) throws Exception {

        for (CampaignContact campaignContact : contactList) {
            campaignContact.setEndDate(null);
            if (restart != null && restart) {
                campaignContact.setProgress(0);
            }
            campaignService.saveOrUpdateCampaignContact(campaignContact);
        }

        if (campaign.isActive()) {
            campaignService.rescheduleRelativeCampaign(campaign, (User) null);
        }

    }

    protected void setContactsService(ContactsService contactsService) {
        this.contactsService = contactsService;
	}

}
