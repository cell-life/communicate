package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.WebElement;

public class ReportsPage extends LoggedInPage {

	protected String currentReportURL; 
	
	public ReportsPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}	

	@Override
	protected void checkAtPage() {		
		String title = driver().findElementById("headerTitle").getText();
		if (!title.equals("Reports")){
			throw new IllegalArgumentException("Unable to verify Create Filter Page");
		}		
	}
	
	public ReportsPage generateCreditSummaryReport() {

		ReportsPage msgBoxPage = clickGenerateButton("credit_summary");
		
		String messageBoxHeader = msgBoxPage.getMessageBoxHeader();		
		Assert.assertEquals("Parameters for: Credit summary", messageBoxHeader);
		msgBoxPage.clickElementById("saveButton");
		wait_ms(5000);
		
		String[] windowHandles = this.driver().getWindowHandles().toArray(new String[0]);
		this.driver().switchTo().window(windowHandles[1]);
		currentReportURL = this.driver().getCurrentUrl();
		wait_ms(1000);
		
		this.driver().switchTo().window(windowHandles[0]);
		
		return this;
	}
	
	public String getCurrentReportURL() {
		return currentReportURL;
	}
	
	private ReportsPage clickGenerateButton(String reportType) {
		clickElementById("generate-" + reportType);
		return this;
	}
	
	private ReportsPage clickScheduleButton(String reportType) {
		clickElementById("view_automation-" + reportType);
		return this;
	}
	
	public PreviousReportsPage clickViewGeneratedReports(String reportType){		
		clickElementById("view_generated-" + reportType);
		return new PreviousReportsPage(this,false);
	}
	
	public String getMessageBoxHeader(){
		WebElement messageBoxText = driver().findElementByClassName("x-window-header-text");
		return messageBoxText.getText();
	}
	
	public ReportsPage scheduleCreditSummaryReport() {
		ReportsPage wizardPage1 = clickScheduleButton("credit_summary");		
		wizardPage1.clickElementById("nextButton");
		wait_ms(500);
		wizardPage1.clickElementById("nextButton");
		return this;
	}
	
	public ScheduledReportsPage clickViewScheduledReports(String reportType) {
		clickElementById("view_scheduled-" + reportType);
		return new ScheduledReportsPage(this,false);
	}	
}