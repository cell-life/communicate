package org.celllife.communicate.page;

import java.util.List;

import org.celllife.communicate.util.CommonHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePage {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private static boolean runWithWaits = true;

	private RemoteWebDriver driver;

	private BasePage previousPage;

	public BasePage(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public BasePage(BasePage previousPage) {
		this.previousPage = previousPage;
		this.driver = previousPage.driver;
	}


	public RemoteWebDriver driver() {
		return driver;
	}

	protected void wait_ms(int ms) {
		if (runWithWaits) {
			CommonHelper.wait_ms(ms);
		}
	}

	public static void runWithWaits() {
		BasePage.runWithWaits = true;
	}

	public BasePage getPreviousPage() {
		return previousPage;
	}

	protected abstract void checkAtPage();

	public String getMessageBoxText(){
		try {
			WebElement messageBoxText = driver.findElementByClassName("ext-mb-text");
			return messageBoxText.getText();
		} catch (Exception e) {
			return null;
		}
	}

	public BasePage clickMessageBoxOk(){
		clickElementById("msg-box-button-ok");
		return this;
	}

	public BasePage clickMessageBoxNo(){
		clickElementById("msg-box-button-no");
		return this;
	}

	public BasePage clickMessageBoxYes(){
		clickElementById("msg-box-button-yes");
		return this;
	}

	public void makeComboSelection(String comboName, String selection) {
		WebElement combo;
		try {
			combo = driver.findElementByName(comboName);
		} catch (Exception e) {
			log.trace("Failed to find combo by name: {}. Attempting to find by Id", comboName);
			combo = driver.findElementById(comboName);
		}
		combo.sendKeys(Keys.chord(Keys.CONTROL, "a")); // Select all
		combo.sendKeys(Keys.BACK_SPACE, selection);
		waitForElementByClassName("x-combo-selected", 5000);
		combo.sendKeys(Keys.RETURN);
		wait_ms(1000);
	}

	protected void clickElementById(String id) {
		clickElementById(id, 1000);
	}

	protected void clickElementById(String id, int delay_ms) {
		WebElement element = driver.findElementById(id);
		element.click();
		wait_ms(delay_ms);
	}

	protected void clickElementByClass(String id) {
		clickElementByClass(id, 1000);
	}

	protected void clickElementByClass(String id, int delay_ms) {
		WebElement element = driver.findElementByClassName(id);
		element.click();
		wait_ms(delay_ms);
	}

	protected void clickElementByName(String name) {
		clickElementByName(name, 1000);
	}

	protected void clickElementByName(String name, int delay_ms) {
		WebElement element = driver.findElementByName(name);
		element.click();
		wait_ms(delay_ms);
	}

	public void clickExpandButton(String id) {
		//xpath://id('justsms-17')/ancestor::tr[1]//td[@class='x-toolbar-right']//button
		WebElement rowEntity = driver().findElementById(id);
		WebElement button = rowEntity.findElement(
				By.xpath("ancestor::tr[1]//td[@class='x-toolbar-right']//button"));
		button.click();
		wait_ms(1000);
	}

	/**
	 * Since moving to resource bundles, this no longer works.
	 */
	@Deprecated
	public void clickExpandMenuItem(String id, String styleKeyword) {
		clickExpandButton(id);
		//xpath://div[contains(@class,'x-menu-list-item')]//node()[contains(@style, 'delete')]
		WebElement menuItem = driver().findElement(By.xpath(
				"//div[contains(@class,'x-menu-list-item')]//node()[contains(@style, '" +
				styleKeyword + "')]"));
		menuItem.click();
		wait_ms(1000);
	}

	public void clickExpandMenuItemByText(String id, String itemText) {
		clickExpandButton(id);
		List<WebElement> menuList = driver().findElementsByClassName("x-menu-list-item");
		for (WebElement webElement : menuList) {
			String elementName = webElement.getText();
			if (elementName.equals(itemText)){
				webElement.click();
			}
		}
	}

	public boolean isButtonEnabled(String buttonId) {
		WebElement button = driver.findElementById(buttonId);
		String clazz = button.getAttribute("class");
		return !clazz.contains("x-item-disabled");
	}

	public String getButtonText(String buttonId){
		WebElement buttonTable = driver.findElementById(buttonId);
		WebElement button = buttonTable.findElement(By.xpath("descendant::button"));
		return button.getText();
	}

	/**
	 * Since moving to resource bundles, this no longer works.
	 */
	@Deprecated
	public String getButtonImage(String buttonId){
		WebElement buttonTable = driver.findElementById(buttonId);
		WebElement button = buttonTable.findElement(By.xpath("descendant::img"));
		String imageCss = button.getCssValue("background-image");

		// get image name out of URL: url("http://localhost:8181/communicate/images/arrow-refresh.png");
		return imageCss.substring(imageCss.lastIndexOf("/")+1, imageCss.lastIndexOf(")")-1);
	}

	protected void addItemFromDualList(String itemName) {
		List<WebElement> fromList = driver().findElementsByClassName("x-combo-list-item");
		for (WebElement webElement : fromList) {
			String elementName = webElement.getText();
			if (elementName.equals(itemName)){
				webElement.click();
				driver().findElementByClassName("arrow-right").click();
			}
		}
	}

	protected void removeItemFromDualList(String itemName) {
		List<WebElement> fromList = driver().findElementsByClassName("x-combo-list-item");
		for (WebElement webElement : fromList) {
			String elementName = webElement.getText();
			if (elementName.equals(itemName)){
				webElement.click();
				driver().findElementByClassName("arrow-left").click();
			}
		}
	}

	protected void addAllFromDualList() {
		driver().findElementByClassName("arrow-double-right").click();
	}

	protected void removeAllFromDualList() {
		driver().findElementByClassName("arrow-double-left").click();
	}

	public void waitForElementById(String id, int timeOut_ms) {
		log.info("Waiting for element id=" + id + "(timeout=" + timeOut_ms + " ms) ..");
		waitForElement(By.id(id), timeOut_ms);
	}

	public void waitForElementByClassName(String className, int timeOut_ms) {
		log.info("Waiting for element className=" + className
				+ "(timeout=" + timeOut_ms + " ms) ..");
		waitForElement(By.className(className), timeOut_ms);
	}

	public void waitForElement(By by, int timeOut_ms) {
		int time_ms = 0;
		while (time_ms < timeOut_ms) {
			try {
				System.out.print(".");
				CommonHelper.wait_ms(200);
				time_ms += 200;
				driver().findElement(by);
				// If findElement doesn't throw an exception, then we can stop.
				break;
			}
			catch (NoSuchElementException e) {
			}
			catch (ElementNotVisibleException e) {
			}
		}
		System.out.print("\n");
		if (time_ms >= timeOut_ms)
			throw new TimeoutException();
	}

	/**
	 * Returns true if id present (even if not visible).
	 */
	public boolean idPresent(String id) {
		try {
			driver().findElementById(id);
		} catch (NotFoundException e) {
			return false;
		} catch (ElementNotVisibleException e) {
			return true;
		}
		return true;
	}

	/**
	 * Returns true if id visible.
	 */
	public boolean idVisible(String id) {
		try {
			driver().findElementById(id);
		} catch (NotFoundException e) {
			return false;
		} catch (ElementNotVisibleException e) {
			return false;
		}
		return true;
	}
	
	public void clickMenuItem(String text) {
		//xpath://div[contains(@class,'x-menu-list-item')]//node()[contains(@style, 'delete')]
		WebElement menuItem = driver().findElement(By.xpath(
				"//div[contains(@class,'x-menu-list-item')]//node()[contains(., '" +
				text + "')]"));
		menuItem.click();
		wait_ms(1000);
	}
}
