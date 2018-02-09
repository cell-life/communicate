package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.GroupListView;
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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class GroupListViewImpl extends EntityListTemplateImpl implements GroupListView{

	private Button newGroupButton;
	private Action actionManageContacts;
	private Action actionViewContacts;
	private Action actionDeleteGroup;

	private TextField<String> changeField;
	private GenericContactManagementView<Contact> contactManagementView;

	@Override
	public void createView() {
		newGroupButton = new MyGXTButton("newGroupButton", Messages.INSTANCE.groupAddNew(),
				Resources.INSTANCE.add(), IconAlign.LEFT, ButtonScale.SMALL);
		actionManageContacts = new Action("Manage Contacts", "Manage Contacts",
				Resources.INSTANCE.manageRecipients(), "void");
		actionViewContacts = new Action("View Contacts", "View Contacts",
				Resources.INSTANCE.viewRecipients(), "void");
		actionDeleteGroup = new Action ("Delete Group", "Delete Group",
				Resources.INSTANCE.delete(), "deletebutton");
		changeField = new TextField<String>();
		contactManagementView = new ContactAddToGroupViewImpl();

		Button[] buttons = new Button[]{newGroupButton};

		layoutListTemplate(Messages.INSTANCE.groupListHeader(), buttons, true);
	}

	@Override
	public GenericContactManagementView<Contact> getAddPopup() {
		return contactManagementView;
	}


	@Override
	public void clearFormValues() {
		changeField.clear();

	}

	@Override
	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add( new EntityIDColumnConfig( ContactGroup.PROP_GROUP_NAME, "Group Name", 100, "group" ) );
		configs.add( new ColumnConfig( ContactGroup.PROP_GROUP_DESCRIPTION, "Description", 150 ) );

		ColumnConfig actions = new ColumnConfig("Actions", "Actions",89);
		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();
		actions.setRenderer(actionRenderer);
		configs.add(actions);

		actionRenderer.addAction(actionManageContacts);
		actionRenderer.addAction(actionViewContacts);
		actionRenderer.addAction(actionDeleteGroup);

		renderEntityListGrid(store, filter, configs, "Click on a group name to see details and make changes", "Search for Group");

	}

	@Override
	public Button getNewEntityButton() {
		return newGroupButton;
	}

	@Override
	public Action getManageContactsAction(){
		return actionManageContacts;
	}

	@Override
	public Action getViewContactsAction(){
		return actionViewContacts;
	}

	@Override
	public Action getDeleteGroupAction(){
		return actionDeleteGroup;
	}

	@Override
	public void showAddContactPopup(String groupName) {
		contactManagementView.showPopup(groupName);
	}

	public void setAddContactPopupSave(SelectionListener<ButtonEvent> listener){
		contactManagementView.getDoneButton().addSelectionListener(listener);
	}

}
