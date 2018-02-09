package org.celllife.mobilisr.client.contacts;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.ui.Anchor;

public interface GenericContactManagementView<T> {

	static int MODE_ADDALL = 0;
	static int MODE_MAX_ADD = 1;
	
	void addListener(EventType eventType, Listener<? extends BaseEvent> listener);
	
	void addAllSelected(int totalCount);

	void addEntityToLeftGrid(T entityObject);

	boolean addEntityToRightGrid(BeanModel beanModel);

	boolean addEntityToRightGrid(T entityObject);

	void buildLeftGroupGridPanel(ListStore<BeanModel> listEntityStore,
			StoreFilterField<BeanModel> listEntityFilter, GridCellRenderer<BeanModel> addBtnRenderer);

	void buildRightGroupGridPanel(ListStore<BeanModel> selectedEntityStore,
			StoreFilterField<BeanModel> selectedEntityFilter,
			GridCellRenderer<BeanModel> removeBtnRenderer);

	void clearAddNewEntityFieldValue();

	void clearRightGrid();

	void clearUI();

	void filterInRightGridStore(String filterValue, String filterProperty);

	Anchor getAddAllAnchor();

	Button getAddNewEntityButton();

	String getAddNewEntityFieldValue();

	int getCurrentRecordsDisplayed();

	PagingToolBar getListOfEntityToolBar();

	Button getNextTenButton();
	
	Button getDoneButton();

	Anchor getRemoveAllAnchor();

	int getTotalRecords();

	boolean isAddAllSelected();

	boolean isRemoveAllSelected();

	void removeAllSelected();

	void removeEntityFromSelectedGrid(BeanModel beanModel);

	void setNextTenButtonEnabled(boolean buttonStatus);

	void showPopup(String entityValueTxt);

	void updatePagingLabel(int currentRecordsDisplayed, int totalRecords);

	void showAddAllAnchor(boolean showAnchor);

	void setAddAllMode(int mode, int limitVal);
	
	int getMaxAddAllValue();

	int getAddAllMode();
	
	boolean isDirty();
}
