package org.celllife.communicate.page;


public class AddContactWizardPage1 extends AddContactWizardBase {

	public AddContactWizardPage1(BasePage previousPage) {
		super(previousPage);
	}
	
	public AddContactWizardPage2 clickNext(){
		clickNextButton();
		return new AddContactWizardPage2(this);
	}

	public void fillForm(String number, String firstName, String lastName) {
		if (number != null && !number.isEmpty())
			driver().findElementByName("msisdn").sendKeys(number);
		
		if (firstName != null && !firstName.isEmpty())
			driver().findElementByName("firstName").sendKeys(firstName);
		
		if (lastName != null && !lastName.isEmpty())
			driver().findElementByName("lastName").sendKeys(lastName);
	}

}
