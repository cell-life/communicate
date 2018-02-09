package org.celllife.mobilisr.client.filter;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface FilterListView extends EntityList{

	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter);
	
	void setFormObject(ViewModel<?> viewEntityModel);

	AnchorCellRenderer getFilterNameAnchor();

	Action getToggleActiveStateAction();

	Action getViewFilterInboxAction();

	Action getToggleVoidStateAction();

	Button getShowVoidedButton();
	
	void setOrganizationStore(ListStore<BeanModel> store);

	ComboBox<BeanModel> getFilterOrgCombo();

	void clearFilterOrgCombo();
}
