package org.celllife.communicate.page;

public class CampaignWizardPage extends LoggedInPage {

	private int stepNumber;

	public CampaignWizardPage(BasePage previousPage, int stepNumber) {
		super(previousPage);
		this.stepNumber = stepNumber;
		checkAtPage();
	}
	
	@Override
	protected void checkAtPage() {
		if (stepNumber <= 0){
			return;
		}
		
		String title = driver().findElementById("titleLabelText").getText();
		if (!title.equals("Campaign Wizard")){
			throw new IllegalArgumentException("Unable to verify campaign wizard page");
		}
		
		String stepTitle = driver().findElementById("wizardStepTitle").getText();
		if (!stepTitle.endsWith("Step "+stepNumber+" of 4")){
			throw new IllegalArgumentException("Unable to verify campaign wizard step " + stepNumber);
		}
	}
	
	public CampaignWizardPage1 goStep1(){
		goStep(1);
		return new CampaignWizardPage1(this);
	}
	
	public CampaignWizardPage2 goStep2(){
		goStep(1);
		return new CampaignWizardPage2(this);
	}
	
	public CampaignWizardPage3 goStep3(){
		goStep(1);
		return new CampaignWizardPage3(this);
	}
	
	public CampaignWizardPage4 goStep4(){
		goStep(4);
		return new CampaignWizardPage4(this);
	}
	
	public CampaignPage cancel(){
		clickElementById("cancelButton");
		clickMessageBoxYes();
		return new CampaignPage(this, true);
	}
	
	private void goStep(int i) {
		clickElementById("wizardStep-" + i);
	}

}
