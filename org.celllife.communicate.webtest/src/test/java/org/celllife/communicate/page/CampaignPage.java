package org.celllife.communicate.page;

import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.openqa.selenium.ElementNotVisibleException;

public class CampaignPage extends EntityListPage {

	public CampaignPage(BasePage previousPage, boolean isAdmin) {
		super(previousPage, isAdmin ? "All Campaigns" : "My Campaigns",
				"campaign-");
	}

	public CampaignWizardPage1 goCreatePage() {
		log.info("Navigate to Campaign create page");

		clickElementById("newCampaignButton");
		return new CampaignWizardPage1(this);
	}

	public CampaignWizardPage4 createRelativeCampaign(String name, String organizationName,
			String description, String welcomeMessage, int duration,
			int timesPerDay, String... times) {
		return goCreatePage().selectType(CampaignType.DAILY).goNext()
				.fillForm(name, organizationName, description).goNext()
				.fillForm(welcomeMessage, duration, timesPerDay, times)
				.goNext();
	}

	public CampaignWizardPage4 createGenericCampaign(String name, String organizationName,
			String description, String welcomeMessage, List<CampaignMessage> messages) {
		CampaignWizardPage4 page4 = goCreatePage().selectType(CampaignType.FLEXI).goNext()
				.fillForm(name, organizationName, description).goNext()
				.fillForm(welcomeMessage, null, null, (String[]) null)
				.goNext();

		if (messages != null) {
			for (CampaignMessage message : messages) {
				page4.addMessage(message);
			}
		}

		return page4;
	}

	public CampaignWizardPage1 editCampaign(String string) {
		clickElementById(string);
		return new CampaignWizardPage1(this);
	}

	public CampaignManageRecipientsPage manageRecipients(Long id) {
		try {
			clickElementById("manage_recipients-" + id);
		} catch (ElementNotVisibleException e) {
			// If the element is not visible, might be in expandable-menu
			clickExpandMenuItem(id, "manage-recipients");
		}
		return new CampaignManageRecipientsPage(this);
	}

	public CampaignPage startCampaign(Long id) {
		clickElementById("start_stop-" + id);
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Are you sure"));
		clickMessageBoxYes();
		return this;
	}

	public int getRecipientCount(Long id) {
		String count = driver().findElementById("campaign_recipients-" + id).getText();
		return Integer.parseInt(count);
	}
}
