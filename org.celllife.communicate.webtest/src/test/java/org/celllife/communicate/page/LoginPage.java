package org.celllife.communicate.page;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LoginPage extends BasePage {
	
	public LoginPage(RemoteWebDriver driver) {
		super(driver);
		checkAtPage();
	}

	public LoginPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}
	

	protected void checkAtPage() {
		try {
			WebElement heading = driver().findElement(By.id("login_heading"));
			Assert.assertEquals("Login page heading doesn't match expected text",
					"Communicate Login", heading.getText() );
			
			driver().findElement(By.name("j_username"));
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}
	
	public LoggedInPage loginAs(String username, String password) {
		submitLoginForm(username, password);
		return new LoggedInPage(this);
	}

	/**
	 * @param username
	 * @param password
	 */
	private void submitLoginForm(String username, String password) {
		// NB: Order important here. With FF4, for some strange reason, if sendKeys() is first 
		// called on username field, WebDriver doesn't select the element first, and ends up typing
		// into the address bar. Doing sendKeys() on the password field first seems to fix this.
		// The other option that seems to work is first calling click() on the element.
		//     This behaviour is possibly caused by the setFocus() Javascript on the login page,
		// since removing the onload call to setFocus() on the login page also appears to fix this.
		WebElement inputPass = driver().findElement(By.name("j_password"));
		inputPass.sendKeys(password);
		WebElement inputUser = driver().findElement(By.name("j_username"));
		inputUser.sendKeys(username);
		
		log.info("Logging in as {}:{}", username, password);
		inputPass.submit();
	}
	
	public LoggedInPage loginAsAdmin(){
		return loginAs("admin", "admin");
	}

	public LoginPage loginAsExpectingError(String username, String password) {
		submitLoginForm(username, password);
		return new LoginPage(this);
	}

	public String getMessage() {
		WebElement error = driver().findElementById("message");
		return error.getText();
	}

	public ForgotPasswordPage goForgotPassword() {
		log.info("Navigating to forgot password page");
		// The "forgot password" link should take us to a reset password form.
		WebElement forgotPassLink = driver().findElementByLinkText("Forgot password?");
		forgotPassLink.click();
		wait_ms(500);
		return new ForgotPasswordPage(this);
	}
}
