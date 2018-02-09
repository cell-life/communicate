package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.HomeLeftView;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.ui.Widget;

public class HomeLeftViewImpl extends LayoutContainer implements HomeLeftView{
	
	private VBoxLayoutData buttonLayout;
	private Button justSMSButton;
	private Button campaignButton;
	private Button filterButton;
	private Button reportsButton;

	@Override
	public void createView(){
		buttonLayout = new VBoxLayoutData(new Margins(10, 0, 10,	0));
		justSMSButton = new MyGXTButton("justSMSButton", Messages.INSTANCE.menuJustSms());
		campaignButton = new MyGXTButton("campaignButton", Messages.INSTANCE.menuCampaigns());
		filterButton = new MyGXTButton("filterButton", Messages.INSTANCE.filterListHeader());
		reportsButton = new MyGXTButton("reportsButton", Messages.INSTANCE.reportListHeader());

		setBorders(true);
		setStyleAttribute("background-color", "white");
		
		VBoxLayout westLayout = new VBoxLayout();
		westLayout.setPadding(new Padding(10));
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);

		setLayout(westLayout);

		add(justSMSButton, buttonLayout);
		addIfAllowed(campaignButton, MobilisrPermission.CAMPAIGNS_VIEW);
		addIfAllowed(filterButton,  MobilisrPermission.FILTERS_VIEW);
		addIfAllowed(reportsButton, MobilisrPermission.REPORTS_VIEW);
	}
	
	private void addIfAllowed(Button button, MobilisrPermission permission) {
		if (UserContext.hasPermission(permission)){
			add(button, buttonLayout);
		}
	}
	
	@Override
	public Button getJustSMSButton() {
		return justSMSButton;
	}
	
	@Override
	public Button getCampaignsButton(){
		return campaignButton;
	}
	
	@Override
	public Button getReportsButton() {
		return reportsButton;
	}
	
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public Button getFiltersButton() {
		return filterButton;
	}
}
