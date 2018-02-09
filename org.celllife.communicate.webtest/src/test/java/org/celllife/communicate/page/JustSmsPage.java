package org.celllife.communicate.page;

public class JustSmsPage extends EntityListPage {

	public JustSmsPage(BasePage previousPage, boolean isAdmin) {
		super(previousPage, isAdmin ? "All 'Just Send SMS' Campaigns"
				: "My 'Just Send SMS' Campaigns", "justsms-");
	}
	
	public JustSmsEditPage goCreatePage() {
		log.info("Navigate to JustSms create page");
		
		clickElementById("newCampaignButton");
		return new JustSmsEditPage(this);
	}

}
