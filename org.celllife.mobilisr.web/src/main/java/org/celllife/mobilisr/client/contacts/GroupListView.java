package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface GroupListView extends EntityList {

	void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter);

	public Action getManageContactsAction();
	
	public Action getViewContactsAction();
	
	public Action getDeleteGroupAction();

	void showAddContactPopup(String groupName);

	GenericContactManagementView<Contact> getAddPopup();

	void clearFormValues();

	void setAddContactPopupSave(SelectionListener<ButtonEvent> listener);
}
