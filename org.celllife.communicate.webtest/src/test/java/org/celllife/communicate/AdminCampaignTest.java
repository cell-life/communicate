package org.celllife.communicate;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.celllife.communicate.page.AddContactWizardPage1;
import org.celllife.communicate.page.CampaignManageRecipientsPage;
import org.celllife.communicate.page.CampaignPage;
import org.celllife.communicate.page.CampaignWizardPage1;
import org.celllife.communicate.page.CampaignWizardPage4;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.communicate.util.CommonHelper;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.NoSuchElementException;

import au.com.bytecode.opencsv.CSVWriter;

public class AdminCampaignTest extends AbstractBaseTest {

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	public CampaignPage goAdminCampaigns(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goAdminPage().goAdminCampaignsPage();
	}


	@Test
	public void testVoidCampaign(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setType(CampaignType.DAILY);
		db().saveOrgUpdate(campaign);

		CampaignPage page = goAdminCampaigns().clickVoidButton(campaign.getId());
		Assert.assertFalse(page.idVisible("void-" + campaign.getId()) );
		page.setVoidFilter("Deleted");
		Assert.assertTrue(page.idVisible("unvoid-" + campaign.getId()) );

		campaign = db().getUniqueEntity(Campaign.class, campaign.getId());
		Assert.assertTrue(campaign.isVoided());
	}

	@Test
	public void testUnVoidCampaign(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setVoided(true);
		campaign.setType(CampaignType.DAILY);
		db().saveOrgUpdate(campaign);

		CampaignPage page = goAdminCampaigns().setVoidFilter("All");
		page.clickUnvoidButton(campaign.getId());
		Assert.assertFalse(page.idVisible("unvoid-" + campaign.getId()) );
		Assert.assertTrue(page.idVisible("void-" + campaign.getId()) );

		campaign = db().getUniqueEntity(Campaign.class, campaign.getId());
		Assert.assertFalse(campaign.isVoided());
	}

	@Test
	public void testVoidActiveCampaign(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setStatus(CampaignStatus.ACTIVE);
		campaign.setType(CampaignType.DAILY);
		db().saveOrgUpdate(campaign);

		boolean buttonEnabled = goAdminCampaigns().isButtonEnabled("void-" + campaign.getId());
		Assert.assertFalse(buttonEnabled);
	}

	@Test
	public void testCreateRelativeCampaign(){
		String name = "test campaign";
		CampaignWizardPage4 wizardPage4 = goAdminCampaigns().createRelativeCampaign(name,
				"Admin organisation",  "description", "welcome message", 10, 2, "08:00",
				"20:00");
		CampaignPage campaignPage = wizardPage4.finish();

		Campaign campaign = db().getUniqueEntity(Campaign.class, Campaign.PROP_NAME, name);
		Assert.assertTrue(campaignPage.isInList(campaign.getId()));
	}

