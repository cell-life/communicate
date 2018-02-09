package org.celllife.communicate.page;


public class OrganizationPage extends EntityListPage {

	public OrganizationPage(BasePage previousPage) {
		super(previousPage, "Organisations", "org-");
	}

	public OrganizationEditPage goCreatePage() {
		log.info("Navigate to Organisation create page");
		
		clickElementById("newOrgButton");
		return new OrganizationEditPage(this);
	}

}
