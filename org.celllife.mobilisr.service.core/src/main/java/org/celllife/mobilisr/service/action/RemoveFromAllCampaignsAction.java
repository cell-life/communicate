package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Component("RemoveFromAllCampaignsAction")
public class RemoveFromAllCampaignsAction extends GetContactCommand implements Action {

    private static Logger log = LoggerFactory.getLogger(RemoveFromAllCampaignsAction.class);

    static final String BEAN_NAME = "RemoveFromAllCampaignsAction";

    static final String UNSUBSCRIBE = "unsubscribeMessage";

    @Autowired
    private MessageService messageService;

    @Autowired
    protected CampaignService campaignService;

    @Autowired
    protected ContactsService contactsService;

    @Override
    public Pconfig getConfigDescriptor() {

        Pconfig config = new Pconfig(null, "Remove From All Campaigns Action");
        config.setResource(BEAN_NAME);

        StringParameter unsubscribe = new StringParameter(UNSUBSCRIBE, "Unsubscribe message:");
        unsubscribe.setDisplayType("sms");
        unsubscribe.setTooltip("If not empty this message will be sent to the contact if they are" +
                " removed from the campaign.");

        config.addParameter(unsubscribe);

        return config;
    }

    @Loggable(LogLevel.TRACE)
    @Override
    public boolean execute(Context context) throws Exception {

        SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
        String unsubscribeMsg = (String) context.get(UNSUBSCRIBE);

        log.trace("Processing incoming sms from [msisdn={}]", smsLog.getMsisdn());

        List<Long> campaignIds = contactsService.listAllCampaignsForContact(smsLog.getMsisdn());

        if (campaignIds.isEmpty()) {
            log.warn("No flexi or daily campaigns could be found for msisdn " + smsLog.getMsisdn());
            return CONTINUE_PROCESSING;
        }

        for (Long campaignId : campaignIds) {

            log.debug("Processing {} action for [contact={}] ", new Object[]{BEAN_NAME, smsLog.getMsisdn()});

            Campaign campaign = campaignService.getCampaign(campaignId);

            if (campaign.getType() != CampaignType.FIXED && campaign.isActive()) {

                CampaignContact campaignContact = campaignService.getCampaignContact(campaign, smsLog.getMsisdn());
                if (campaignContact.isPersisted() && campaignContact.getEndDate() == null) {
                    log.warn("Stopping contact {} on campaign {} with id {} ", smsLog.getMsisdn(), campaign.getName(), campaign.getId());
                    campaignContact.setEndDate(new Date());
                    campaignService.saveOrUpdateCampaignContact(campaignContact);
                    campaignService.rescheduleRelativeCampaign(campaign, (User) null);
                } else if (campaignContact.getEndDate() != null) {
                    log.debug("Could not remove contact {} from campaign {}. Reason: The contact has already ended this campaign.", new Object[]{smsLog.getMsisdn(), campaign.getName()});
                }

            } else if (campaign.getType() != CampaignType.FIXED && !campaign.isActive()) {
                log.warn("Could not remove contact {} from campaign {}. Reason: The campaign is not active.", new Object[]{smsLog.getMsisdn(), campaign.getName()});
            } else if (campaign.getType() == CampaignType.FIXED) {
                log.debug("Could not remove contact {} from campaign {}. Reason: The campaign is a \"Just Send SMS\" Campaign.", new Object[]{smsLog.getMsisdn(), campaign.getName()});
            }
        }

        String message = unsubscribeMsg;
        if (message != null && !message.isEmpty()) {

            log.trace("Sending '{}' message to contact [contact={}]", unsubscribeMsg, smsLog.getMsisdn());

            MessageFilter filter = (MessageFilter) context.get(FILTER);
            SmsMt smsMt = new SmsMt(smsLog.getMsisdn(), message, filter.getIdentifierString());
            smsMt.setOrganizationId(smsLog.getOrganization().getId());
            messageService.sendMessage(smsMt);
        }

        return CONTINUE_PROCESSING;
    }

    protected void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    protected void setCampaignService(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    protected void setContactsService(ContactsService contactsService) {
        this.contactsService = contactsService;
    }
}