	@Test
	public void testDeleteGenericCampaignMessage(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setStatus(CampaignStatus.ACTIVE);
		campaign.setType(CampaignType.FLEXI);
		db().saveOrgUpdate(campaign);

		try {
			List<CampaignMessage> campaignMessages = getCampaignMessages(2, 3);
			for (CampaignMessage campaignMessage : campaignMessages) {
				campaignMessage.setCampaign(campaign);
				db().saveOrgUpdate(campaignMessage);
			}

			CampaignPage adminCampaignsList = goAdminCampaigns();
			CampaignWizardPage1 step1 = adminCampaignsList
					.editCampaign("campaign-" + campaign.getId());
			CampaignWizardPage4 step4 = step1.goStep4().deleteCampaignMessage(
					campaignMessages.get(2));
			Assert.assertFalse(step4.isInList(campaignMessages.get(2).getId()));

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	@Test
	public void testCannotDeleteRelativeCampaignMessage(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setStatus(CampaignStatus.ACTIVE);
		campaign.setType(CampaignType.DAILY);
		db().saveOrgUpdate(campaign);

		try {
			List<CampaignMessage> campaignMessages = getCampaignMessages(2, 3);
			for (CampaignMessage campaignMessage : campaignMessages) {
				campaignMessage.setCampaign(campaign);
				db().saveOrgUpdate(campaignMessage);
			}

			CampaignPage adminCampaignsList = goAdminCampaigns();
			CampaignWizardPage1 step1 = adminCampaignsList
					.editCampaign("campaign-" + campaign.getId());
			CampaignWizardPage4 step4 = step1.goStep4();
			thrown.expect(NoSuchElementException.class);
			thrown.expectMessage("delete");
			step4.deleteCampaignMessage(campaignMessages.get(2));
			Assert.assertTrue(step4.isInList(campaignMessages.get(2).getId()));

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Ignore("Disable due to inability to upload file from remote server")
	@Test
	public void testImportMessagesForRelativeCampaign() throws Exception{
		testImportMessagesForCampaign(CampaignType.DAILY);
	}

	@Test
	public void testCreateGenericCampaign() throws ParseException{
		String name = "test campaign";

		List<CampaignMessage> messages = getCampaignMessages(null, 3);

		CampaignWizardPage4 wizardPage4 = goAdminCampaigns().createGenericCampaign(name,
				"Admin organisation",  "description", "welcome message", messages);
		CampaignPage campaignPage = wizardPage4.finish();

		Campaign campaign = db().getUniqueEntity(Campaign.class, Campaign.PROP_NAME, name);
		Assert.assertTrue(campaignPage.isInList(campaign.getId()));
	}

	@Ignore("Disable due to inability to upload file from remote server")
	@Test
	public void testImportMessagesForGenericCampaign() throws Exception{
		testImportMessagesForCampaign(CampaignType.FLEXI);
	}

	public void testImportMessagesForCampaign(CampaignType type) throws Exception{
		List<CampaignMessage> messages = getCampaignMessages(
				type == CampaignType.DAILY ? 2 : null, 20);

		File file = createMessageImportFile(messages);

		String name = "test campaign";
		CampaignWizardPage4 wizardPage4 = null;
		if (CampaignType.FLEXI == type){
			wizardPage4 = goAdminCampaigns().createGenericCampaign(name,
					"Admin organisation",  "description", "welcome message", null);
		} else if (CampaignType.DAILY == type){
			wizardPage4 = goAdminCampaigns().createRelativeCampaign(name,
					"Admin organisation",  "description", "welcome message", 10, 2, "08:00",
					"20:00");
		}

		String baseUrl = CommonHelper.getTestURL();
		wizardPage4.importMessages(baseUrl + "/images/" + file.getName());
		int campaignCost = wizardPage4.getCampaignCost();
		Assert.assertEquals(41, campaignCost);

		CampaignPage campaignPage = wizardPage4.finish();

		Campaign campaign = db().getUniqueEntity(Campaign.class, Campaign.PROP_NAME, name);
		Assert.assertTrue(campaignPage.isInList(campaign.getId()));

		List<CampaignMessage> dbMessages = db().getEntities(CampaignMessage.class, CampaignMessage.PROP_CAMPAIGN, campaign);
		Assert.assertEquals(messages.size(), dbMessages.size());

		if (!file.delete()){
			file.deleteOnExit();
		}
	}

	@Test
	public void testManageRecipientsAddNew() {
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setType(CampaignType.FLEXI);
		db().saveOrgUpdate(campaign);

		CampaignPage campaignPage = goAdminCampaigns().startCampaign(campaign.getId());
		Assert.assertEquals(0, campaignPage.getRecipientCount(campaign.getId()) );
		CampaignManageRecipientsPage page = campaignPage.manageRecipients(campaign.getId());
		String msisdn = "27210001984";
		page.addNewContact(msisdn, "George", "Orwell");
		Contact newContact = db().getUniqueEntity(Contact.class, Contact.PROP_MSISDN, msisdn);
		Assert.assertEquals("George", newContact.getFirstName());
		
		page.clickDone();

		Assert.assertEquals(1, campaignPage.getRecipientCount(campaign.getId()) );
	}

	@Test
	public void testManageRecipientsAddExisting() {
		// Create test campaign
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setType(CampaignType.FLEXI);
		db().saveOrgUpdate(campaign);
		// Create test contact
		Contact contact = new Contact("27210001066", "", "William", "the Conqueror");
		contact.setOrganization(org);
		db().saveOrgUpdate(contact);

		CampaignPage campaignPage = goAdminCampaigns().startCampaign(campaign.getId());
		Assert.assertEquals(0, campaignPage.getRecipientCount(campaign.getId()) );
		CampaignManageRecipientsPage page = campaignPage.manageRecipients(campaign.getId());
		AddContactWizardPage1 wizardPage1 = page.addAvailableContact(contact);
		wizardPage1.clickFinish();
		page.clickDone();

		Assert.assertEquals(1, campaignPage.getRecipientCount(campaign.getId()) );
	}

	@Test
	public void testManageRecipientsRemoveSelected() {
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setType(CampaignType.FLEXI);
		campaign.setContactCount(1);
		db().saveOrgUpdate(campaign);

		Contact contact = new Contact("27210001234", "", "John", "Doe");
		contact.setOrganization(org);
		db().saveOrgUpdate(contact);
		CampaignContact cContact = new CampaignContact(campaign, contact);
		db().saveOrgUpdate(cContact);

		CampaignPage campaignPage = goAdminCampaigns().startCampaign(campaign.getId());
		Assert.assertEquals(1, campaignPage.getRecipientCount(campaign.getId()) );

		CampaignManageRecipientsPage page = campaignPage.manageRecipients(campaign.getId());
		page.removeSelectedContact(contact);
		page.clickDone();

		Assert.assertEquals(0, campaignPage.getRecipientCount(campaign.getId()) );
	}


	@Test
	public void testManageRecipientsRemoveAll() {
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setType(CampaignType.FLEXI);
		campaign.setContactCount(1);
		db().saveOrgUpdate(campaign);

		Contact contact = new Contact("27210002001", "", "Stanley", "Kubrick");
		contact.setOrganization(org);
		db().saveOrgUpdate(contact);
		CampaignContact cContact = new CampaignContact(campaign, contact);
		db().saveOrgUpdate(cContact);

		CampaignPage campaignPage = goAdminCampaigns().startCampaign(campaign.getId());
		Assert.assertEquals(1, campaignPage.getRecipientCount(campaign.getId()) );

		CampaignManageRecipientsPage page = campaignPage.manageRecipients(campaign.getId());
		page.removeAllContacts();
		page.clickDone();

		Assert.assertEquals(0, campaignPage.getRecipientCount(campaign.getId()) );
	}

	private File createMessageImportFile(List<CampaignMessage> messages) throws Exception {
		File file = File.createTempFile("messages", ".csv");
		CSVWriter writer = new CSVWriter(new FileWriter(file));
		writer.writeNext(new String[] {"message", "day", "time"});

		for (CampaignMessage message : messages) {
			String[] line = new String[] { message.getMessage(), "" + message.getMsgDay(),
					timeFormat.format(message.getMsgTime()) };
			writer.writeNext(line);
		}

		writer.close();

		/*
		 * save file in deployed images folder to bypass need for authentication
		 * when downloading it from a remote machine i.e. when running it via
		 * Selenium grid
		 */
		File dest = new File("target/tomcat6x/webapps/communicate/images");
		FileUtils.moveFileToDirectory(file, dest, true);
		return new File(dest.getAbsolutePath() + File.separator + file.getName());
	}

	/**
	 * Generates a list of CampaignMessages for export to CSV file.
	 *
	 * @return
	 * @throws ParseException
	 */
	private List<CampaignMessage> getCampaignMessages(Integer timesPerDay, int number) throws ParseException {
		List<CampaignMessage> messages = new ArrayList<CampaignMessage>();
		DateTime time = DateTimeFormat.forPattern("HH:mm").parseDateTime("05:00");
		int day = 0;
		String messageBody = CommonHelper.getLoremIpsum(200);

		for (int i = 0; i < number; i++) {
			DateTime msgTime;
			if (timesPerDay != null){
				int slot = i%timesPerDay;
				if (slot == 0) { day++; }

				msgTime = time.plusMinutes(30*slot);
			} else {
				day++;
				msgTime = time.plusMinutes(30);
			}
			messages.add(new CampaignMessage(messageBody + i, day, msgTime.toDate(), null));
		}

		return messages;
	}

}
