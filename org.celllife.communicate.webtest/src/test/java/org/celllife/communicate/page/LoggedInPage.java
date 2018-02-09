package org.celllife.communicate.page;

import org.junit.Assert;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;

public class LoggedInPage extends BasePage {

	public LoggedInPage(BasePage previousPage) {
		super(previousPage);
	}

	protected void checkAtPage() {
		String label = getWelcomeLabel();
		Assert.assertTrue(label.contains("Welcome"));
		driver().findElementById("contactsButton");
		driver().findElementById("profileButton");
		driver().findElementById("logoutButton");
	}
	
	public LoginPage logout() {
		log.info("Logging out");
		
		//  Even though GWT has assigned id="logoutButton" to a table containing a button, it seems
		// that WebDriver does the "right thing".
		WebElement btnLogout = driver().findElementById("logoutButton");
		btnLogout.click();
		wait_ms(500);
		return new LoginPage(driver());
	}
	
	public AdminPage goAdminPage() {
		log.info("Navigating to Admin page ..");
		
		WebElement adminButton = driver().findElementById("adminButton");
		adminButton.click();
		wait_ms(1000);
		return new AdminPage(this);
	}
	
	public ContactsPage goContactsPage() {
		log.info("Navigating to Contacts page ..");
		
		WebElement contactsButton = driver().findElementById("contactsButton");
		contactsButton.click();
		wait_ms(1000);
		return new ContactsPage(this);
	}
	
	public HomePage goMyCampaignPage() {
		log.info("Navigating to My Campaigns page ..");
		
		WebElement campaignsButton = driver().findElementById("programsButton");
		campaignsButton.click();
		wait_ms(1000);
		return new HomePage(this);
	}
	
	public ReportsPage goReportsPage() {
		log.info("Navigating to Reports page ..");
		
		WebElement reportsButton = driver().findElementById("reportsButton");
		reportsButton.click();
		wait_ms(1000);
		return new ReportsPage(this);
	}
	
	public MyProfilePage goMyProfilePage() {
		log.info("Navigating to My Profile page ..");
		
		WebElement profileButton = driver().findElementById("profileButton");
		profileButton.click();
		wait_ms(1000);
		return new MyProfilePage(this);
	}
	
	public String getWelcomeLabel() {
		WebElement label = driver().findElementById("welcomeLabel");
		return label.getText();
	}
	
	public String getSuccessMessage() {
		WebElement divSuccess;
		try {
			divSuccess = driver().findElementByClassName("create-success");
			return divSuccess.getText();
		} catch (NotFoundException e) {
			log.trace("Element 'create-success' on " + getWelcomeLabel()
					+ "Page not found.");
		}
		return "";
	}
	
	public String getSuccessId() {
		WebElement divSuccess;
		try {
			divSuccess = driver().findElementByClassName("create-success");
			return divSuccess.getAttribute("id");
		} catch (NotFoundException e) {
			log.trace("Element 'create-success' on " + getWelcomeLabel()
					+ "Page not found.");
		}
		return "";
	}
	
}
