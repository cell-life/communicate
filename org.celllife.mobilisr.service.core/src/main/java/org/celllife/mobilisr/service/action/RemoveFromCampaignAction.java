package org.celllife.mobilisr.service.action;

import java.util.List;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.pconfig.model.Pconfig;
import org.springframework.stereotype.Component;

@Component("RemoveFromCampaignAction")
public class RemoveFromCampaignAction extends CampaignManagementCommand implements Action {
	
	static final String BEAN_NAME = "RemoveFromCampaignAction";

	@Override
	public Pconfig getConfigDescriptor() {
		return super.getConfigDescriptor(BEAN_NAME, "Remove sender from Campaign");
	}

	@Override
	void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, Campaign campaign, List<CampaignContact> contactList) throws Exception {
		// TODO: how do permissions work here since this is executed asynchronously and the 
		// logged in user if any is not the same as the owner of the filter?
		campaignService.removeContactsFromCampaign(campaign, contactList);
		campaignService.rescheduleRelativeCampaign(campaign, (User)null);
	}

}
