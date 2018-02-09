package org.celllife.communicate.page;

import org.openqa.selenium.WebElement;

public class GroupAddPage extends LoggedInPage {

public GroupAddPage(BasePage previousPage) {
		super(previousPage);
	}

	public GroupAddPage fillForm(String name, String description) {
		
		log.info("Filling out group form.");
		
		driver().findElementByName("groupName").sendKeys(name);
		driver().findElementByName("groupDescription").sendKeys(description);
		
		return this;
	}

	public GroupsPage save() {
		
		log.info("Submitting new group form.");
		WebElement saveButton = driver().findElementById("submitButton");
		wait_ms(1000); //TODO - investigate why this wait seems to be necessary.
		saveButton.click();
		wait_ms(5000); //TODO - change this to a waitForElement() call(?)
		
		return new GroupsPage(this);
	}
}