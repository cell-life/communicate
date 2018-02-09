package org.celllife.mobilisr.client.contacts;

import java.util.List;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Anchor;

public interface ImportContactsView extends EntityCreate<Object> {

	void addCsvSampleDataToGrid(List<List<String>> csvData);
	
	void hideCsvSummaryPoup();

	void showCsvFileBrowser();

	void showAddGroupPopup(String data);

	void showSummaryPopup(int numOfContactsStores, int numOfErrors);
	
	void showCsvDataGrid(ListStore<ModelData> csvDataStore, int numOfColumns);

	List<String> getContactFieldsForCsvColumns();
	
	Anchor getGroupAnchor();

	Anchor getExportDataAnchor();
	
	GenericContactManagementView<ContactGroup> getAddPopup();

	MyGXTFormPanel getFormPanel();
	
	MyGXTButton getCsvSaveButton();

	MyGXTButton getCsvSummaryDoneButton();
	
	Button getCancelButton();
	
	void reset();
}
