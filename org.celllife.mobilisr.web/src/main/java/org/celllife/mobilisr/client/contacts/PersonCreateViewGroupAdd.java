package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Interface for the UI popup when user wants to add groups to the newly created person
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
public interface PersonCreateViewGroupAdd {

	Button getDoneButton();
	Button getCancelButton();
	Button getAddGroupButton();
	
	String getGroupName();
	void setGroupName(String groupName);
	
	Anchor getAddContactToAllGroupAnchor();
	Anchor getDeselectAllGroupsAnchor();
	
	Grid<BeanModel> getAvailableGroupGrid();
	Grid<BeanModel> getSelectedGroupGrid();
	
	void showPopUp(String msisdn);
	void closePopUp();
	
	PagingToolBar getListOfGroupToolBar();
	PagingToolBar getSelectedGroupToolBar();
	void buildRightGroupGridPanel(ListStore<BeanModel> selGroupStore, StoreFilterField<BeanModel> selGroupFilterField, GridCellRenderer<BeanModel> delBtnListRenderer);
	void buildLeftGroupGridPanel(ListStore<BeanModel> listGroupStore, StoreFilterField<BeanModel> listGroupFilterField, GridCellRenderer<BeanModel> addBtnListRenderer);
	void addToRightGrid(ContactGroup contactGroup);
	void addGroupToSelGroupGrid(BeanModel beanModel);
	void addToLeftGrid(ContactGroup contactGroup);
	void clearNewGroupName();
	void removeAllFromSelectedGroupGrid();
	//void removeBeanFromSelGroupGrid(ContactGroup contactGroup);
	void removeBeanFromSelGroupGrid(BeanModel beanModel);
	void filterInStore(String filterValue);
	void clearFilterInStore();
	void setRemoveAllSelected(boolean isRemoveAllSelected);
	boolean isRemoveAllSelected();
	void setAddAllSelected(boolean isAddAllSelected);
	boolean isAddAllSelected();
	void setLeftGridStatus(boolean isEnabled);
	void clearRightGrid();
	void setRightGridEmptyText(String emptyText);
	void showNextResultsButton(boolean isShow);
	Button getNextTenButton();
	void clearForm();
	void setCurrentRecordsDisplayed(int currentRecordsDisplayed);
	void setTotalRecords(int totalRecords);
	void updateLabel();	
}
