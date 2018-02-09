package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.app.RegionTemplateView;
import org.celllife.mobilisr.client.app.view.RegionTemplateViewImpl;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;

import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = RegionTemplateViewImpl.class)
public class RegionTemplatePresenter extends BasePresenter<RegionTemplateView, MobilisrEventBus> {

	public void onSetRegionHeader(Widget myWidgetInterface) {
		getView().setHeaderWidget(myWidgetInterface);
	}

	public void onSetRegionContent(PresenterStateAware mobilisrBasePresenter) {
		Widget widget = mobilisrBasePresenter.getPresenterView().getViewWidget();
		getView().setContentWidget(widget);
	}

	public void onSetRegionFooter(Widget myWidgetInterface) {
		getView().setFooterWidget(myWidgetInterface);
	}
	
	public void onErrorOnLoad(Throwable reason){
		MessageBoxWithIds.alert("Error loading module", reason.getMessage(), null);
	}
	
	public void onBeforeLoad(){
		BusyIndicator.showBusyIndicator("Loading module");
	}
	
	public void onAfterLoad(){
		BusyIndicator.hideBusyIndicator();
		
	}
	
}
