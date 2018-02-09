package org.celllife.mobilisr.service.action;

import junit.framework.Assert;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class AddToCampaignActionTest extends AbstractServiceTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignDAO campaignDAO;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private ContactDAO contactDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    private Organization org;

    private AddToCampaignAction addToCampaignAction = new AddToCampaignAction();

    private long optinCounter = 0;

    @Before
    public void setup() {

        campaignDAO.setLinkedCampaignId(311L,312L);
        campaignDAO.setLinkedCampaignId(312L,313L);
        campaignDAO.setLinkedCampaignId(313L,311L);

        org = organizationDAO.find(33L);

        addToCampaignAction.setCampaignService(campaignService);
        addToCampaignAction.setContactsService(contactsService);

    }

    @Test
    public void testGetLinkedCampaigns() {

        Assert.assertEquals(3, campaignService.getLinkedCampaignsForCampaign(311L).size());

    }

    /*If they weren't previously added to the first campaign, then add them to the first campaign.*/
    @Test
    public void testAddToCampaign_noPreviousCampaigns() throws Exception {

        // Create test contact and save
        Contact testContact = new Contact("27722543508","MTN","Test","Contact");
        testContact.setOrganization(org);
        contactDAO.save(testContact);

        Context context = createContext("27722543508", org, 311L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        List<CampaignContact> contactList = createCampaignContacts("27722543508",311L);
        addToCampaignAction.executeManagementCommand(context,null,null,campaignService.getCampaign(311L),contactList);

        Assert.assertEquals(new Long(311),contactsService.listAllCampaignsForContact("27722543508").get(0));
        Assert.assertNull(campaignService.getCampaignContact(campaignService.getCampaign(311L), "27722543508").getEndDate());

    }

    /* If they are currently active in the campaign (or one of the linked campaigns), then continue as before. */
    @Test
    public void testAddToCampaign_activeInFirstCampaign() throws Exception {

        // Create contact and add to campaign 311.
        Contact testContact = new Contact("27722543509","MTN","Test","Contact");
        testContact.setOrganization(org);
        contactDAO.save(testContact);
        addContactToCampaign("27722543509", 311L);

        Context context = createContext("27722543509", org, 311L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        List<CampaignContact> contactList = createCampaignContacts("27722543509",311L);
        addToCampaignAction.executeManagementCommand(context,null,null,campaignService.getCampaign(311L),contactList);

        Assert.assertEquals(new Long(311),contactsService.listAllCampaignsForContact("27722543509").get(0));
        Assert.assertNull(campaignService.getCampaignContact(campaignService.getCampaign(311L), "27722543509").getEndDate());

    }

    /* If they were previously added to the campaign, and it is not indicated that they should start where they left off,
    then the linked campaign should be checked (this will be recursive). They should start on the linked campaign. */
    @Test
    public void testAddToCampaign_finishedFirstCampaign() throws Exception {

        // Add contact to campaign 311.
        Contact testContact = new Contact("27722543510","MTN","Test","Contact");
        testContact.setOrganization(org);
        contactDAO.save(testContact);
        addContactToCampaign("27722543510",311L);

        // Mark as finished campaign 311.
        List<String> numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543510");

        List<CampaignContact> contactList = createCampaignContacts("27722543510",311L);
        Context context = createContext("27722543510", org, 311L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        addToCampaignAction.executeManagementCommand(context,null,null,campaignService.getCampaign(311L),contactList);

        Assert.assertNotNull(campaignService.getCampaignContact(campaignService.getCampaign(312L),"27722543510"));

    }

    /* If the final campaign links back to the first campaign, and the user has already received messages from all campaigns, then allow them to restart the first campaign.*/
    @Test
    public void testAddToCampaign_finishedAllCampaigns() throws Exception {

        // Add contact to campaign 311.
        Contact testContact = new Contact("27722543511","MTN","Test","Contact");
        testContact.setOrganization(org);
        contactDAO.save(testContact);
        addContactToCampaign("27722543511", 311L);
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(311L));

        // Mark as finished campaign 311.
        List<String> numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(311L),numbersToEnd,calendar.getTime());

        addContactToCampaign("27722543511", 312L); // Add contact to campaign 312.
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(312L));

        // Mark as finished campaign 312.
        numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(312L),numbersToEnd, calendar.getTime());

        addContactToCampaign("27722543511", 313L); // Add contact to campaign 313.
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(313L));

        // Mark as finished campaign 313.
        numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(313L),numbersToEnd, calendar.getTime());

        // Attempt to subscribe contact to campaign 311.
        List<CampaignContact> contactList = createCampaignContacts("27722543511", 311L);
        Context context = createContext("27722543511", org, 311L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        addToCampaignAction.executeManagementCommand(context,null,null,campaignService.getCampaign(311L),contactList);

        CampaignContact retrievedContact = campaignService.getCampaignContact(campaignService.getCampaign(311L),"27722543511");
        Assert.assertEquals(0,retrievedContact.getProgress());
        retrievedContact = campaignService.getCampaignContact(campaignService.getCampaign(312L),"27722543511");
        Assert.assertEquals(1,retrievedContact.getProgress());
        retrievedContact = campaignService.getCampaignContact(campaignService.getCampaign(313L),"27722543511");
        Assert.assertEquals(1,retrievedContact.getProgress());

    }

    /*If the user has been on all the linked campaigns and there is no final linked campaign, then do nothing (or at least what the system currently does when the user re-subscribes to a campaign they have already completed + please document) */
    @Test
    public void testAddToCampaign_finishedAllCampaigns_lastCampaignHasNoLink() throws Exception {
        campaignDAO.setLinkedCampaignId(313L,null);

        // Add contact to campaign 311.
        Contact testContact = new Contact("27722543511","MTN","Test","Contact");
        testContact.setOrganization(org);
        contactDAO.save(testContact);
        addContactToCampaign("27722543511", 311L);
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(311L));

        // Mark last date of 311 campaign message.
        List<String> numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(311L),numbersToEnd,calendar.getTime());

        addContactToCampaign("27722543511", 312L); // Add contact to campaign 312.
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(312L));

        // Mark last date of 312 campaign message.
        numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(312L),numbersToEnd, calendar.getTime());

        addContactToCampaign("27722543511", 313L); // Add contact to campaign 313.
        campaignDAO.updateCampaignContactsProgress(campaignService.getCampaign(313L));

        // Mark last date of 313 campaign message.
        numbersToEnd = new ArrayList<String>();
        numbersToEnd.add("27722543511");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        campaignDAO.setCampaignContactsDateLastMessageByMsisdn(campaignService.getCampaign(313L),numbersToEnd, calendar.getTime());

        // Attempt to subscribe contact to campaign 312.
        List<CampaignContact> contactList = createCampaignContacts("27722543511", 312L);
        Context context = createContext("27722543511", org, 312L);
        context.put(AddToCampaignAction.RESTART_EXISTING, true);
        addToCampaignAction.executeManagementCommand(context,null,null,campaignService.getCampaign(312L),contactList);

        Assert.assertEquals(1,campaignService.getCampaignContact(campaignService.getCampaign(311L),"27722543511").getProgress());
        Assert.assertEquals(1,campaignService.getCampaignContact(campaignService.getCampaign(312L),"27722543511").getProgress());
        Assert.assertEquals(1,campaignService.getCampaignContact(campaignService.getCampaign(313L),"27722543511").getProgress());
    }

    private void addContactToCampaign(String msisdn, Long campaignId) {
        List<Contact> listToAdd = new ArrayList<Contact>();
        listToAdd.add(contactDAO.searchByOrganizationAndMSISDN(org,msisdn));
        campaignDAO.addContactsToCampaign(campaignService.getCampaign(campaignId),org,listToAdd,true);
    }

    private List<CampaignContact> createCampaignContacts(String msisdn, Long campaignId) {

        List<CampaignContact> contactList = new ArrayList<CampaignContact>();
        CampaignContact campaignContact = new CampaignContact();
        campaignContact.setCampaign(campaignService.getCampaign(campaignId));
        campaignContact.setMsisdn(msisdn);
        campaignContact.setContact(contactDAO.searchByOrganizationAndMSISDN(org, msisdn));
        contactList.add(campaignContact);

        return contactList;
    }

    private Context createContext(String msisdn, Organization org, Long campaignId) {
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

    @Test(timeout=30000)
    public void testAddContactToCampaign() throws InterruptedException {

        Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.ACTIVE, new Date(),"DevTest 104","",org);
        getGeneralDao().saveOrUpdate(campaign);
        Campaign savedCampaign = campaignService.getCampaign(campaignService.getCampaignIdForName("DevTest 104"));

        for (int i = 0; i < 50; i++) {
            Thread t = new Thread(new OptinThread(savedCampaign.getName()));
            t.start();
        }

        do {
            System.out.println("testAddContactToCampaign waiting... threads finished: " + optinCounter);
            Thread.sleep(1000);
        } while (optinCounter < 50);

    }

    class OptinThread implements Runnable {

        String campaignName;

        public OptinThread(String campaign) {
            this.campaignName = campaign;
        }

        @Override
        public void run() {
            try {

                login("admin", "admin");

                Random rnd = new Random();
                int n = 1000000 + rnd.nextInt(9000000);

                List<Contact> contactList = new ArrayList<Contact>();
                Contact contact = new Contact("2772" + n, org);
                getGeneralDao().save(contact);
                contactList.add(contact);

                int delay = 1 + rnd.nextInt(1000);
                Thread.sleep(delay);

                Campaign campaign = campaignService.getCampaign(campaignService.getCampaignIdForName(campaignName));
                List<CampaignContact> campaignContacts = campaignService.convertContactToCampaignContact(contactList, campaign);

                CampaignContact campaignContact = campaignService.saveOrUpdateCampaignContact(campaignContacts.get(0));
                List<CampaignContact> campaignContactList = new ArrayList<CampaignContact>();
                campaignContactList.add(campaignContact);

                addToCampaignAction.addContactToCampaign(campaignContactList, true, campaign);
                CampaignContact campaignContact1 = campaignService.getCampaignContact(campaign, contact.getMsisdn());

                Assert.assertNotNull(campaignContact1);

                optinCounter++;

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }


    @After
    public void tearDown() {

        // This is so the foreign key constraint doesn't cause an issue later on.
        campaignDAO.setLinkedCampaignId(311L,null);
        campaignDAO.setLinkedCampaignId(312L,null);
        campaignDAO.setLinkedCampaignId(313L,null);

    }

}
