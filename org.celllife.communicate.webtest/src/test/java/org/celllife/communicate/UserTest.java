package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.AdminPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.MyProfilePage;
import org.celllife.communicate.page.UserPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.junit.Test;

public class UserTest extends AbstractBaseTest {

	private User user;
	private LoggedInPage homePage;

	public UserPage goUser() {
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAs("admin", "admin");
		AdminPage adminPage = hp.goAdminPage();
		return adminPage.goUserPage();
	}

	private MyProfilePage createUserGoProfile(String password) {
		user = db().createUser(password);
		LoginPage lp = getLogin();
		homePage = lp.loginAs(user.getUserName(), password);
		return homePage.goMyProfilePage();
	}

	@Test
	public void testVoidUser() {
		User user = db().createTestEntity(User.class);
		UserPage page = goUser().voidUser(user);
		boolean isInList = page.isInList(user.getId());
		Assert.assertFalse(isInList);

		user = db().getUniqueEntity(User.class, user.getId());
		Assert.assertTrue(user.isVoided());
	}

	@Test
	public void testUnVoidUser(){
		User user = db().createTestEntity(User.class);
		user.setVoided(true);
		db().saveOrgUpdate(user);

		UserPage page = goUser().showVoided().clickUnvoidButton(user.getId());
		boolean isInList = page.isInList(user.getId());
		Assert.assertFalse(isInList);

		user = db().getUniqueEntity(User.class, user.getId());
		Assert.assertFalse(user.isVoided());
	}

	@Test
	public void testVoidLoggedInUser(){
		User adminUser = db().getUniqueEntity(User.class, User.PROP_USERNAME, "admin");

		UserPage page = goUser().clickVoidButton(adminUser.getId());
		String messageBoxText = page.getMessageBoxText();
		Assert.assertNotNull("Expected error dialog",messageBoxText);
	}

	@Test
	public void testUserNotVisibleForVoidedOrganization() {
		User user = db().createTestEntity(User.class);
		Organization org = user.getOrganization();
		org.setVoided(true);
		db().saveOrgUpdate(org);

		UserPage userPage = goUser();
		boolean isInList = userPage.isInList(user.getId());
		Assert.assertFalse(isInList);

		boolean isInVoidedList = userPage.showVoided().isInList(user.getId());
		Assert.assertTrue(isInVoidedList);

		boolean buttonEnabled = userPage.isButtonEnabled("void-"+user.getId());
		Assert.assertFalse(buttonEnabled);
	}

	@Test
	public void testProfileChangePassword() {
		String password = "123";

		User user = db().createUser(password);
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAs(user.getUserName(), password);
		MyProfilePage pp = hp.goMyProfilePage();
		pp.changePassword(password, "123456");
		pp.done();
		lp = hp.logout();
		// Check new password works
		hp = lp.loginAs(user.getUserName(), "123456");
		hp.logout();
	}

	@Test
	public void testProfileChangeUserName() {
		String password = "123456";
		MyProfilePage pp = createUserGoProfile(password);

		String newUserName = user.getUserName() + "Edit";
		pp.changeUserName(newUserName);
		pp.done();
		LoginPage lp = homePage.logout();
		// Check new username works
		homePage = lp.loginAs(newUserName, password);
		homePage.logout();
	}

	@Test
	public void testProfileChangeFirstName() {
		String password = "123456";
		MyProfilePage pp = createUserGoProfile(password);

		String newFirstName = user.getFirstName() + "Edit";
		pp.changeFirstName(newFirstName);
		pp.done();
		// Check change
		pp = homePage.goMyProfilePage();
		String firstName = pp.getFirstName();
		Assert.assertEquals(newFirstName, firstName);
		pp.cancel();
		LoginPage lp = homePage.logout();
		homePage = lp.loginAs(user.getUserName(), password);
		Assert.assertTrue(homePage.getWelcomeLabel().contains(newFirstName));
	}

	@Test
	public void testProfileChangeLastName() {
		String password = "123456";
		MyProfilePage pp = createUserGoProfile(password);

		String newLastName = user.getLastName() + "Edit";
		pp.changeLastName(newLastName);
		pp.done();
		// Check change
		pp = homePage.goMyProfilePage();
		Assert.assertEquals(newLastName, pp.getLastName());
		pp.cancel();
		LoginPage lp = homePage.logout();
		homePage = lp.loginAs(user.getUserName(), password);
		Assert.assertTrue(homePage.getWelcomeLabel().contains(newLastName));
	}

	@Test
	public void testProfileChangeMSISDN() {
		String password = "123456";
		MyProfilePage pp = createUserGoProfile(password);

		String newMSISDN = Long.toString( Long.parseLong(user.getMsisdn()) + 1 );
		pp.changeMSISDN(newMSISDN);
		pp.done();
		// Check change
		pp = homePage.goMyProfilePage();
		Assert.assertEquals(newMSISDN, pp.getMSISDN());
		pp.cancel();
	}

	@Test
	public void testProfileChangeEmailAddress() {
		String password = "123456";
		MyProfilePage pp = createUserGoProfile(password);

		String newEmail = "newaddress@cell-life.org";
		pp.changeEmail(newEmail);
		pp.done();
		// Check change
		pp = homePage.goMyProfilePage();
		Assert.assertEquals(newEmail, pp.getEmail());
		pp.cancel();
	}

}
