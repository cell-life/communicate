package org.celllife.communicate.page;

import org.celllife.mobilisr.domain.Campaign;

public class CampaignWizardPage2 extends CampaignWizardPage {

	public CampaignWizardPage2(BasePage previousPage) {
		super(previousPage,2);
	}
	
	public CampaignWizardPage2 fillForm(String name, String organizationName,
			String description) {
		driver().findElementById("campaignName-input").sendKeys(name);
		makeComboSelection(Campaign.PROP_ORGANIZATION, organizationName);
		if (description != null){
			driver().findElementById("campaignDescription-input").sendKeys(description);
		}
		return this;
	}
	
	public CampaignWizardPage1 goBack(){
		clickElementById("backButton");
		return new CampaignWizardPage1(this);
	}
	
	public CampaignWizardPage2 save(){
		clickElementById("saveButton");
		return this;
	}
	
	public CampaignWizardPage3 goNext(){
		clickElementById("nextButton");
		return new CampaignWizardPage3(this);
	}
	
}
