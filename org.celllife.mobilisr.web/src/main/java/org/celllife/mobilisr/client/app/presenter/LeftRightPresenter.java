package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.LeftRightView;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.app.view.LeftRightViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = LeftRightViewImpl.class)
public class LeftRightPresenter extends MobilisrBasePresenter<LeftRightView, MobilisrEventBus> {

	private static final String CAMPAIGNS = "campaigns";
	private static final String CONTACTS = "contacts";
	private static final String ADMIN = "admin";
	
	private String currentRegion;

	public void onShowAdminRegion() {
		if (UserContext.hasPermission(MobilisrPermission.VIEW_ADMIN_CONSOLE)){
			System.out.println("changing region from:" + currentRegion + " to admin");
			currentRegion = ADMIN;
			getEventBus().setRegionContent(this);
		} else {
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.VIEW_ADMIN_CONSOLE
							.name()));
		}
	}

	public void onShowContactsRegion() {
		System.out.println("changing region from:" + currentRegion + " to contacts");
		currentRegion = CONTACTS;
		getEventBus().setRegionContent(this);
	}
	
	public void onShowHomeRegion() {
		System.out.println("changing region from:" + currentRegion + " to campaigns");
		currentRegion = CAMPAIGNS;
		getEventBus().setRegionContent(this);
		getEventBus().showHomeButtonPanel();
		getEventBus().showJustSMSView(new ViewModel<Campaign>());
	}
	
	public void onSetRegionLeft(PresenterStateAware mobilisrBasePresenter) {
		Widget widget = mobilisrBasePresenter.getPresenterView().getViewWidget();
		getView().setLeftLayoutContainer(widget);
	}

	public void onSetRegionRight(PresenterStateAware mobilisrBasePresenter) {
		Widget widget = mobilisrBasePresenter.getPresenterView().getViewWidget();
		getView().setRightLayoutContainer(widget);
	}
}
