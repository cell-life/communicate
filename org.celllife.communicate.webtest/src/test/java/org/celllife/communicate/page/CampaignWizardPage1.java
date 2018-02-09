package org.celllife.communicate.page;

import org.celllife.mobilisr.constants.CampaignType;

public class CampaignWizardPage1 extends CampaignWizardPage {

	public CampaignWizardPage1(BasePage previousPage) {
		super(previousPage, 1);
	}
	
	public CampaignWizardPage1 selectType(CampaignType type){
		clickElementById("type-" + type);
		return this;
	}
	
	public CampaignWizardPage2 goNext(){
		clickElementById("nextButton");
		return new CampaignWizardPage2(this);
	}
}
