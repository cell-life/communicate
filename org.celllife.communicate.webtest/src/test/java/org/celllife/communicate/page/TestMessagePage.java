package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.WebElement;


/**
 * Deals with org.celllife.mobilisr.client.view.gxt.MyGXTSmsTestDialog
 */
public class TestMessagePage extends BasePage {

	public TestMessagePage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}

	@Override
	protected void checkAtPage() {
		WebElement dialogHeader = driver().findElementById("DialogHeader");
		Assert.assertEquals("Send Test Message", dialogHeader.getText() );
		driver().findElementById("saveButton");
		driver().findElementById("cancelButton");
	}

	public void clickMyNumber() {
		clickElementById("radioMyNumber");
	}

	public void clickAndSetCustomNumber(String MSISDN) {
		driver().findElementByName("msisdnField-input").sendKeys(MSISDN);
	}

	public JustSmsSummaryPage sendNow() {
		clickElementById("saveButton");
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Message successfully sent"));
		clickMessageBoxOk();
		return new JustSmsSummaryPage(this);
	}

	public JustSmsSummaryPage cancel() {
		clickElementById("smsCancelButton");
		return new JustSmsSummaryPage(this);
	}

}
