package org.celllife.communicate.page;

import junit.framework.Assert;

public abstract class AddContactWizardBase extends LoggedInPage {

	public AddContactWizardBase(BasePage previousPage) {
		super(previousPage);
	}

	protected void clickPreviousButton() {
		clickElementById("prevButton");
	}

	protected void clickNextButton() {
		String text = getButtonText("nextButton");
		Assert.assertEquals("Next", text);
		clickElementById("nextButton");
	}

	public CampaignManageRecipientsPage clickCancel() {
		clickElementById("cancelButton");
		return new CampaignManageRecipientsPage(this);
	}


	public CampaignManageRecipientsPage clickFinish() {
		String text = getButtonText("nextButton");
		Assert.assertEquals("Finish", text);
		clickElementById("nextButton");
		return new CampaignManageRecipientsPage(this);
	}

}