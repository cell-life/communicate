package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;

public interface LostMessagesView extends EntityList {

	/**
	 * Builds widgets in the view using objects passed from the presenter.
	 */
	void buildWidgets(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter);

	/**
	 * Get the selection model (in order to get/set the selection).
	 */
	CheckBoxSelectionModel<BeanModel> getSelectionModel();

	/**
	 * Get the delete button.
	 */
	Button getDeleteButton();

	/**
	 * Get the re-process button.
	 */
	Button getReprocessButton();

	void clearFilters();

}
