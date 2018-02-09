package org.celllife.mobilisr.client.contacts;

import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class ContactRegionHandler extends BaseEventHandler<ContactEventBus> {

	public void onShowContactsRegion() {
		getEventBus().showContactsButtonPanel();
		getEventBus().showContactList(null);
	}
	
}
