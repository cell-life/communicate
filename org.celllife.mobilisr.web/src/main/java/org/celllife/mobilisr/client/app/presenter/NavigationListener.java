package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.presenter.BasePresenter;

public class NavigationListener implements Listener<ButtonEvent> {

	private BasePresenter<?, MobilisrEventBus> basePresenter;

	public NavigationListener(BasePresenter<?, MobilisrEventBus> basePresenter) {
		this.basePresenter = basePresenter;
	}

	public void handleEvent(ButtonEvent be) {

		MyGXTButton b = (MyGXTButton) be.getButton();
		String id = b.getId();
		
		MobilisrEventBus eventBus = (MobilisrEventBus) basePresenter
		.getEventBus();

		if (id.equals("programsButton")) {

			eventBus.showHomeRegion();

		} else if (id.equals("contactsButton")) {
			
			eventBus.showContactsRegion();
			
		} else if (id.equals("adminButton")) {

			eventBus.showAdminRegion();

		} else if (id.equals("helpButton")) {
			
			Window.alert("Clicked Help button");
			
		} else if (id.equals("logoutButton")) {
			StartEventHandler.removeWindowCloseHandler();
			Window.Location.replace(GWT.getModuleBaseURL()+"j_spring_security_logout"); 
		
		}
	}

}
