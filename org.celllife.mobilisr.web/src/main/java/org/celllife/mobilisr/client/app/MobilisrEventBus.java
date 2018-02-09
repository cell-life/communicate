package org.celllife.mobilisr.client.app;

import org.celllife.mobilisr.client.admin.AdminModule;
import org.celllife.mobilisr.client.app.presenter.ErrorPresenter;
import org.celllife.mobilisr.client.app.presenter.FooterPresenter;
import org.celllife.mobilisr.client.app.presenter.HeaderPresenter;
import org.celllife.mobilisr.client.app.presenter.HomeLeftViewPresenter;
import org.celllife.mobilisr.client.app.presenter.LeftRightPresenter;
import org.celllife.mobilisr.client.app.presenter.RegionTemplatePresenter;
import org.celllife.mobilisr.client.app.presenter.StartEventHandler;
import org.celllife.mobilisr.client.app.view.RegionTemplateViewImpl;
import org.celllife.mobilisr.client.campaign.CampaignModule;
import org.celllife.mobilisr.client.contacts.ContactModule;
import org.celllife.mobilisr.client.filter.FilterModule;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.ReportModule;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrEntity;

import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.Start;
import com.mvp4g.client.annotation.module.AfterLoadChildModule;
import com.mvp4g.client.annotation.module.BeforeLoadChildModule;
import com.mvp4g.client.annotation.module.ChildModule;
import com.mvp4g.client.annotation.module.ChildModules;
import com.mvp4g.client.annotation.module.LoadChildModuleError;
import com.mvp4g.client.event.EventBus;

@Events(startView = RegionTemplateViewImpl.class)
@ChildModules({
		@ChildModule(moduleClass = ContactModule.class, autoDisplay = false),
		@ChildModule(moduleClass = AdminModule.class, autoDisplay = false),
		@ChildModule(moduleClass = FilterModule.class, autoDisplay = false),
		@ChildModule(moduleClass = CampaignModule.class, autoDisplay = false),
		@ChildModule(moduleClass = ReportModule.class, autoDisplay = false) })
public interface MobilisrEventBus extends EventBus {

	// Default Presenters for app load
	@Start
	@Event(handlers = { StartEventHandler.class, HeaderPresenter.class,
			ErrorPresenter.class, FooterPresenter.class })
	public void start();

	@Event(handlers = { StartEventHandler.class }, navigationEvent = false)
	public void showWhatsNew();

	@Event(handlers = { HeaderPresenter.class })
	public void updateOrgBalanceLabel();

	@Event(handlers = RegionTemplatePresenter.class)
	public void setRegionHeader(Widget widget);

	@Event(handlers = RegionTemplatePresenter.class)
	public void setRegionContent(PresenterStateAware mobilisrBasePresenter);

	@Event(handlers = RegionTemplatePresenter.class)
	public void setRegionFooter(Widget widget);

	@Event(handlers = LeftRightPresenter.class)
	public void setRegionLeft(PresenterStateAware mobilisrBasePresenter);

	@Event(handlers = LeftRightPresenter.class)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);

	@Event(handlers = ErrorPresenter.class, navigationEvent = true)
	public void showErrorView(String message);

	@Event(handlers = LeftRightPresenter.class, navigationEvent = true)
	public void showHomeRegion();

	@Event(handlers = HomeLeftViewPresenter.class)
	public void showHomeButtonPanel();

	// ============= CHILD MODULE EVENTS

	@Event(modulesToLoad = AdminModule.class, handlers = LeftRightPresenter.class, navigationEvent = true)
	public void showAdminRegion();

	@Event(modulesToLoad = ContactModule.class, handlers = LeftRightPresenter.class, navigationEvent = true)
	public void showContactsRegion();

	@Event(modulesToLoad = ReportModule.class, navigationEvent = true)
	public void showReportView(ViewModel<Void> viewModel);

	@Event(modulesToLoad = FilterModule.class, navigationEvent = true)
	public void showFilterListView(ViewModel<?> vem);

	@Event(modulesToLoad = CampaignModule.class, navigationEvent = true)
	public void showMessageLog(ViewModel<? extends MobilisrEntity> vem);
	
	@Event(modulesToLoad = CampaignModule.class, navigationEvent = true)
	public void showCampaignList(ViewModel<Campaign> vem);
	
	@Event(modulesToLoad = CampaignModule.class, navigationEvent = true)
	public void showJustSMSView(ViewModel<Campaign> vem);
	
	@Event(modulesToLoad = CampaignModule.class, navigationEvent = true)
	public void showJustSMSCampaignList(ViewModel<Campaign> vem);

	// ============= SYSTEM MODULE EVENTS

	@LoadChildModuleError
	@Event(handlers = RegionTemplatePresenter.class)
	public void errorOnLoad(Throwable reason);

	@BeforeLoadChildModule
	@Event(handlers = RegionTemplatePresenter.class)
	public void beforeLoad();

	@AfterLoadChildModule
	@Event(handlers = RegionTemplatePresenter.class)
	public void afterLoad();
}
