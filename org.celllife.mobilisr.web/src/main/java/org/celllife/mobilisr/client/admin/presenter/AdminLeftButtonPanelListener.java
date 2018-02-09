package org.celllife.mobilisr.client.admin.presenter;

import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.NumberInfo;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;

public class AdminLeftButtonPanelListener implements Listener<ButtonEvent> {

	private final AdminEventBus eventBus;

	public AdminLeftButtonPanelListener(AdminEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void handleEvent(ButtonEvent be) {

		MyGXTButton b = (MyGXTButton) be.getButton();
		String id = b.getId();

		if(id.equals("dashboardButton")){
			eventBus.showAdminDashboard();
		}else if(id.equals("organizationButton")){
			eventBus.showOrgList(null);
		}else if( id.equals("userButton")){
			eventBus.showUserList(null);
		}else if( id.equals("settingsButton")){
			eventBus.showSettingsView();
		}else if( id.equals("roleButton")){
			eventBus.showRoleView();
		}else if( id.equals("justSMSButton")){
			ViewModel<Campaign> viewModel = new ViewModel<Campaign>();
			viewModel.putProperty(MobilisrBasePresenter.ADMIN_VIEW, true);
			eventBus.showJustSMSCampaignList(viewModel);
		}else if( id.equals("campaignButton")){
			ViewModel<Campaign> viewModel = new ViewModel<Campaign>(null);
			viewModel.putProperty(MobilisrBasePresenter.ADMIN_VIEW, true);
			eventBus.showCampaignList(viewModel);
		}else if( id.equals("reportButton")){
			ViewModel<Void> viewModel = new ViewModel<Void>();
			viewModel.putProperty(MobilisrBasePresenter.ADMIN_VIEW, true);
			eventBus.showReportView(viewModel);
		}else if( id.equals("channelButton")){
			eventBus.showChannelListView(new ViewModel<Channel>());
		}else if( id.equals("channelConfigButton")){
			eventBus.showChannelConfigListView(new ViewModel<ChannelConfig>());
		}else if( id.equals("numberInfoButton")){
			eventBus.showNumberInfoList(new ViewModel<NumberInfo>());
		}else if (id.equals("filterButton")) {
			ViewModel<?> viewModel = new ViewModel<Object>();
			viewModel.putProperty(MobilisrBasePresenter.ADMIN_VIEW, true);
			eventBus.showFilterListView(viewModel);
		}else if (id.equals("lostMessagesButton")) {
			eventBus.showLostMessagesView();
		}
	}

}
