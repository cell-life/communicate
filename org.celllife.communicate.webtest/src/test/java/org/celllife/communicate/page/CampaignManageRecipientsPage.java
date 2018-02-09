package org.celllife.communicate.page;

import junit.framework.Assert;

import org.celllife.mobilisr.domain.Contact;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class CampaignManageRecipientsPage extends LoggedInPage {


	public CampaignManageRecipientsPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement headerTitle = driver().findElementById("titleLabel");
			if (headerTitle == null ||
					headerTitle.getText() == null ||
					!headerTitle.getText().contains("Campaign: ")){
				throw new IllegalArgumentException("Unable to verify " +
						"Manage Recipients page");
			}
		}
		catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to verify " +
					"Manage Recipients page");
		}
	}

	public CampaignManageRecipientsPage addNewContact(String number, String firstName,
			String lastName) {
		clickElementById("addContactButton");
		AddContactWizardPage1 page1 = new AddContactWizardPage1(this);
		page1.fillForm(number, firstName, lastName);
		page1.clickFinish();
		return this;
	}

	public void clickCancel() {
		clickElementById("cancelButton");
	}
	
	public void clickDone() {
		clickElementById("doneButton");
	}

	public void removeAllContacts() {
		clickElementById("bulkRemoveButton");
		clickMenuItem("Remove all");
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Are you sure"));
		clickMessageBoxYes();
	}

	public void removeSelectedContact(Contact contact) {
		clickElementById("remove_recipient-" + contact.getId());
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Are you sure"));
		clickMessageBoxYes();
	}

	public AddContactWizardPage1 addAvailableContact(Contact contact) {
		clickElementById("add_contact-" + contact.getId());
		return new AddContactWizardPage1(this);
	}
}
