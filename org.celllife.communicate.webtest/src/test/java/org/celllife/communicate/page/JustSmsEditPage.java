package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class JustSmsEditPage extends LoggedInPage {

	public JustSmsEditPage(BasePage previousPage) {
		super(previousPage);
	}
	
	@Override
	protected void checkAtPage() {
		try {
			WebElement titleLabelText = driver().findElementById("titleLabelText");
			Assert.assertEquals("Just Send SMS Campaign", titleLabelText.getText() );
			driver().findElementById("campaignName");
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}
	
	public CampaignManageRecipientsPage clickSelectContacts() {
		clickElementById("contactsBtnAdapter");
		return new CampaignManageRecipientsPage(this);
	}

	public JustSmsEditPage fillForm(String campaignName, String msgText) {
		log.info("Filling out Just Send SMS form ..");
		driver().findElementById("campaignName-input").sendKeys(campaignName);
		driver().findElementById("smsBoxText-input").sendKeys(msgText);
		return this;
	}
	
	public JustSmsSummaryPage saveAndContinue() {
		clickElementById("submitButton");
		return  new JustSmsSummaryPage(this);
	}
	
	public JustSmsPage cancel() {
		clickElementById("cancelButton");
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("There are unsaved changes"));
		clickMessageBoxYes();
		return new JustSmsPage(this, false);
	}
	
}
