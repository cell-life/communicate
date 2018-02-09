package org.celllife.communicate.page;

import org.celllife.mobilisr.domain.User;

import junit.framework.Assert;


public class UserPage extends EntityListPage {

	public UserPage(BasePage previousPage) {
		super(previousPage, "Users", "user-");
	}

	public UserEditPage goCreatePage() {
		log.info("Navigate to USer create page");

		clickElementById("newUserButton");
		return new UserEditPage(this);
	}

	public UserPage voidUser(User user) {
		clickVoidButton(user.getId());
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("Expected message dialog", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("deactivate user"));
		Assert.assertTrue(messageBoxText.contains(user.getUserName()));
		clickMessageBoxYes();
		return new UserPage(this);
	}

	public GroupsPage goGroupsCampaignsPage() {
		log.info("Navigating to Groups page ..");
		clickElementById("newGroupButton");
		return new GroupsPage(this);
	}

}
