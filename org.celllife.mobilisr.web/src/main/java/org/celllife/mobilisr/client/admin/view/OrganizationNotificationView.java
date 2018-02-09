package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface OrganizationNotificationView extends EntityCreate<OrganisationNotificationViewModel> {

	void setOrganisationStore(ListStore<BeanModel> store);

	void addFieldSetListener(Listener<BaseEvent> listener);

	boolean checkForm();

	Button getTestButton();

}
