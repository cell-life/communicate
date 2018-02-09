package org.celllife.mobilisr.client.filter;

import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.filter.presenter.FilterCreatePresenter;
import org.celllife.mobilisr.client.filter.presenter.FilterListPresenter;
import org.celllife.mobilisr.client.filter.view.FilterListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.event.EventBus;

@Events(module = FilterModule.class, startView = FilterListViewImpl.class)
public interface FilterEventBus extends EventBus {

	@Event(handlers = FilterListPresenter.class, navigationEvent = true)
	public void showFilterListView(ViewModel<?> vem);

	@Event(handlers = FilterCreatePresenter.class, navigationEvent = true)
	public void showFilterCreateView(ViewModel<MessageFilterViewModel> vem);

	// =============== PARENT EVENTS

	@Event(forwardToParent = true)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);

	@Event(forwardToParent = true)
	public void showMessageLog(ViewModel<? extends MobilisrEntity> vem);
}
