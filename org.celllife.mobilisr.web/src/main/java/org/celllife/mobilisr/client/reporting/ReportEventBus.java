package org.celllife.mobilisr.client.reporting;

import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.view.ReportListViewImpl;
import org.celllife.pconfig.model.Pconfig;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.event.EventBus;

@Events(module = ReportModule.class, startView = ReportListViewImpl.class)
public interface ReportEventBus extends EventBus {

	@Event(handlers = ReportPresenter.class, navigationEvent = true)
	public void showReportView(ViewModel<Void> viewModel);

	@Event(handlers = GeneratedReportPresenter.class, navigationEvent = true)
	public void showGeneratedReportView(ViewModel<Pconfig> viewModel);

	@Event(handlers = ScheduledReportPresenter.class, navigationEvent = true)
	public void showScheduledReportsView(ViewModel<Pconfig> viewModel);
	
	// =============== PARENT EVENTS

	@Event(forwardToParent = true)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);
	
}
