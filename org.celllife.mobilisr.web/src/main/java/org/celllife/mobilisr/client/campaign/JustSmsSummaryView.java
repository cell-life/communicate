package org.celllife.mobilisr.client.campaign;

import org.celllife.mobilisr.client.app.BasicView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.Campaign;

public interface JustSmsSummaryView extends BasicView {

	void setFormObject(ViewModel<Campaign> vem);

	public MyGXTButton getBtnSchedule();

	public MyGXTButton getBtnEditCampaign();

	public MyGXTButton getBtnCheckCampaign();
	
	public MyGXTButton getSmsSendButton();
	
	public MyGXTButton getBtnCloseCampaignSummary();

	public void showSendSmsTest();
	
	public String getSmsTestNumber();
	
	public Campaign getCampaign();
	
	public double getBalanceAfterCampaign();
	
	public double getTotalCostForCampaign();
	
	public void populateFields();

	public int getNumSMSParts();

	String getCampSchedStatusMsg();
	
}
