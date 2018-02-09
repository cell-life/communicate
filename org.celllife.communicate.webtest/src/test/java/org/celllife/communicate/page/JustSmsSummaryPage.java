package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class JustSmsSummaryPage extends LoggedInPage {

	public JustSmsSummaryPage(BasePage previousPage) {
		super(previousPage);
	}
	
	@Override
	protected void checkAtPage() {
		try {
			WebElement titleLabelText = driver().findElementById("titleLabelText");
			//TODO: figure out how to pass campaignName in here
			//Assert.assertEquals("Campaign Summary: " + campaignName, titleLabelText.getText() );
			Assert.assertTrue(titleLabelText.getText().contains("Campaign Summary:") );
			driver().findElementById("btnSave");
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}

	public JustSmsPage schedule() {
		clickElementById("btnSave");
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Do you want to start"));
		clickMessageBoxYes();
		return new JustSmsPage(this, false);
	}

	public TestMessagePage clickSendTestMsg() {
		clickElementById("checkCampaignAdapter");
		return new TestMessagePage(this);
	}

}
