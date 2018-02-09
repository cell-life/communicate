package org.celllife.mobilisr.client.admin.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.AdminLeftView;
import org.celllife.mobilisr.client.admin.view.AdminLeftViewImpl;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.event.Events;
import com.mvp4g.client.annotation.Presenter;

@Presenter( view=AdminLeftViewImpl.class)
public class AdminLeftViewPresenter extends MobilisrBasePresenter<AdminLeftView, AdminEventBus>{

	@Override
	public void bindView() {

		AdminLeftButtonPanelListener adminLeftButtonPanelListener = new AdminLeftButtonPanelListener(getEventBus());

		getView().getDashboardButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getOrgButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getUserButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getRoleButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getSettingsButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getJustSMSButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getCampaignButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getReportButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getChannelButton().addListener(Events.Select, adminLeftButtonPanelListener);
		
		getView().getChannelConfigButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getNumberInfoButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getFilterButton().addListener(Events.Select, adminLeftButtonPanelListener);

		getView().getLostMessagesButton().addListener(Events.Select, adminLeftButtonPanelListener);
	}

	public void onShowAdminButtonPanel(){
		if (!UserContext.hasPermission(MobilisrPermission.VIEW_ADMIN_CONSOLE)){
			getEventBus().showErrorView(Messages.INSTANCE
									.securityAccessDenied(MobilisrPermission.VIEW_ADMIN_CONSOLE
											.name()));
			return;
		}
		getEventBus().setRegionLeft(this);
	}
}
