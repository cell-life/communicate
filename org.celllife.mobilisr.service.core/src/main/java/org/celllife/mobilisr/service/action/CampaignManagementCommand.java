package org.celllife.mobilisr.service.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dagmar Timler
 */
public abstract class CampaignManagementCommand extends GetContactCommand {

	protected static final String CAMPAIGN_ID = "campaign";

	@Autowired
	protected CampaignService campaignService;

    @Autowired
    protected ContactsService contactsService;
	
	protected static Logger log = LoggerFactory.getLogger(CampaignManagementCommand.class);

	public CampaignManagementCommand() {
		super();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean execute(Context context) throws Exception {
		super.execute(context);
		
		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		log.debug("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());
		
		Contact contact = (Contact) context.get(CONTACT);

		Long campaignId = (Long) context.get(CAMPAIGN_ID);
		if (campaignId == null){
			log.warn("No campaign specified in campaign management command");
			return CONTINUE_PROCESSING;
		}
		
		Campaign campaign = (Campaign) getDao().find(Campaign.class, campaignId);
		if (campaign == null){
			log.warn("No campaign found with [id={}]", campaignId);
			return CONTINUE_PROCESSING;
		}
		
		List<Contact> contacts = new ArrayList<Contact>();
		contacts.add(contact);
		List<CampaignContact> contactList = campaignService.convertContactToCampaignContact(contacts, campaign);
		
		executeManagementCommand(context, smsLog, contact, campaign, contactList);
		
		return CONTINUE_PROCESSING;
	}
	
	abstract void executeManagementCommand(Context context, SmsLog smsLog, Contact contact, Campaign campaign, List<CampaignContact> contactList) throws Exception; 

	public Pconfig getConfigDescriptor(String beanName, String beanDescription) {
		Pconfig config = new Pconfig(null, beanDescription);
		config.setResource(beanName);
		EntityParameter campaign = new EntityParameter(CAMPAIGN_ID, "Campaign:");
		campaign.setDisplayProperty(Campaign.PROP_NAME);
		campaign.setValueProperty(Campaign.PROP_ID);
		campaign.setValueType(Long.class.getSimpleName());
		campaign.setEntityClass(Campaign.class.getName());
		config.addParameter(campaign);
		
		return config;
	}

	protected void setCampaignService(CampaignService campaignService) {
		this.campaignService = campaignService;
	}


}