package org.celllife.communicate.page;

import org.openqa.selenium.WebElement;

public class AdminPage extends LoggedInPage {

	public AdminPage(BasePage previousPage) {
		super(previousPage);
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement dashboardButton = driver().findElementById("dashboardButton");
			if (dashboardButton == null){
				throw new IllegalStateException("Unable to verify admin page");
			}
		} catch (Exception e) {
			throw new IllegalStateException("Unable to verify admin page");
		}
	}

	public DashboardPage goDashboardPage(){
		log.info("Navigating to Dashboard page ..");

		clickElementById("dashboardButton");
		return new DashboardPage(this);
	}

	public OrganizationPage goOrganizationPage(){
		log.info("Navigating to Organisations page ..");

		clickElementById("organizationButton");
		return new OrganizationPage(this);
	}

	public UserPage goUserPage(){
		log.info("Navigating to Users page ..");

		clickElementById("userButton");
		return new UserPage(this);
	}

	public RolePage goRolePage(){
		log.info("Navigating to Role page ..");

		clickElementById("roleButton");
		return new RolePage(this);
	}

	public SettingsPage goSettingsPage(){
		log.info("Navigating to Settings page ..");

		clickElementById("settingsButton");
		return new SettingsPage(this);
	}

	public JustSmsPage goAdminJustSmsPage() {
		log.info("Navigating to Admin Just SMS page ..");

		clickElementById("justSMSButton");
		return new JustSmsPage(this, true);
	}

	public CampaignPage goAdminCampaignsPage() {
		log.info("Navigating to Admin campaigns page ..");

		clickElementById("campaignButton");
		return new CampaignPage(this, true);
	}

	public FilterPage goAdminFiltersPage() {
		log.info("Navigating to Admin campaigns page ..");
		
		clickElementById("filterButton");
		return new FilterPage(this, true);
	}	
	
	public LostMessagesPage goLostMessagesPage(){
		log.info("Navigating to Lost Messages page ..");

		clickElementById("lostMessagesButton");
		return new LostMessagesPage(this);
	}

}
