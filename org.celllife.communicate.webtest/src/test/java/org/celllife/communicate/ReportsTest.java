package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.PreviousReportsPage;
import org.celllife.communicate.page.ReportsPage;
import org.celllife.communicate.page.ScheduledReportsPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.junit.Test;

public class ReportsTest extends AbstractBaseTest {
		
	public ReportsPage goReportsPage(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goReportsPage();
	}
	
	@Test
	public void testGenerateReport(){			
		ReportsPage reportsPage = goReportsPage();
		reportsPage.generateCreditSummaryReport();
		
		String url = reportsPage.getCurrentReportURL();
		String index = url.substring(url.lastIndexOf("/")+1, url.length());
				
		PreviousReportsPage previousReports = reportsPage.clickViewGeneratedReports("credit_summary");
		Assert.assertTrue(previousReports.isInList(index));
	}
	
	@Test
	public void testScheduleReport(){		
		ReportsPage reportsPage = goReportsPage();
		reportsPage.scheduleCreditSummaryReport();
		Assert.assertEquals("Credit summary scheduled",reportsPage.getSuccessMessage()); 		
		
		String messageid = reportsPage.getSuccessId();
		String index = messageid.substring(messageid.lastIndexOf("-")+1, messageid.length());
		
		ScheduledReportsPage scheduledReportsPage = reportsPage.clickViewScheduledReports("credit_summary");
		Assert.assertTrue(scheduledReportsPage.isInList(index));
	}	
	
}