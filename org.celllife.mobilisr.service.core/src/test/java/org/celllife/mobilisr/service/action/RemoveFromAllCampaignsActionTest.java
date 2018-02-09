package org.celllife.mobilisr.service.action;

import junit.framework.Assert;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.mock.MockLogger;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoveFromAllCampaignsActionTest extends AbstractServiceTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ContactsService contactsService;

    private RemoveFromAllCampaignsAction removeFromAllCampaignsAction = new RemoveFromAllCampaignsAction();

    @Before
    public void setup() {
        removeFromAllCampaignsAction.setCampaignService(campaignService);
        removeFromAllCampaignsAction.setMessageService(messageService);
        removeFromAllCampaignsAction.setContactsService(contactsService);
    }

    @Test
    public void removeFromCampaignTest() throws Exception {
        Organization org = new Organization();
        org.setName("Cell-Life");
        Context context = createContext("27724194158", org, 1L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        removeFromAllCampaignsAction.execute(context);

        List<Long> campaignContactIds = contactsService.listAllCampaignsForContact("27724194158");
        Campaign campaign = campaignService.getCampaign(campaignContactIds.get(0).longValue());
        CampaignContact campaignContact = campaignService.getCampaignContact(campaign,"27724194158");

        Assert.assertTrue(campaignContact.getEndDate().before(new Date()));
    }

    @Test
    public void removeFromCampaignTest_Fail() throws Exception {

        Organization org = new Organization();
        org.setName("Cell-Life");
        Context context = createContext("27724194157", org, 1L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);

        Boolean b = removeFromAllCampaignsAction.execute(context);

        Assert.assertEquals(false, b.booleanValue()); // Check that no exceptions are thrown when the number doesn't exist.
    }

    protected Context createContext(String msisdn, Organization org, Long campaignId) {
        Context context = new ContextBase();
        MessageFilter filter = new MessageFilter();
        context.put(Action.FILTER, filter);
        SmsLog smsLog = new SmsLog();
        smsLog.setMsisdn(msisdn);
        smsLog.setOrganization(org);
        context.put(Action.SMS_LOG, smsLog);
        Contact contact = new Contact(msisdn, org);
        context.put(GetContactCommand.CONTACT, contact);
        context.put(CampaignManagementCommand.CAMPAIGN_ID, campaignId);
        return context;
    }
}
