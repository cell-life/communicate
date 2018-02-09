package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class OrganizationEditPage extends LoggedInPage {

	public OrganizationEditPage(BasePage previousPage) {
		super(previousPage);
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement titleLabelText = driver().findElementById("titleLabelText");
			Assert.assertEquals("Organisation: Add New Organisation", titleLabelText.getText() );
			driver().findElementById("orgName");
			driver().findElementById("orgAddress");
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}

	public OrganizationEditPage fillForm(String name, String address, String contactPerson, String contactNumber,
			String contactEmail, String threshold) {
		log.info("Filling out organisation form.");
		
		driver().findElementById("orgName-input").sendKeys(name);
		driver().findElementById("orgAddress-input").sendKeys(address);
		driver().findElementById("orgContactPerson-input").sendKeys(contactPerson);
		driver().findElementById("orgContactNumber-input").sendKeys(contactNumber);
		driver().findElementById("orgContactEmail-input").sendKeys(contactEmail);
		driver().findElementById("orgBalanceThreshold-input").sendKeys(Keys.BACK_SPACE, threshold);
		
		return this;
	}

	public OrganizationPage submit() {
		log.info("Submitting organisation form.");
		
		WebElement saveButton = driver().findElementById("submitButton");
		wait_ms(1000); //TODO - investigate why this wait seems to be necessary.
		saveButton.click();
		wait_ms(5000); //TODO - change this to a waitForElement() call(?)
		
		return new OrganizationPage(this);
	}

}
