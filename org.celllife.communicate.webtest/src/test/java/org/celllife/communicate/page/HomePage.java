package org.celllife.communicate.page;

import org.openqa.selenium.WebElement;


public class HomePage extends LoggedInPage {

	public HomePage(BasePage previousPage) {
		super(previousPage);
	}
	
	@Override
	protected void checkAtPage() {
		try {
			WebElement dashboardButton = driver().findElementById("justSMSButton");
			if (dashboardButton == null){
				throw new IllegalStateException("Unable to verify admin page");	
			}
		} catch (Exception e) {
			throw new IllegalStateException("Unable to verify admin page");
		}
	}

	public JustSmsPage goJustSmsPage() {
		log.info("Navigating to JustSms page ..");
		
		clickElementById("justSMSButton");
		return new JustSmsPage(this, false);
	}
	
	public CampaignPage goCampaignsPage() {
		log.info("Navigating to Campaign page ..");
		
		clickElementById("campaignButton");
		return new CampaignPage(this, false);
	}
}
