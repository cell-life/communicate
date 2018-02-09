package org.celllife.communicate.page;

public class FilterInboxPage extends LoggedInPage {
	
	public FilterInboxPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}
	
	@Override
	protected void checkAtPage() {		
		String title = driver().findElementById("headerTitle").getText();
		if (!title.contains("Message Log for Filter: ")){
			throw new IllegalArgumentException("Unable to verify Create Filter Page");
		}		
	}
	
	public boolean isFilterInboxPage() {
		String title = driver().findElementById("headerTitle").getText();
		if (!title.contains("Message Log for Filter: ")){
			throw new IllegalArgumentException("Unable to verify Create Filter Page");
		}
		else {
			return true;
		}
	}

}