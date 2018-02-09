package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class AdminRegionHandler extends BaseEventHandler<AdminEventBus> {

	public void onShowAdminRegion() {
		if (UserContext.hasPermission(MobilisrPermission.VIEW_ADMIN_CONSOLE)){
			getEventBus().showAdminButtonPanel();
			getEventBus().showAdminDashboard();
		}
	}
	
}
