package org.celllife.communicate.page;

import org.openqa.selenium.ElementNotVisibleException;

public class PreviousReportsPage extends EntityListPage {

	public PreviousReportsPage(BasePage previousPage, boolean isAdmin) {
		super(previousPage, "Reports", "report-");
	}
	
	public boolean isInList(String id) {
		try {
			driver().findElementById("report-" + id);
			return true;
		} catch (ElementNotVisibleException e) {
			return false;
		}
	}
}