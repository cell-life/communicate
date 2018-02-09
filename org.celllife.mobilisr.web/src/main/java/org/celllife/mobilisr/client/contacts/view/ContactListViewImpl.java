package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.contacts.ContactListView;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;

public class ContactListViewImpl extends EntityListTemplateImpl implements ContactListView{

	private Button newPersonButton;
	private MyGXTButton exportContactsBtn;

	private Action campaignStatusAction;
	private Action manageGroupsAction;
	private Action messageLogsAction;

	private GroupAddToContactViewImpl groupManagementView;

	@Override
	public void createView(){
		newPersonButton = new MyGXTButton("newPersonButton",
				Messages.INSTANCE.contactAddNew(), Resources.INSTANCE.add(), IconAlign.LEFT,
				ButtonScale.SMALL);
		exportContactsBtn = new MyGXTButton("csvExportContacts", "Export Contacts",
				Resources.INSTANCE.csv(), IconAlign.LEFT, ButtonScale.SMALL);

		groupManagementView = new GroupAddToContactViewImpl();

		Button[] buttons = new Button[]{newPersonButton};
		layoutListTemplate(Messages.INSTANCE.contactListHeader(), buttons, true);
	}

	@Override
	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter ) {

		createAction();

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add(new EntityIDColumnConfig(Contact.PROP_FIRST_NAME, "First Name", 50, "contact"));
		configs.add(new ColumnConfig(Contact.PROP_LAST_NAME, "Last Name", 50));

		ColumnConfig msisdn = new ColumnConfig( Contact.PROP_MSISDN, "Mobile Number", 50);
		msisdn.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex,
					int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
				Contact contact = model.getBean();
				String msisdn = model.get(property);
				if (contact.isInvalid()){
					msisdn += " (Invalid)";
				}
				return "<span>" + msisdn + "</span>";
			}
		});
		configs.add( msisdn);
		configs.add( new ColumnConfig( Contact.PROP_MOBILE_NETWORK, "Mobile Network", 50 ) );

		ColumnConfig actions = new ColumnConfig("Actions", "Actions", 100 );
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();
		renderer.addAction(campaignStatusAction);
		renderer.addAction(manageGroupsAction);
		renderer.addAction(messageLogsAction);
		actions.setRenderer(renderer);
		actions.setSortable(false);
		configs.add(actions);

		renderEntityListGrid(store, filter, configs, "Click on a contact mobile number to see " +
				"details and make changes", "Search for a contact");
		configureExportButton();
	}
	
	@Override
	protected GridView createGridView() {
		GridView view = super.createGridView();
		view.setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				if (model != null) {
					Boolean invalid = model.get(Contact.PROP_INVALID);
					if (invalid != null && invalid) {
						return "row-invalid";
					}
				}
				return "";
			}
		});
		
		return view;
	}

	private void createAction() {
		campaignStatusAction = new Action("Campaign Status","Click to view the campaigns this contact belongs to",Resources.INSTANCE.information(),"status");
		manageGroupsAction = new Action("Manage Groups","Click to manage the groups for this contact",Resources.INSTANCE.manageRecipients(),"manage_groups");
		messageLogsAction = new Action("Message Logs","Click to view the messages sent to this contact",Resources.INSTANCE.messageLogs(),"message_logs");

	}

	private void configureExportButton() {
		getTopToolBar().add(new FillToolItem());
		getTopToolBar().add(exportContactsBtn);
	}

	@Override
	public GenericContactManagementView<ContactGroup> getGroupPopup(){
		return groupManagementView;

	}

	@Override
	public void showGroupPopup(String msisdn) {
		groupManagementView.showPopup(msisdn);
	}


	@Override
	public Button getNewEntityButton() {
		return newPersonButton;
	}

	@Override
	public Button getExportContactsButton() {
		return exportContactsBtn;
	}

	@Override
	public Action getCampaignStatusAction() {
		return campaignStatusAction;
	}

	@Override
	public Action getManageGroupsAction() {
		return manageGroupsAction;
	}

	@Override
	public Action getMessageLogsAction() {
		return messageLogsAction;
	}
}
