package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.ImportContactsView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ImportContactsViewImpl extends EntityCreateTemplateImpl<Object> implements
		ImportContactsView {

	private Label titleLabel;
	private FileUploadField csvFileUploadField;
	private MyGXTButton uploadButton;
	private MyGXTButton cancelButton;
	private Anchor manageGroupAnchor;

	private Window csvSummaryWindow;
	private Anchor exportDataAnchor;
	private MyGXTButton doneButton;

	private SimpleComboBox<String>[] contactFields;
	private Grid<ModelData> csvGrid;

	private GenericContactManagementView<ContactGroup> groupManagementView;

	@Override
	public void createView() {
		super.createView();

		uploadButton = new MyGXTButton("Continue to Step 2");
		cancelButton = new MyGXTButton(Messages.INSTANCE.cancel());

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(uploadButton, true);
		formButtons.put(cancelButton, false);

		layoutCreateTemplate("Import Contacts: Step 1 (of 2)");

		groupManagementView = new GroupAddToContactViewImpl();

		titleLabel = new Label();
		csvFileUploadField = new FileUploadField();
		manageGroupAnchor = new Anchor(
				"Manage Group Memberships for Imported Contacts", "#");

		csvSummaryWindow = new Window();
		exportDataAnchor = new Anchor("Export Data", "#");
		doneButton = new MyGXTButton("Close");

		showCsvFileBrowser();

		initCsvFileUpload();

		formElementContainer.add(csvFileUploadField, new FormData());
		addAndConfigFormButtons(formButtons, false);
	}

	/**
	 * 
	 */
	private void initCsvFileUpload() {
		csvFileUploadField.setId("csvFile");
		csvFileUploadField.setName("csvFile");
		csvFileUploadField.setAllowBlank(false);
		csvFileUploadField.setFieldLabel("* CSV File");
		csvFileUploadField.setValidator(new Validator() {

			@Override
			public String validate(Field<?> field, String value) {
				String fileNm = value.toLowerCase();
				if (!fileNm.endsWith(".csv")) {
					return "Only CSV files are accepted";
				}
				return null; /* 'null' means it's valid file */
			}
		});
	}

	@Override
	public void showCsvFileBrowser() {

		formPanel.removeAll();
		formPanel.clear();
		titleLabel.setText("Import Contacts: Step 1 (of 2)");
		getHeaderLabel().setText("Import Contacts: Step 1 (of 2)");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());

		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setAction(GWT.getModuleBaseURL() + "readCsv");
		formPanel.setPadding(5);
		formPanel.setBorders(true);

		formPanel.setLayout(new RowLayout());
		formPanel.setAutoWidth(true);
		formPanel.add(formCenterContainer, new RowData(1, 1, new Margins(10)));

		uploadButton.setText("Continue to Step 2");
		setErrorMessage(null);
		doLayout();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void showCsvDataGrid(ListStore<ModelData> csvDataStore,
			int numOfColumns) {

		formPanel.removeAll();
		formPanel.clear();
		getHeaderLabel().setText("Import Contacts: Step 2 (of 2)");
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		contactFields = new SimpleComboBox[numOfColumns];

		ToolBar toolBar = new ToolBar();

		for (int i = 0; i < numOfColumns; i++) {

			contactFields[i] = new SimpleComboBox<String>();
			contactFields[i].setId("combo" + i);
			contactFields[i].add("Not Applicable");
			contactFields[i].add(Messages.INSTANCE.contactMobileNumber());
			contactFields[i].add(Messages.INSTANCE.contactFirstName());
			contactFields[i].add(Messages.INSTANCE.contactLastName());
			contactFields[i].add(Messages.INSTANCE.contactMobileNetwork());
			contactFields[i].setEmptyText("* Select Data type");
			contactFields[i].setSimpleValue("");
			contactFields[i].setWidth(150);
			contactFields[i].setTriggerAction(TriggerAction.ALL);

			toolBar.add(contactFields[i]);

			ColumnConfig columnConfig = new ColumnConfig();
			columnConfig.setId("col" + i);
			columnConfig.setHeader("Column " + (i + 1));
			columnConfig.setWidth(150);
			configs.add(columnConfig);

		}

		ColumnModel columnModel = new ColumnModel(configs);

		csvGrid = new Grid<ModelData>(csvDataStore, columnModel);
		csvGrid.setBorders(true);
		csvGrid.setHeight("70%");
		csvGrid.setStripeRows(true);
		// csvGrid.setSize(500,500);

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Select a heading for each column of your data. Please note \"Mobile Number\" is compulsory");
		cp.setFrame(true);
		cp.setLayout(new FitLayout());
		cp.setTopComponent(toolBar);
		cp.setBottomComponent(new AdapterField(
				new Label(
						"This table shows upto maximum of first 100 rows from your CSV file")));
		cp.setFooter(true);
		cp.add(csvGrid);
		cp.setVisible(true);

		getCsvSaveButton().setText("Save Contacts");

		formPanel.add(cp);
		formPanel.add(manageGroupAnchor);
		doLayout();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<String> getContactFieldsForCsvColumns() {

		List<String> selectedFields = new ArrayList<String>(4);
		int numOfTimesMsisdnSelected = 0;

		for (SimpleComboBox simpleComboBox : contactFields) {
			String userData = simpleComboBox.getRawValue();

			if (userData.equals(Messages.INSTANCE.contactMobileNumber())) {
				numOfTimesMsisdnSelected++;
				selectedFields.add(Contact.PROP_MSISDN);
			} else if (userData.equals(Messages.INSTANCE.contactFirstName())) {
				selectedFields.add(Contact.PROP_FIRST_NAME);
			} else if (userData.equals(Messages.INSTANCE.contactLastName())) {
				selectedFields.add(Contact.PROP_LAST_NAME);
			} else if (userData
					.equals(Messages.INSTANCE.contactMobileNetwork())) {
				selectedFields.add(Contact.PROP_MOBILE_NETWORK);
			} else {
				selectedFields.add("na");
			}
		}

		if (!selectedFields.contains(Contact.PROP_MSISDN)) {
			// That means user hasnt selected any msisdn
			setErrorMessage("Please select \"Mobile Number\" from the data below");
			selectedFields = null;

		} else {
			if (numOfTimesMsisdnSelected > 1) {
				setErrorMessage("Please select only one column for the \"Mobile Number\" from the data below");
				selectedFields = null;
			} else {
				// User has selected one msisdn, clear error text if any shown
				// (might be thr from previous trials)
				setErrorMessage(null);
			}
		}

		return selectedFields;
	}

	@Override
	public void addCsvSampleDataToGrid(List<List<String>> csvData) {
		ListStore<ModelData> listStore = new ListStore<ModelData>();
		int totalColumns = 0;
		for (List<String> rowData : csvData) {
			totalColumns = rowData.size();
			BaseModelData baseModelData = new BaseModelData();

			for (int k = 0; k < totalColumns; k++) {
				baseModelData.set("col" + k, rowData.get(k));
			}
			listStore.add(baseModelData);
		}
		showCsvDataGrid(listStore, totalColumns);
	}

	@Override
	public void showSummaryPopup(int numOfContactsStores, int numOfErrors) {

		csvSummaryWindow.removeAll();
		csvSummaryWindow.setTitle("Saving Contacts");
		csvSummaryWindow.setLayout(new RowLayout(Orientation.VERTICAL));

		String contactsStoredTxt = "Well Done ! " + numOfContactsStores
				+ " have succesfully been imported into Mobilisr.";

		Label msgSuccesStoredLabel = new Label(contactsStoredTxt);
		csvSummaryWindow.add(msgSuccesStoredLabel, new RowData(1, 0.2,
				new Margins(5)));

		if (numOfErrors > 0) {

			String numOfErrorText = "We found "
					+ numOfErrors
					+ " errors in your data so weren't able to import all of your contacts. To see a "
					+ "detailed list of these please select the link below to export the data";
			Label msgErrorStoredLabel = new Label(numOfErrorText);
			csvSummaryWindow.add(msgErrorStoredLabel, new RowData(1, 0.3,
					new Margins(5)));
			csvSummaryWindow.add(exportDataAnchor, new RowData(1, 0.2,
					new Margins(5)));
		}

		csvSummaryWindow.add(doneButton, new RowData(0.3, 0.2, new Margins(5)));
		csvSummaryWindow.setHeight(180);
		csvSummaryWindow.setWidth(350);
		csvSummaryWindow.setVisible(true);
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void reset() {
		contactFields = null;
		csvFileUploadField.clear();
		groupManagementView.clearRightGrid();
		groupManagementView.updatePagingLabel(0, 0);
	}

	@Override
	public ViewModel<Object> getFormObject() {
		return null;
	}

	@Override
	public GenericContactManagementView<ContactGroup> getAddPopup() {
		return groupManagementView;
	}

	@Override
	public MyGXTButton getCsvSaveButton() {
		return uploadButton;
	}

	@Override
	public MyGXTFormPanel getFormPanel() {
		return formPanel;
	}

	@Override
	public Anchor getGroupAnchor() {
		return manageGroupAnchor;
	}

	@Override
	public void showAddGroupPopup(String msisdn) {
		groupManagementView.showPopup(msisdn);
	}

	@Override
	public Anchor getExportDataAnchor() {
		return exportDataAnchor;
	}

	@Override
	public MyGXTButton getCsvSummaryDoneButton() {
		return doneButton;
	}

	@Override
	public void hideCsvSummaryPoup() {
		csvSummaryWindow.hide();
	}

	@Override
	public Button getCancelButton() {
		return cancelButton;
	}
}