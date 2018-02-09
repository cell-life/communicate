package org.celllife.communicate;

import org.celllife.communicate.page.ForgotPasswordPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.junit.Assert;
import org.junit.Test;


// Example relevant System Properties
// -DtestUrl=http://127.0.0.1:8181/communicate
// -Dwebdriver.firefox.bin="C:\Program Files\Mozilla Firefox 3.x\firefox.exe"
// -DpropertiesOverride=communicate_override.properties
// -DseleniumHubUrl=http://172.16.23.117:5556/wd/hub
public class LoginTest extends AbstractBaseTest {

	@Test
	public void testLogin_success() {
		LoginPage lp = getLogin();
		lp.loginAsAdmin();
	}

	@Test
	public void testLogin_incorrectLoginDetails() {
		LoginPage lp = getLogin();
		lp = lp.loginAsExpectingError("admin", "123");

		String message = lp.getMessage();
		Assert.assertEquals("Please enter the correct username and password.", message);
	}

	@Test
	public void testLogout() {
		LoginPage lp = getLogin();
		LoggedInPage home = lp.loginAsAdmin();
		home.logout();
	}

	@Test
	public void testNavigateForgotPassword() {
		LoginPage lp = getLogin();
		ForgotPasswordPage fpp = lp.goForgotPassword();
		fpp.goLogin();
	}

	@Test
	public void testLoginWithVoidedUser(){
		String password = "123";
		User user = db().createUser(password);
		user.setVoided(true);
		db().saveOrgUpdate(user);

		LoginPage lp = getLogin();
		lp = lp.loginAsExpectingError(user.getUserName(), password);

		String message = lp.getMessage();
		Assert.assertTrue(message.startsWith("You account has been disabled"));
	}

	@Test
	public void testLoginWithUserFromVoidedOrg(){
		User user = db().getUniqueEntity(User.class, User.PROP_USERNAME, "admin");
		Organization org = user.getOrganization();
		org.setVoided(true);
		db().saveOrgUpdate(org);

		try {
			LoginPage lp = getLogin();
			lp = lp.loginAsExpectingError("admin", "admin");

			String message = lp.getMessage();
			Assert.assertTrue(message.startsWith("You account has been disabled"));
		}
		finally { // Not strictly necessary, but eases debugging/testing after this test has run.
			org.setVoided(false);
			db().saveOrgUpdate(org);
		}
	}

}
