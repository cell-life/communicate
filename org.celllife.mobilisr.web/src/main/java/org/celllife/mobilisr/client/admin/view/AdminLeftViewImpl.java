package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminLeftView;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.ui.Widget;

public class AdminLeftViewImpl extends LayoutContainer implements AdminLeftView{

	private VBoxLayout westLayout;
	private VBoxLayoutData buttonLayout;
	private MyGXTButton dashboardButton;
	private MyGXTButton organizationButton;
	private MyGXTButton userButton;
	private MyGXTButton roleButton;
	private MyGXTButton settingsButton;
	private MyGXTButton justSMSButton;
	private MyGXTButton campaignButton;
	private MyGXTButton reportButton;
	private MyGXTButton channelButton;
	private MyGXTButton channelConfigButton;
	private MyGXTButton numberInfoButton;
	private MyGXTButton filterButton;
	private MyGXTButton lostMessagesButton;

	@Override
	public void createView(){
		buttonLayout = new VBoxLayoutData(new Margins(10, 0, 10, 0));

		setBorders(true);
		setStyleAttribute("background-color", "white");
		
		westLayout = new VBoxLayout();
		westLayout.setPadding(new Padding(10));
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		setLayout(westLayout);
		
		boolean sectionExists = false;
		dashboardButton = new MyGXTButton("dashboardButton", Messages.INSTANCE.menuDashboard());
		justSMSButton = new MyGXTButton("justSMSButton", Messages.INSTANCE.menuJustSms());
		campaignButton = new MyGXTButton("campaignButton", Messages.INSTANCE.menuCampaigns());
		sectionExists |= addIfAllowed(dashboardButton, MobilisrPermission.VIEW_ADMIN_CONSOLE);
		sectionExists |= addIfAllowed(justSMSButton, MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE);
		sectionExists |= addIfAllowed(campaignButton, MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE);

		if (sectionExists)
			add(new Html("<hr class=\"hrblue\"/>"));
		
		sectionExists = false;
		filterButton = new MyGXTButton("filterButton", Messages.INSTANCE.filterListHeaderAdmin());
		lostMessagesButton = new MyGXTButton("lostMessagesButton", Messages.INSTANCE.lostMessagesHeader());
		sectionExists |= addIfAllowed(filterButton, MobilisrPermission.FILTERS_ADMIN_VIEW);
		sectionExists |= addIfAllowed(lostMessagesButton, MobilisrPermission.MANAGE_LOST_MESSAGES);
		
		if (sectionExists)
			add(new Html("<hr class=\"hrblue\"/>"));

		sectionExists = false;
		organizationButton = new MyGXTButton("organizationButton", Messages.INSTANCE.orgListHeader());
		userButton = new MyGXTButton("userButton", Messages.INSTANCE.userListHeader());
		roleButton = new MyGXTButton("roleButton", Messages.INSTANCE.roleListHeader());
		sectionExists |= addIfAllowed(organizationButton, MobilisrPermission.ORGANISATIONS_MANAGE);
		sectionExists |= addIfAllowed(userButton, MobilisrPermission.MANAGE_USERS);
		sectionExists |= addIfAllowed(roleButton, MobilisrPermission.MANAGE_ROLES);
		
		if (sectionExists)
			add(new Html("<hr class=\"hrblue\"/>"));

		channelButton = new MyGXTButton("channelButton", Messages.INSTANCE.channelListHeader());
		channelConfigButton = new MyGXTButton("channelConfigButton", Messages.INSTANCE.channelConfigListHeader());
		numberInfoButton = new MyGXTButton("numberInfoButton", Messages.INSTANCE.numberInfoListHeader());
		reportButton = new MyGXTButton("reportButton", Messages.INSTANCE.reportListHeader());
		settingsButton = new MyGXTButton("settingsButton", Messages.INSTANCE.menuSettings());
		addIfAllowed(channelButton, MobilisrPermission.CHANNELS_VIEW);
		addIfAllowed(channelConfigButton, MobilisrPermission.CHANNEL_CONFIG_MANAGE);
		addIfAllowed(numberInfoButton, MobilisrPermission.NUMBER_INFO_VIEW);
		addIfAllowed(reportButton, MobilisrPermission.REPORTS_ADMIN_VIEW);
		addIfAllowed(settingsButton, MobilisrPermission.MANAGE_SETTINGS);
		
	}

	private boolean addIfAllowed(MyGXTButton button, MobilisrPermission permission) {
		if (UserContext.hasPermission(permission)){
			return add(button, buttonLayout);
		}
		return false;
	}

	@Override
	public Component getDashboardButton() {
		return dashboardButton;
	}

	public Button getOrgButton() {
		return organizationButton;
	}

	public Button getUserButton() {
		return userButton;
	}

	public Button getSettingsButton() {
		return settingsButton;
	}

	public Button getRoleButton() {
		return roleButton;
	}

	@Override
	public Button getJustSMSButton() {
		return justSMSButton;
	}

	@Override
	public Button getCampaignButton(){
		return campaignButton;
	}

	@Override
	public Button getReportButton() {
		return reportButton;
	}

	@Override
	public Button getFilterButton() {
		return filterButton;
	}

	@Override
	public Component getChannelButton() {
		return channelButton;
	}
	
	@Override
	public Component getChannelConfigButton() {
		return channelConfigButton;
	}

	@Override
	public Component getNumberInfoButton() {
		return numberInfoButton;
	}

	@Override
	public Button getLostMessagesButton() {
		return lostMessagesButton;
	}

	public Widget getViewWidget() {
		return this;
	}
}
