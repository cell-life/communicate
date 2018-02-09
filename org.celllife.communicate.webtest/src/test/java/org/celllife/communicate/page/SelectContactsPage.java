package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.WebElement;

public class SelectContactsPage extends BasePage {

	public SelectContactsPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}

	@Override
	protected void checkAtPage() {
		WebElement titleLabelText = driver().findElementById("selectContactsHeader");
		Assert.assertEquals("Select Message Recipients", titleLabelText.getText() );
		driver().findElementById("addAllAnchor");
		driver().findElementById("removeAllAnchor");
	}
	
	public void addNewContact(String newMSISDN) {
		driver().findElementByName("newEntityField").sendKeys(newMSISDN);
		wait_ms(1000);
		clickElementById("addNewEntityBtn");
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains(newMSISDN));
		clickMessageBoxOk();
	}
	
	public void done() {
		clickElementById("doneBtn");
	}
	
	public void cancel() {
		clickElementById("cancelBtn");
	}


}
