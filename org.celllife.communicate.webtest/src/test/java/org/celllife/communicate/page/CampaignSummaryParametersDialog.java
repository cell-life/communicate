package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CampaignSummaryParametersDialog extends BasePage {

	public CampaignSummaryParametersDialog(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}
	
	@Override
	protected void checkAtPage() {
		WebElement titleLabelText = driver().findElement(By.xpath("//span[contains(@class, 'header-text')"));
		Assert.assertEquals("Parameters for: Campaign Credit Summary", titleLabelText.getText() );
	}

}