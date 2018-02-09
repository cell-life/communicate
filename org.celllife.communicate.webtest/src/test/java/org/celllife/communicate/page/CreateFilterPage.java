package org.celllife.communicate.page;

import org.openqa.selenium.WebElement;

public class CreateFilterPage extends LoggedInPage {
	
	public CreateFilterPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}
	
	@Override
	protected void checkAtPage() {		
		String title = driver().findElementById("titleLabelText").getText();
		if (!title.equals("Filter: Create New Filter")){
			throw new IllegalArgumentException("Unable to verify Create Filter Page");
		}		
	}
	
	public CreateFilterPage fillForm(String organisation, String filterName, String channel, String type) {
		
		log.info("Filling out new filter form.");	
		
		makeComboSelection("orgComboBox-input", organisation);			
		makeComboSelection("channelCombo-input", channel);		
		
		WebElement filterNameElement = driver().findElementById("filterName-input");
		filterNameElement.clear();
		filterNameElement.sendKeys(filterName);
		
		CreateFilterPage msgBoxPage = makeComboSelectionFilter(type);
		
		String messageBoxHeader = msgBoxPage.getMessageBoxHeader();		
		if (messageBoxHeader.contains("Keyword")) {
			//TODO
		}
		else if (messageBoxHeader.contains("Match all")) {
			msgBoxPage.clickElementById("pSubmitButton");
		}
		else if (messageBoxHeader.contains("Regex")) {
			// TODO 
		}
		
		return this;
	}

	public CreateFilterPage makeComboSelectionFilter(String type){
		makeComboSelection("filterTypeCombo-input", type);
		return this;
	}
	
	public String getMessageBoxHeader(){
		try {
			WebElement messageBoxText = driver().findElementByClassName("x-window-header-text");
			return messageBoxText.getText();
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}
	
	public FilterPage clickSubmit() {
		clickElementById("submitButton");
		return new FilterPage(this,true);
	}
	
	
}