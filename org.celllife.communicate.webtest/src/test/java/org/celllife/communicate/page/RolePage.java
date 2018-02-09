package org.celllife.communicate.page;

import org.celllife.mobilisr.domain.MobilisrPermission;

public class RolePage extends EntityListPage {

	public RolePage(BasePage previousPage) {
		super(previousPage, "Roles", "role-");
	}

	public RolePage deleteRole(Long id) {
		selectItemInList(id);
		clickDeleteButton();
		return this;
	}

	public RolePage deleteRoleExpectConfirmation(Long id) {
		selectItemInList(id);
		clickDeleteButton();
		clickMessageBoxYes();
		return this;
	}

	public RolePage clickDeleteButton() {
		clickElementById("deleteRoleButton");
		return this;
	}

	public RolePage createRole(String name, MobilisrPermission... permissions) {
		clickElementById("newRoleButton");
		driver().findElementByName("name").sendKeys(name);

		for (MobilisrPermission perm : permissions) {
			clickElementById(perm.name());
		}

		clickElementById("submitButton");
		return this;
	}

}
