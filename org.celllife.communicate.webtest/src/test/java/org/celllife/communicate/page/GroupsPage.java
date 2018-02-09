package org.celllife.communicate.page;

import org.celllife.mobilisr.domain.ContactGroup;

public class GroupsPage extends EntityListPage {

	public GroupsPage(BasePage previousPage) {
		super(previousPage, "My Groups", "groups-");
	}

	public GroupAddPage goCreatePage() {
		log.info("Navigate to Add New Groups page");
		clickElementById("newGroupButton");
		return new GroupAddPage(this);
	}

	public GroupsPage deleteGroup(ContactGroup contactGroup) {
		try {
			clickElementById("deletebutton-"+contactGroup.getId());
		} catch (org.openqa.selenium.ElementNotVisibleException e) {
			clickExpandMenuItemByText("group-"+contactGroup.getId(), "Delete Group");
		}
		return this;
	}

}