package org.celllife.communicate.page;

import org.openqa.selenium.ElementNotVisibleException;

import junit.framework.Assert;

public class FilterPage extends EntityListPage {

	public FilterPage(BasePage previousPage, boolean isAdmin) {		
		super(previousPage, isAdmin ? "All Filters" : "My Filters", "messagefilter-");		
	}	
	
	public CreateFilterPage createNewFilter() {
		clickElementById("newFilterButton");
		return new CreateFilterPage(this);
	}
	
	public FilterPage deactivateFilter(Long id)
	{
		try {
			clickElementById("deactivate-" + id);
		} catch (ElementNotVisibleException e) {
			clickExpandMenuItem(id, "deactivate"); // If the element is not visible, might be in expandable-menu
		}
		
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("MessageBox Null", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Are you sure"));
		clickMessageBoxYes();		
		
		return this;
	}	
	
	public FilterPage activateFilter(Long id)
	{
		try {
			clickElementById("deactivate-" + id);
		} catch (ElementNotVisibleException e) {
			// If the element is not visible, might be in expandable-menu
			clickExpandMenuItem(id, "deactivate");
		}
		
		String messageBoxText = getMessageBoxText();
		Assert.assertNotNull("MessageBox Null", messageBoxText);
		Assert.assertTrue(messageBoxText.contains("Are you sure"));
		clickMessageBoxYes();		
		
		return this;
	}	
	
	public FilterInboxPage viewFilters(Long id)
	{
		try {
			clickElementById("viewInbox-" + id);
		} catch (ElementNotVisibleException e) {
			// If the element is not visible, might be in expandable-menu
			clickExpandMenuItem(id, "viewInbox-");
		}
		
		return new FilterInboxPage(this);
	}
	
}