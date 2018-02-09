package org.celllife.communicate.page;

public class AddContactWizardPage2 extends AddContactWizardBase {

	public AddContactWizardPage2(BasePage previousPage) {
		super(previousPage);
	}
	
	public AddContactWizardPage1 clickPrevious(){
		clickPreviousButton();
		return new AddContactWizardPage1(this);
	}

}
