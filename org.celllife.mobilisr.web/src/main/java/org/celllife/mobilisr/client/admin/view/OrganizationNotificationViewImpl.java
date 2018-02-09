package org.celllife.mobilisr.client.admin.view;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.BaseFormView;
import org.celllife.mobilisr.client.view.gxt.CenterTopLayout;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;

public class OrganizationNotificationViewImpl extends BaseFormView<OrganisationNotificationViewModel> implements OrganizationNotificationView {

	private TextField<String> subject;
	private HtmlEditor message;
	
	private CheckBoxSelectionModel<BeanModel> sm;
	private Grid<BeanModel> orgGrid;
	private FieldSet fieldSet;
	private CheckBox includeUsers;
	private MyGXTButton testButton;
	
	@Override
	public void createView() {
		super.createView();
		
		testButton = new MyGXTButton("testButton", Messages.INSTANCE.sendTestMessage());
		
		subject = new MyGXTTextField("* Subject", "subject", false, "");
		message = new HtmlEditor();
		message.setFieldLabel("* Message text");
		message.setHeight(200);
		ToolTipConfig tipConfig = new ToolTipConfig("Notification text info",
				"The notification text is inserted into an email template similar to this:<hr/><br/>" +
				"Dear Contact Name<br/><br/>" +
				"<span style=\"color: rgb(255, 0, 0);\">Your message text</span><br/><br/>" +
				"Best regards<br>Cell-Life<br/><br/><hr/>" +
				"<b>Send a test message to yourself before sending the notification!</b>");
		tipConfig.setDismissDelay(10000);
		tipConfig.setCloseable(true);
		tipConfig.setMaxWidth(400);
		tipConfig.setAnchorToTarget(true);
		tipConfig.setAnchor("left");
		message.setToolTip(tipConfig);
		
		includeUsers = new CheckBox();
		includeUsers.setName("includeUsers");
		includeUsers.setFieldLabel("Include organisation users");
		includeUsers.setToolTip("If selected the notification will be sent to all Communicate" +
				" users who belong to the organisations selected.");
		
		fieldSet = new FieldSet();
		fieldSet.setId("selectOrgs");
		fieldSet.setHeading("Send to selected organisations only");
		fieldSet.setLayout(new RowLayout());
		fieldSet.setCollapsible(true);
		fieldSet.setCheckboxToggle(true);
		fieldSet.collapse();
		
		addTitleLabel("Send a notification email");
		
		configureFormPanel();
		
		add(getFormPanel(), new RowData(1, 1));

		LayoutContainer formContainer = new LayoutContainer();
		FormLayout layout = new FormLayout();
		layout.setLabelWidth(140);
		layout.setDefaultWidth(600);
		formContainer.setLayout(layout);
		formContainer.add(subject, new FormData());
		formContainer.add(message, new FormData());
		formContainer.add(includeUsers, new FormData());
		formContainer.add(fieldSet, new FormData("0 0"));
		
		LayoutContainer formCenterContainer = new LayoutContainer();
		formCenterContainer.setSize("100%", "100%");
		formCenterContainer.setLayout(new CenterTopLayout());
		formCenterContainer.add(formContainer);

		createOrganisationGrid();

		getFormPanel().add(formCenterContainer, new RowData(1,-1));

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(testButton, true);
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(getFormPanel(), true);
	}
	
	public void createOrganisationGrid() {
		sm = new CheckBoxSelectionModel<BeanModel>();
		
		ContentPanel container = new ContentPanel();
		container.setHeaderVisible(true);
		container.setHeading("Organisations");
		container.setLayout(new FitLayout());
		container.setSize("100%", "100%");
		container.setBodyBorder(false);
		container.setBorders(true);
		
		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");

		ColumnModel cm = new ColumnModel(getColumnConfigs());

		orgGrid = new Grid<BeanModel>(new ListStore<BeanModel>(), cm);
		orgGrid.setLoadMask(true);
		orgGrid.setView(gridView);
		orgGrid.setBorders( true );
		orgGrid.setHeight("100%");
		orgGrid.setStripeRows( true );
		
		orgGrid.setSelectionModel(sm);
		orgGrid.addPlugin(sm);

		container.add(orgGrid);
		fieldSet.add(container, new RowData(1, 0.8, new Margins(0, 5, 0, 5)));
	}
	
	private List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		ColumnConfig checkColumn = sm.getColumn();
		checkColumn.setRenderer(new GridCellRenderer<BeanModel>() {
			public String render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				config.cellAttr = "rowspan='2'";
				Organization p = model.getBean();
				return "<div class='x-grid3-row-checker' id='" + p.getName()
						+ "'>&#160;</div>";
			}
		});
		configs.add(checkColumn);
		configs.add(new ColumnConfig(Organization.PROP_NAME,
				"Organisation Name", 100));
		configs.add(new ColumnConfig(Organization.PROP_CONTACT_PERSON,
				"Contact Person", 100));
		configs.add(new ColumnConfig(Organization.PROP_CONTACT_EMAIL,
				"Contact Email", 100));
		return configs;
	}
	
	@Override
	public void setOrganisationStore(ListStore<BeanModel> store){
		orgGrid.reconfigure(store, new ColumnModel(getColumnConfigs()));
	}
	
	@Override
	public ViewModel<OrganisationNotificationViewModel> getFormObject() {
		ViewModel<OrganisationNotificationViewModel> formObject = super.getFormObject();
		OrganisationNotificationViewModel ovm = formObject.getModelObject();
		
		ovm.setMessage(message.getRawValue());
		boolean sendToAll = !fieldSet.isExpanded();
		ovm.setSendToAll(sendToAll);
		
		if (!sendToAll){
			List<BeanModel> items = sm.getSelectedItems();
			for (BeanModel item : items) {
				Organization org = item.getBean();
				ovm.getOrganisationList().add(org.getId());
			}
		} else {
			ovm.getOrganisationList().clear();
		}
		
		return formObject;
	}
	
	@Override
	public boolean checkForm() {
		String msg = message.getRawValue();
		msg = msg.replaceAll("<(.|\n)*?>", "");
		return !msg.isEmpty();
	}
	
	@Override
	public void setFormObject(ViewModel<OrganisationNotificationViewModel> viewModel) {
		super.setFormObject(viewModel);
		message.clear();
		sm.deselectAll();
		fieldSet.collapse();
		submitButton.setText(Messages.INSTANCE.send());
	}
	
	@Override
	public void addFieldSetListener(Listener<BaseEvent> listener){
		fieldSet.addListener(Events.Expand, listener);
	}
	
	@Override
	public Button getTestButton() {
		return testButton;
	}
}
