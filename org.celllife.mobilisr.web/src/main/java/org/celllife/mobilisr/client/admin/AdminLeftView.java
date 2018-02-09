package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.app.BasicView;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface AdminLeftView extends BasicView{

	public Button getOrgButton();

	public Button getUserButton();

	public Button getSettingsButton();

	public Button getRoleButton();

	public Button getJustSMSButton();

	public Component getDashboardButton();

	public Button getCampaignButton();

	public Button getReportButton();

	public Button getFilterButton();

	public Component getChannelButton();

	public Button getLostMessagesButton();

	Component getChannelConfigButton();

	Component getNumberInfoButton();
}
