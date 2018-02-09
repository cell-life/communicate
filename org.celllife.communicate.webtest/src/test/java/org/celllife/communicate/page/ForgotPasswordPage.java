package org.celllife.communicate.page;

import org.openqa.selenium.WebElement;


public class ForgotPasswordPage extends BasePage {

	public ForgotPasswordPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}
	
	@Override
	protected void checkAtPage() {
		driver().findElementById("email"); // Should throw exception if not present.
	}

	public LoginPage goLogin() {
		log.info("Navigating to login page");
		// The login link should take us back to the login page.
		WebElement loginLink = driver().findElementByLinkText("Return to login screen");
		loginLink.click();
		wait_ms(500);
		
		return new LoginPage(this);
	}

}
