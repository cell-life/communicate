package org.celllife.mobilisr.client.app.presenter;


import org.celllife.mobilisr.client.app.HomeLeftView;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.view.HomeLeftViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.mvp4g.client.annotation.Presenter;

@Presenter( view=HomeLeftViewImpl.class)
public class HomeLeftViewPresenter extends MobilisrBasePresenter<HomeLeftView, MobilisrEventBus>{

	
	@Override
	public void bindView() {
		
		getView().getJustSMSButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				eventBus.showJustSMSView(new ViewModel<Campaign>());				
			}
		});
		
		getView().getCampaignsButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				eventBus.showCampaignList(null);				
			}
		});
		
		getView().getReportsButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				eventBus.showReportView(new ViewModel<Void>());				
			}
		});
		
		getView().getFiltersButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				eventBus.showFilterListView(null);				
			}
		});
	}
	
	public void onShowHomeButtonPanel(){
		
		getEventBus().setRegionLeft(this);
	}
}

