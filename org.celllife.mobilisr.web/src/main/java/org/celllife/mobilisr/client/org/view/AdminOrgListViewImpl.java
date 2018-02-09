package org.celllife.mobilisr.client.org.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.org.AdminOrgListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTToggleButton;
import org.celllife.mobilisr.client.view.gxt.ToggleAction;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;

public class AdminOrgListViewImpl extends EntityListTemplateImpl implements AdminOrgListView{

	private Button newOrgButton;
	private ToggleAction toggleStateAction;
	private Action creditAccountAction;
	private MyGXTButton sendNotificationButton;
	private MyGXTToggleButton showVoided;


	@Override
	public void createView(){
		newOrgButton = new MyGXTButton("newOrgButton",
				Messages.INSTANCE.orgAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		sendNotificationButton = new MyGXTButton("sendNotificationButton",
				Messages.INSTANCE.orgSendNotification(), Resources.INSTANCE.messageTest(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		showVoided = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.trash(), IconAlign.LEFT, ButtonScale.SMALL);
		showVoided.setToolTip("Show deleted Campaigns");
		showVoided.setToggledTooltip("Show Campaigns");

		Button[] buttons;
		if (UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_SEND_NOTIFICATIONS)) {
			buttons = new Button[]{newOrgButton,sendNotificationButton};
		} else {
			buttons = new Button[]{newOrgButton};
		}

		layoutListTemplate(Messages.INSTANCE.orgListHeader(), buttons, true);
	}


	public void buildWidget( final ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add( new EntityIDColumnConfig( Organization.PROP_NAME, "Name", 150, "org") );
		configs.add( new ColumnConfig( Organization.PROP_BALANCE, "Unused credits", 150 ) );
		configs.add( new ColumnConfig( Organization.PROP_RESERVED, "Reserved credits", 150 ) );
		configs.add( new ColumnConfig( Organization.PROP_AVAILABLE_BALANCE, "Balance", 150 ) );
		ColumnConfig actions = new ColumnConfig( "organizationActions", "Actions", 150 );
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();
		creditAccountAction = new Action(null,
				"Click to credit the organisation's account",
				Resources.INSTANCE.credit(), "credit");
		if (UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE)){
			actionRenderer.addAction(creditAccountAction);
		}

		toggleStateAction = new ToggleAction(null, "Deactivate Organisation",
				Resources.INSTANCE.delete(), "void", "voided") {
		};
		toggleStateAction.setAltImage(Resources.INSTANCE.add());
		toggleStateAction.setAltTooltip("Activate Organisation");
		toggleStateAction.setAltIdPrefix("unvoid");

		if (UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_MANAGE)){
			actionRenderer.addAction(toggleStateAction);
		}
		actions.setRenderer(actionRenderer);
		configs.add(actions);

		renderEntityListGrid(store, filter, configs, "Click on an organisation name to see details and make changes", "Search for an Organisation");
		super.topToolBar.add(new FillToolItem());
		super.topToolBar.add(showVoided);

	}

	@Override
	public Button getNewEntityButton(){
		return newOrgButton;
	}

	@Override
	public Action getCreditAccountAction() {
		return creditAccountAction;
	}

	@Override
	public Action getToggleStateAction() {
		return toggleStateAction;
	}
	
	@Override
	public Button getSendNotificationButton() {
		return sendNotificationButton;
	}
	
	@Override
	public Button getShowVoidedButton() {
		return showVoided;
	}
}
