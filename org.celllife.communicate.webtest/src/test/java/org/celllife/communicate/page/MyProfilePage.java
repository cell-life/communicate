package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

//TODO: Possibly create a new page, PopupPage, which this extends(?)
// Can't extend LoggedInPage, since we can't access the usual header buttons, etc.
public class MyProfilePage extends BasePage {
	
	public MyProfilePage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement header = driver().findElementById("profileEditHeader");
			Assert.assertEquals("My Profile", header.getText() );
			driver().findElementById("cancelBtn");
			driver().findElementById("doneBtn"); //TODO: check this - might not find it if greyed out.
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}
	
	public void changePassword(String oldPass, String newPass) {
		WebElement fieldset = driver().findElementById("changePass");
		WebElement chkBox = fieldset.findElement(By.xpath("descendant::input[@type = 'checkbox']"));
		chkBox.click();
		driver().findElementById("password-input").sendKeys(oldPass);
		wait_ms(1000);
		driver().findElementById("newPassword-input").sendKeys(newPass);
		driver().findElementById("confirmNewPwd-input").sendKeys(newPass);
	}
	
	public void changeField(String fieldName, String newValue) {
		WebElement input = driver().findElementById(fieldName);
		input.clear();
		input.sendKeys(newValue);
	}
	public void changeUserName(String newUserName) {
		changeField("username-input", newUserName);
	}
	
	public void changeFirstName(String newFirstName) {
		changeField("firstName-input", newFirstName);
	}
	
	public void changeLastName(String newLastName) {
		changeField("lastname-input", newLastName);
	}
	
	public void changeMSISDN(String newMSISDN) {
		changeField("MSISDN-input", newMSISDN);
	}
	
	public void changeEmail(String newEmail) {
		changeField("emailAddress-input", newEmail);
	}
	
	public String getUserName() {
		return driver().findElementById("username-input").getAttribute("value");
	}
	
	public String getFirstName() {
		return driver().findElementById("firstName-input").getAttribute("value");
	}
	
	public String getLastName() {
		return driver().findElementById("lastname-input").getAttribute("value");
	}
	
	public String getMSISDN() {
		return driver().findElementById("MSISDN-input").getAttribute("value");
	}
	
	public String getEmail() {
		return driver().findElementById("emailAddress-input").getAttribute("value");
	}
	
	public String getOrganization() {
		return driver().findElementById("organization-input").getAttribute("value");
	}
	
	public void done() {
		clickElementById("doneBtn");
	}
	
	public void cancel() {
		clickElementById("cancelBtn");
	}

}
