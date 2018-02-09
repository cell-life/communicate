package org.celllife.communicate.page;

import junit.framework.Assert;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;

public class LostMessagesPage extends LoggedInPage {
	private final String pageTitle = "Lost Messages";

	public LostMessagesPage(BasePage previousPage) {
		super(previousPage);
		checkAtPage();
	}

	@Override
	protected void checkAtPage() {
		try {
			WebElement headerTitle = driver().findElementById("headerTitle");
			if (headerTitle == null ||
					headerTitle.getText() == null ||
					!headerTitle.getText().contains(pageTitle)){
				throw new IllegalArgumentException("Unable to verify "+pageTitle+" page");
			}
		}
		catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to verify "+pageTitle+" page");
		}
	}

	public void selectAllMessages() {
		driver().findElementByXPath("//.[@title='Select/Deselect All']").click();
	}

	public void clickDelete() {
		clickElementById("deleteBtn");
	}

	public void deleteAndConfirm(int iNumMessages) {
		clickDelete();
		String text = getMessageBoxText();
		Assert.assertTrue(text.contains("Are you sure you want to delete " + iNumMessages) );
		clickMessageBoxYes();
	}

	public void clickReprocess() {
		clickElementById("reprocessBtn");
	}

	public String getSuccessMessageText() {
		return driver().findElementById("create-success").getText();
	}

	public String getPagingText() {
		return driver().findElementByClassName("my-paging-display").getText();
	}

	public void setSearchText(String searchTerm) {
		driver().findElementById("searchEntityList-input").sendKeys(searchTerm);
		wait_ms(3500);
	}

	public boolean isMessageOnPage(Long id) {
		try {
			driver().findElementById("message-" + id);
		} catch (ElementNotVisibleException e) {
			return false;
		} catch (NotFoundException e) {
			return false;
		}
		return true;
	}

}
