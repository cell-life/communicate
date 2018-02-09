package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.admin.presenter.AdminLeftViewPresenter;
import org.celllife.mobilisr.client.admin.presenter.ChannelConfigListPresenter;
import org.celllife.mobilisr.client.admin.presenter.ChannelCreatePresenter;
import org.celllife.mobilisr.client.admin.presenter.ChannelListPresenter;
import org.celllife.mobilisr.client.admin.presenter.DashboardPresenter;
import org.celllife.mobilisr.client.admin.presenter.LostMessagesPresenter;
import org.celllife.mobilisr.client.admin.presenter.NumberInfoListPresenter;
import org.celllife.mobilisr.client.admin.presenter.OrganizationNotificationPresenter;
import org.celllife.mobilisr.client.admin.presenter.SettingsPresenter;
import org.celllife.mobilisr.client.admin.view.DashoardViewImpl;
import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.org.presenter.AdminOrgCreatePresenter;
import org.celllife.mobilisr.client.org.presenter.AdminOrgListPresenter;
import org.celllife.mobilisr.client.role.presenter.AdminRoleUIPresenter;
import org.celllife.mobilisr.client.user.presenter.AdminUserCreatePresenter;
import org.celllife.mobilisr.client.user.presenter.AdminUserListPresenter;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.event.EventBus;

@Events(module = AdminModule.class, startView = DashoardViewImpl.class)
public interface AdminEventBus extends EventBus {
	
	@Event(handlers = AdminRegionHandler.class, navigationEvent = true)
	public void showAdminRegion();

	@Event(handlers = AdminLeftViewPresenter.class)
	public void showAdminButtonPanel();
	
	// =============== ADMIN VIEWS

	@Event(handlers = DashboardPresenter.class, navigationEvent = true)
	public void showAdminDashboard();

	@Event(handlers = AdminUserListPresenter.class, navigationEvent = true)
	public void showUserList(ViewModel<User> vem);

	@Event(handlers = AdminUserCreatePresenter.class, navigationEvent = true)
	public void showUserCreate(ViewModel<User> vem);

	@Event(handlers = AdminOrgListPresenter.class, navigationEvent = true)
	public void showOrgList(ViewModel<Organization> vem);

	@Event(handlers = AdminOrgCreatePresenter.class, navigationEvent = true)
	public void showOrgCreate(ViewModel<Organization> vem);

	@Event(handlers = AdminRoleUIPresenter.class, navigationEvent = true)
	public void showRoleView();

	@Event(handlers = SettingsPresenter.class, navigationEvent = true)
	public void showSettingsView();

	@Event(handlers = ChannelListPresenter.class, navigationEvent = true)
	public void showChannelListView(ViewModel<Channel> vem);

	@Event(handlers = ChannelCreatePresenter.class, navigationEvent = true)
	public void showChannelCreateView(ViewModel<Channel> vem);

	@Event(handlers = ChannelConfigListPresenter.class, navigationEvent = true)
	public void showChannelConfigListView(ViewModel<ChannelConfig> vem);

	@Event(handlers = LostMessagesPresenter.class, navigationEvent = true)
	public void showLostMessagesView();

	@Event(handlers = OrganizationNotificationPresenter.class, navigationEvent = true)
	public void showOrganizationNotificationView();
	
	@Event(handlers = NumberInfoListPresenter.class, navigationEvent = true)
	public void showNumberInfoList(ViewModel<NumberInfo> viewModel);
	
	// =============== PARENT EVENTS

	@Event(forwardToParent = true)
	public void showErrorView(String message);

	@Event(forwardToParent = true)
	public void setRegionLeft(PresenterStateAware mobilisrBasePresenter);

	@Event(forwardToParent = true)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);

	@Event(forwardToParent = true)
	public void updateOrgBalanceLabel();

	@Event(forwardToParent = true)
	public void showMessageLog(ViewModel<? extends MobilisrEntity> vem);
	
	@Event(forwardToParent = true)
	public void showJustSMSCampaignList(ViewModel<Campaign> vem);
	
	@Event(forwardToParent = true)
	public void showCampaignList(ViewModel<Campaign> vem);
	
	@Event(forwardToParent = true)
	public void showReportView(ViewModel<Void> viewModel);
	
	@Event(forwardToParent = true)
	public void showFilterListView(ViewModel<?> vem);

}
