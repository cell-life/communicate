package org.celllife.mobilisr.client.user;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface AdminUserListView extends EntityList {

	public void buildWidget(ListStore<BeanModel> store,	StoreFilterField<BeanModel> filter);
	Action getToggleVoidAction();
	Button getShowVoidedButton();
	ComboBox<BeanModel> getFilterOrgCombo();
	void setOrganizationStore(ListStore<BeanModel> store);
	void setFormObject(ViewModel<User> vm);
}
