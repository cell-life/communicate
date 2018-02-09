package org.celllife.communicate.page;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;

public abstract class EntityListPage extends LoggedInPage {

	private final String pageTitle;
	private final String listIdPrefix;

	public EntityListPage(BasePage previousPage, String pageTitle, String listIdPrefix) {
		super(previousPage);
		this.pageTitle = pageTitle;
		this.listIdPrefix = listIdPrefix;
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

	public String getSuccessMessage() {
		WebElement divSuccess;
		try {
			divSuccess = driver().findElementByClassName("create-success");
			return divSuccess.getText();
		} catch (NotFoundException e) {
			log.trace("Element 'create-success' on " + pageTitle
					+ "Page not found.");
		}
		return "";
	}

	public boolean isInList(Long id){
		try {
			driver().findElementById(listIdPrefix+id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityListPage> T selectItemInList(Long id){
		clickElementById(listIdPrefix+id);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public  <T extends EntityListPage> T clickVoidButton(Long id) {
		try {
			clickElementById("void-"+id);
		} catch (ElementNotVisibleException e) {
			// If the element is not visible, might be in expandable-menu
			return (T) clickExpandMenuItem(id, "delete");
		}
		return (T) this;
	}

	/**
	 * The same as <b>clickVoidButton</b>, except looks for "add" in the expandable-menu
	 * if the element is not visible
	 */
	//TODO: replace this and clickVoid button with
	//  clickActionButton(String elementId, Long id, String fallbackStyleKeyword)
	@SuppressWarnings("unchecked")
	public  <T extends EntityListPage> T clickUnvoidButton(Long id) {
		try {
			clickElementById("unvoid-"+id);
		} catch (ElementNotVisibleException e) {
			// If the element is not visible, might be in expandable-menu
			return (T) clickExpandMenuItem(id, "add");
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityListPage> T showVoided() {
		makeComboSelection("void_filter", "Inactive");
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityListPage> T setVoidFilter(String filterValue) {
		makeComboSelection("void_filter", filterValue);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityListPage> T clickExpandButton(Long id) {
		//xpath://id('justsms-17')/ancestor::tr[1]//td[@class='x-toolbar-right']//button
		WebElement rowEntity = driver().findElementById(listIdPrefix+id);
		WebElement button = rowEntity.findElement(
				By.xpath("ancestor::tr[1]//td[@class='x-toolbar-right']//button"));
		button.click();
		wait_ms(1000);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityListPage> T clickExpandMenuItem(Long id, String styleKeyword) {
		clickExpandButton(id);
		//xpath://div[contains(@class,'x-menu-list-item')]//node()[contains(@style, 'delete')]
		WebElement menuItem = driver().findElement(By.xpath(
				"//div[contains(@class,'x-menu-list-item')]//node()[contains(@style, '" +
				styleKeyword + "')]"));
		menuItem.click();
		wait_ms(1000);
		return (T) this;
	}
	
}
