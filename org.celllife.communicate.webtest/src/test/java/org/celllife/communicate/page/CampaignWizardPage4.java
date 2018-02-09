package org.celllife.communicate.page;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.celllife.communicate.util.CommonHelper;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverBackedSelenium;

public class CampaignWizardPage4 extends CampaignWizardPage {

	public CampaignWizardPage4(BasePage previousPage) {
		super(previousPage, 4);
	}

	public CampaignWizardPage4 editMessage(Long id, String newMessage, Integer dayToSend, String time){
		clickElementById("edit-" + id);
		fillMessageDialogForm(newMessage, dayToSend, time);
		return this;
	}

	/**
	 * @param newMessage
	 * @param dayToSend
	 * @param time
	 */
	private void fillMessageDialogForm(String newMessage, Integer dayToSend,
			String time) {
		if (newMessage != null){
			driver().findElementById("smsBoxText-input").sendKeys(newMessage);
		}
		if (dayToSend != null){
			driver().findElementById("dayToSend-input").sendKeys(Keys.BACK_SPACE, dayToSend.toString());
		}
		if (newMessage != null){
			makeComboSelection("timeToSend-input", time);
		}

		clickElementById("saveButton");
	}

	public int getCampaignCost(){
		String text = driver().findElementById("campaignCost").getText();
		Pattern p = Pattern.compile("([0-9]+)");
		Matcher m = p.matcher(text);

		if (m.find()) {
			String costString = m.group(1);
			return Integer.parseInt(costString);
		}

		return -1;
	}

	public CampaignWizardPage3 goBack(){
		clickElementById("backButton");
		return new CampaignWizardPage3(this);
	}

	public CampaignWizardPage4 save(){
		clickElementById("saveButton");
		return this;
	}

	public CampaignPage finish(){
		clickElementById("nextButton");
		return new CampaignPage(this, true);
	}

	public CampaignWizardPage4 addMessage(CampaignMessage message) {
		clickElementByName("addNewMessage");
		String time = new SimpleDateFormat("HH:mm").format(message.getMsgTime());
		fillMessageDialogForm(message.getMessage(), message.getMsgDay(), time);
		return this;
	}

	/**
	 * @param fileUrl
	 *            the URL of the file to upload. This must be accessible to from
	 *            the machine running the tests.
	 * @return
	 */
	public CampaignWizardPage4 importMessages(String fileUrl) {
		clickElementById("csvImport");

		String baseUrl = CommonHelper.getTestURL();
		WebDriverBackedSelenium selenium = new WebDriverBackedSelenium(driver(), baseUrl);
		selenium.attachFile("name=csvFile", fileUrl);

		wait_ms(1000);
		clickElementById("importButton");
		wait_ms(2000);
		return this;
	}

	public CampaignWizardPage4 deleteCampaignMessage(CampaignMessage campaignMessage) {
		try{
			clickElementById("delete-"+ campaignMessage.getId());
		}catch(org.openqa.selenium.ElementNotVisibleException e ){
			clickExpandMenuItemByText("campaignMessage-" + campaignMessage.getId(),"Delete Message");
		}
		return this;
	}

	public boolean isInList(Long id){
		try {
			driver().findElementById("campaignMessage-"+id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}



}
