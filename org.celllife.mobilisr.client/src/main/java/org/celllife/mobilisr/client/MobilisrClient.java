package org.celllife.mobilisr.client;


public interface MobilisrClient {

	CampaignService getCampaignService();
	
	ContactService getContactService();

    MessageLogService getMessageLogService();
	
}
