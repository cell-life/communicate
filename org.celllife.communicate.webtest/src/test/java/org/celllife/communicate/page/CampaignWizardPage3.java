package org.celllife.communicate.page;

public class CampaignWizardPage3 extends CampaignWizardPage {

	public CampaignWizardPage3(BasePage previousPage) {
		super(previousPage, 3);
	}
	
	public CampaignWizardPage3 fillForm(String welcomeMessage, Integer duration,
			Integer timesPerDay, String... times) {
		driver().findElementById("smsBoxText-input").sendKeys(welcomeMessage);
		if (duration != null){
			driver().findElementById("numDays-input").sendKeys(duration.toString());
		}
		if (timesPerDay != null){
			makeComboSelection("messagesPerDay-input", timesPerDay.toString());
		}
		if (times != null){
			for (int i = 0; i < times.length; i++) {
				makeComboSelection("time"+i+"-input", times[i]);
			}
		}
		return this;
	}
	
	public CampaignWizardPage2 goBack(){
		clickElementById("backButton");
		return new CampaignWizardPage2(this);
	}
	
	public CampaignWizardPage3 save(){
		clickElementById("saveButton");
		return this;
	}
	
	public CampaignWizardPage4 goNext(){
		clickElementById("nextButton");
		return new CampaignWizardPage4(this);
	}
	

}
