package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class ContactsPage extends LoggedInPage {

	public ContactsPage(BasePage previousPage) {
		super(previousPage);
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement headerTitle = driver().findElementById("headerTitle");
			Assert.assertEquals("My Contacts", headerTitle.getText() );
			WebElement clickText = driver().findElementById("clickTextLabel");
			Assert.assertTrue(clickText.getText().contains("Click on a contact"));
			WebElement searchLabel = driver().findElementById("searchEntityListLabel");
			Assert.assertEquals("Search for a contact", searchLabel.getText() );
			driver().findElementById("newPersonButton");
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}
	
	public GroupsPage goGroupsPage() {
		log.info("Navigating to Groups page ..");
		
		WebElement groupsButton = driver().findElementById("myGroupButton");
		groupsButton.click();
		wait_ms(1000);
		return new GroupsPage(this);
	}

}
