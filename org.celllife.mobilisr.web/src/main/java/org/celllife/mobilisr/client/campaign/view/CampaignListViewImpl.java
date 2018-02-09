package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.campaign.CampaignListView;
import org.celllife.mobilisr.client.campaign.presenter.CampaignListPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTToggleButton;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.ToggleAction;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.resources.client.ImageResource;

public class CampaignListViewImpl extends EntityListTemplateImpl implements CampaignListView{

	private Button newCampaignButton;
	private Button rebuildSchedulesButton;

	private SimpleComboBox<String> filterTypeCombo;
	private ComboBox<BeanModel> filterOrgCombo;
	private AnchorCellRenderer campaignNameAnchor;
	private ToggleAction voidAction;
	private Action startStopAction;
	private Action viewRecipientsAction;
	private Action viewMessageLogsAction;
	private Action manageRecipientsAction;
	private MyGXTToggleButton showVoided;
	private MyGXTButton clearOrgFilterButton;

	@Override
	public void createView() {
		newCampaignButton = new MyGXTButton("newCampaignButton",
				null, Resources.INSTANCE.add(), IconAlign.LEFT, ButtonScale.SMALL);
		
		rebuildSchedulesButton = new MyGXTButton(
				"rebuildSchedulesButton", "Rebuild All Campaign Schedules",
				Resources.INSTANCE.refresh(), IconAlign.LEFT,
				ButtonScale.SMALL);
		
		filterTypeCombo = new SimpleComboBox<String>();
		filterTypeCombo.setTriggerAction(TriggerAction.ALL);
		filterTypeCombo.setEmptyText("Filter by type");
		filterTypeCombo.setName("type_filter");
		filterTypeCombo.add("ALL");
		filterTypeCombo.add(CampaignType.FLEXI.toString());
		filterTypeCombo.add(CampaignType.DAILY.toString());
		
		filterOrgCombo = new MyGXTComboBox<BeanModel>("Filter by Organisation",
				Organization.PROP_NAME, true);
		
		clearOrgFilterButton = new MyGXTButton("clearOrgFilter", null,
				Resources.INSTANCE.clearTrigger(), IconAlign.LEFT,
				ButtonScale.SMALL);
		clearOrgFilterButton.setToolTip("Clear organisation filter");
		clearOrgFilterButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterOrgCombo.clear();
			}
		});
		
		showVoided = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.trash(), IconAlign.LEFT, ButtonScale.SMALL);
		showVoided.setToolTip("Show deleted Campaigns");
		showVoided.setToggledTooltip("Show Campaigns");

		Button[] buttons = new Button[] { newCampaignButton };
		if (UserContext.hasPermission(MobilisrPermission.REBUILD_CAMPAIGN_SCHEDULES)){
			buttons = new Button[] { newCampaignButton, rebuildSchedulesButton };
		}

		layoutListTemplate(Messages.INSTANCE.campaignListHeader(), buttons, true);
	}

	@Override
	public void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter) {

		createActions();

		List<ColumnConfig> configs = getColumnConfigs(false);

		renderEntityListGrid(store, filter, configs, "Click on a campaign name to see details and make changes", "Search for Campaign");

		GridFilters filters = new GridFilters();
		ListStore<ModelData> statusStore = new ListStore<ModelData>();
		statusStore.add(status(CampaignStatus.INACTIVE.name()));
		statusStore.add(status(CampaignStatus.ACTIVE.name()));
		statusStore.add(status(CampaignStatus.SCHEDULE_ERROR.name()));
		statusStore.add(status(CampaignStatus.FINISHED.name()));
		ListFilter listFilter = new ListFilter(Campaign.PROP_STATUS, statusStore);
		listFilter.setDisplayProperty("status");
		filters.addFilter(listFilter);
		getEntityListGrid().addPlugin(filters);
		
		configureToolbar();
	}
	
	private void configureToolbar() {
		getTopToolBar().add(new FillToolItem());
		
		if(UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE)){
			getTopToolBar().add(filterTypeCombo);
			getTopToolBar().add(filterOrgCombo);
			getTopToolBar().add(clearOrgFilterButton);
			getTopToolBar().add(new SeparatorToolItem());
		}
		
		getTopToolBar().add(showVoided);
	}
	
	private ModelData status(String status) {
		ModelData model = new BaseModelData();
		model.set("status", status);
		return model;
	}

	private void createActions() {
		campaignNameAnchor = new AnchorCellRenderer("campaign");

		voidAction = new ToggleAction(null, "Click to delete this campaign",
				Resources.INSTANCE.delete(), "void", "voided") {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);
				final Campaign campaign = model.getBean();
				if (campaign.getStatus().isActiveState()){
					button.disable();
				}
				return button;
			}
		};
		voidAction.setAltTooltip("Click to undelete this campaign");
		voidAction.setAltImage(Resources.INSTANCE.add());
		voidAction.setAltIdPrefix("unvoid");


		startStopAction = new Action(null,null,null, "start_stop") {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);

				final Campaign campaign = model.getBean();
				final boolean isActive = campaign.isActive();
				final String btnText = isActive ? "Stop" : "Start";
				final ImageResource btnImage = isActive ? Resources.INSTANCE.stop() :
					Resources.INSTANCE.start();
				button.setIcon(createImage(btnImage));
				button.setToolTip("Click to " + btnText + " this campaign");

				return button;
			}
		};

		viewRecipientsAction = new Action(null,
				"Click to view this campaign's recipients", Resources.INSTANCE.viewRecipients(),
				"view_recipients");

		manageRecipientsAction = new Action(null,
				"Click to manage this campaign's recipients", Resources.INSTANCE.manageRecipients(),
				"manage_recipients") {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);

				final Campaign campaign = model.getBean();
				final boolean isActive = campaign.isActive();
				button.setEnabled(isActive);

				return button;
			}
		};

		viewMessageLogsAction = new Action(null,
				"Click to view this campaign's message log", Resources.INSTANCE.messageLogs(),
				"message_logs");
	}

	public void configureActions(boolean isAdminView){
		manageRecipientsAction.setText(isAdminView ? null : "Manage");
		viewMessageLogsAction.setText(isAdminView ? null : "Messages");
		viewRecipientsAction.setText(isAdminView ? null : "View");
	}

	/**
	 * @param isAdminView
	 * @return
	 */
	private List<ColumnConfig> getColumnConfigs(boolean isAdminView) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig campNameConfig = new ColumnConfig(Campaign.PROP_NAME, "Name", 100);
		if (UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
				MobilisrPermission.CAMPAIGNS_EDIT) ){
			campNameConfig.setRenderer(campaignNameAnchor);
		}
		configs.add(campNameConfig);

		configs.add(new ColumnConfig(Campaign.PROP_DURATION, "Duration", 30));

		ColumnConfig config = new ColumnConfig(Campaign.PROP_COST, "Credits Per Recipient", 50);
		config.setSortable(false);
		configs.add(config);

		config = new EntityIDColumnConfig(Campaign.PROP_COUNT, "Recipients", 50,
				"campaign_recipients");
		config.setSortable(false);
		configs.add(config);

		ColumnConfig statusConfig = new ColumnConfig(Campaign.PROP_STATUS, "Status", 70);
		statusConfig.setRenderer(new CampaignStatusRenderer());
		configs.add(statusConfig);

		if(isAdminView){
			ColumnConfig orgColumn = new ColumnConfig(Campaign.PROP_ORGANIZATION.concat(
					".").concat(Organization.PROP_NAME), "Organisation", 60);
			configs.add(orgColumn);
		}

		// add message logs
		ColumnConfig actions = new ColumnConfig("campaignActions", "Actions", 180);
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();

		configureActions(isAdminView);
		configureActionsColumn(renderer);

		actions.setRenderer(renderer);
		actions.setSortable(false);
		configs.add(actions);

		return configs;
	}

	private void configureActionsColumn(ButtonGridCellRenderer renderer) {
		if (UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
						MobilisrPermission.CAMPAIGNS_START_STOP))
			renderer.addAction(startStopAction);

		renderer.addAction(viewRecipientsAction);
		
		if (UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
						MobilisrPermission.CAMPAIGNS_MANAGE_RECIPIENTS))
			renderer.addAction(manageRecipientsAction);

		renderer.addAction(viewMessageLogsAction);
		
		if (UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
				MobilisrPermission.CAMPAIGNS_VOID))
			renderer.addAction(voidAction);
	}

	@Override
	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		Boolean isAdminView = (viewEntityModel == null ? false : viewEntityModel
				.isPropertyTrue(CampaignListPresenter.ADMIN_VIEW));
		String btnText = (isAdminView
				|| UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_CREATE) 
				? Messages.INSTANCE.campaignAddNew()
				: Messages.INSTANCE.campaignRequestNew());
		getNewEntityButton().setText(btnText);

		displaySuccessMsg((viewEntityModel == null ? null : viewEntityModel
						.getViewMessage()));

		if (isAdminView) {
			titleLabel.setText(Messages.INSTANCE.campaignListHeaderAdmin());
		} else {
			titleLabel.setText(Messages.INSTANCE.campaignListHeader());
		}

		List<ColumnConfig> columnConfigs = getColumnConfigs(isAdminView);
		reconfigureGrid(columnConfigs);
		
		filterOrgCombo.setVisible(isAdminView);
		clearOrgFilterButton.setVisible(isAdminView);
		
		// remove the right margin on the org filter combo
		filterOrgCombo.el().setStyleAttribute("marginRight", "0px");
	}

	@Override
	public Button getNewEntityButton() {
		return newCampaignButton;
	}

	@Override
	public AnchorCellRenderer getCampaignNameAnchor() {
		return campaignNameAnchor;
	}

	@Override
	public Button getRebuildSchedulesButton() {
		return rebuildSchedulesButton;
	}

	public SimpleComboBox<String> getTypeFilterCombo() {
		return filterTypeCombo;
	}

	@Override
	public Action getVoidAction() {
		return voidAction;
	}

	@Override
	public Action getStartStopAction() {
		return startStopAction;
	}

	@Override
	public Action getViewRecipientsAction() {
		return viewRecipientsAction;
	}

	@Override
	public Action getManageRecipientsAction() {
		return manageRecipientsAction;
	}

	@Override
	public Action getViewMessageLogsAction() {
		return viewMessageLogsAction;
	}
	
	@Override
	public Button getShowVoidedButton() {
		return showVoided;
	}
	
	@Override
	public ComboBox<BeanModel> getFilterOrgCombo() {
		return filterOrgCombo;
	}

	@Override
	public void setOrganizationStore(ListStore<BeanModel> store) {
		filterOrgCombo.setStore(store);
	}
	
	@Override
	public void clearFilterOrgCombo(){
		filterOrgCombo.enableEvents(false);
		filterOrgCombo.clear();
		filterOrgCombo.enableEvents(true);
	}

}
