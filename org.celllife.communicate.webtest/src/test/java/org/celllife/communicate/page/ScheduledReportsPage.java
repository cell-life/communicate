package org.celllife.communicate.page;

import org.openqa.selenium.NoSuchElementException;

public class ScheduledReportsPage extends EntityListPage {
	
	public ScheduledReportsPage(BasePage previousPage, boolean isAdmin) {
		super(previousPage, "Reports", "report-");
	}
	
	public boolean isInList(String id) {
		try {
			driver().findElementById("report-"+id);
			return true;
		}
		catch (NoSuchElementException e){
			return false;
		}
	}
}