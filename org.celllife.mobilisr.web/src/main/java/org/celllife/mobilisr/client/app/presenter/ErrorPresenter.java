package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.app.ErrorView;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.view.ErrorViewImpl;

import com.mvp4g.client.annotation.Presenter;

@Presenter( view=ErrorViewImpl.class)
public class ErrorPresenter extends MobilisrBasePresenter<ErrorView, MobilisrEventBus> {

	public void onShowErrorView(String message){
		getEventBus().setRegionContent(this);
		getView().setMessage(message);
	}
	
	public void onStart() {
		getEventBus().setRegionContent( this );
	}
}
