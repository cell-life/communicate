package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.CampaignManageRecipientsPage;
import org.celllife.communicate.page.JustSmsEditPage;
import org.celllife.communicate.page.JustSmsPage;
import org.celllife.communicate.page.JustSmsSummaryPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.TestMessagePage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.communicate.util.CommonHelper;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;
import org.junit.Test;

public class JustSMSTest extends AbstractBaseTest {

	public JustSmsPage goJustSms(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goMyCampaignPage().goJustSmsPage();
	}

	@Test
	public void testVoidJustSms(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		db().saveOrgUpdate(campaign);

		JustSmsPage page = goJustSms().clickVoidButton(campaign.getId());
		boolean isInList = page.isInList(campaign.getId());
		Assert.assertFalse(isInList);

		campaign = db().getUniqueEntity(Campaign.class, campaign.getId());
		Assert.assertTrue(campaign.isVoided());
	}

	@Test
	public void testUnVoidJustSms(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setVoided(true);
		db().saveOrgUpdate(campaign);

		JustSmsPage page = goJustSms();
		page.makeComboSelection("void_filter", "Deleted");
		page = page.clickUnvoidButton(campaign.getId());
		boolean isInList = page.isInList(campaign.getId());
		Assert.assertFalse(isInList);

		campaign = db().getUniqueEntity(Campaign.class, campaign.getId());
		Assert.assertFalse(campaign.isVoided());
	}

	@Test
	public void testVoidActiveJustSms(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		Organization org = db().getAdminOrganization();
		campaign.setOrganization(org);
		campaign.setStatus(CampaignStatus.SCHEDULED);
		db().saveOrgUpdate(campaign);

		boolean buttonEnabled = goJustSms().isButtonEnabled("void-" + campaign.getId());
		Assert.assertFalse(buttonEnabled);
	}

	@Test
	public void testAddJustSms() {
		JustSmsEditPage edPage = goJustSms().goCreatePage();
		String campaignName = "TestCampaignName";
		edPage.fillForm(campaignName, "SMS message text: " + CommonHelper.getLoremIpsum(50));
		CampaignManageRecipientsPage contactsPage = edPage.clickSelectContacts();
		contactsPage.addNewContact("27878686237", "", ""); //27-TSTNUMBER
		contactsPage.clickDone();
		JustSmsSummaryPage summaryPage = edPage.saveAndContinue();
		JustSmsPage page = summaryPage.schedule();

		String refString = "Campaign: " + campaignName + " scheduled successfully";
		String successMessage = page.getSuccessMessage();
		Assert.assertEquals(refString, successMessage);

		boolean exists = db().checkEntityExists(Campaign.class, Campaign.PROP_NAME, campaignName);
		Assert.assertTrue(exists);
	}

	@Test
	public void testAddJustSms_cancel() {
		JustSmsEditPage edPage = goJustSms().goCreatePage();
		String campaignName = "TestCampaignName";
		edPage.fillForm(campaignName, "SMS message text: " + CommonHelper.getLoremIpsum(50));
		edPage.cancel();

		boolean exists = db().checkEntityExists(Campaign.class, Campaign.PROP_NAME, campaignName);
		Assert.assertFalse(exists);
	}

	@Test
	public void testAddJustSms_withTestMsg() {
		JustSmsEditPage edPage = goJustSms().goCreatePage();
		String campaignName = "TestCampaignName";
		edPage.fillForm(campaignName, "SMS message text: " + CommonHelper.getLoremIpsum(50));
		CampaignManageRecipientsPage contactsPage = edPage.clickSelectContacts();
		contactsPage.addNewContact("27878686237", "", ""); //27-TSTNUMBER
		contactsPage.clickDone();
		JustSmsSummaryPage summaryPage = edPage.saveAndContinue();

		TestMessagePage testPage = summaryPage.clickSendTestMsg();
		testPage.clickMyNumber();
		summaryPage = testPage.sendNow();

		JustSmsPage page = summaryPage.schedule();

		String refString = "Campaign: " + campaignName + " scheduled successfully";
		String successMessage = page.getSuccessMessage();
		Assert.assertEquals(refString, successMessage);

		boolean exists = db().checkEntityExists(Campaign.class, Campaign.PROP_NAME, campaignName);
		Assert.assertTrue(exists);
	}

}
