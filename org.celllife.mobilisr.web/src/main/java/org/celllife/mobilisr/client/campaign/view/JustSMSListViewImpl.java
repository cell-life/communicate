package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.JustSMSListView;
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
import org.celllife.mobilisr.constants.CampaignStatus;
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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;

public class JustSMSListViewImpl extends EntityListTemplateImpl implements JustSMSListView {

	private Button newCampaignButton;
	private Action scheduleAction;
	private Action viewCampaignSummaryAction;
	private Action viewRecipientsAction;
	private ToggleAction toggleVoidAction;
	private Action viewMessageLogsAction;
	private AnchorCellRenderer campaignNameAnchor;
	private MyGXTToggleButton showVoided;
	private ComboBox<BeanModel> filterOrgCombo;
	private MyGXTButton clearOrgFilterButton;

	@Override
	public void createView() {
		newCampaignButton = new MyGXTButton("newCampaignButton",
				"Add New 'Just Send SMS' Campaign", Resources.INSTANCE.add(), IconAlign.LEFT,
				ButtonScale.SMALL);
		
		showVoided = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.trash(), IconAlign.LEFT, ButtonScale.SMALL);
		showVoided.setToolTip("Show deleted Campaigns");
		showVoided.setToggledTooltip("Show Campaigns");
		
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

		Button[] buttons = new Button[] { newCampaignButton };
		layoutListTemplate(Messages.INSTANCE.justSmsListHeader(), buttons, true);

	}

	@Override
	public void buildWidget(final ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter) {

		createActions();

		List<ColumnConfig> configs = getColumnConfigs(false);

		renderEntityListGrid(store, filter, configs, "Click on a campaign name to see details and make changes", "Search for Campaign");

		configureToolbar();

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
	}

	private void configureToolbar() {
		getTopToolBar().add(new FillToolItem());
		if(UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE)){
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

	private List<ColumnConfig> getColumnConfigs(Boolean isAdminView) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig campNameConfig = new ColumnConfig(Campaign.PROP_NAME, "Name", 150);
		campNameConfig.setRenderer(campaignNameAnchor);
		configs.add(campNameConfig);

		ColumnConfig startDateColumnConfig = new ColumnConfig(Campaign.PROP_START_DATE, "Send Date", 50);
		startDateColumnConfig.setDateTimeFormat(DateTimeFormat.getFormat("dd MMMM yyyy"));
		configs.add(startDateColumnConfig);

		ColumnConfig config = new ColumnConfig(Campaign.PROP_COUNT, "Recipients", 50);
		config.setSortable(false);
		configs.add(config);

		ColumnConfig statusConfig = new ColumnConfig(Campaign.PROP_STATUS, "Status", 100);
		statusConfig.setRenderer(new CampaignStatusRenderer());
		configs.add(statusConfig);

		if(isAdminView){
			ColumnConfig orgColumn = new ColumnConfig(Campaign.PROP_ORGANIZATION.concat(
					".").concat(Organization.PROP_NAME), "Organisation", 60);
			configs.add(orgColumn);
		}

		ColumnConfig actions = new ColumnConfig("actions", "Actions", 200);
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();

		addActionsToColumn(renderer);

		actions.setRenderer(renderer);
		actions.setSortable(false);

		configs.add(actions);
		return configs;
	}

	private void addActionsToColumn(ButtonGridCellRenderer renderer) {
		renderer.addAction(scheduleAction);
		renderer.addAction(viewMessageLogsAction);
		renderer.addAction(viewCampaignSummaryAction);
		renderer.addAction(viewRecipientsAction);
		renderer.addAction(toggleVoidAction);
	}

	private void createActions() {
		campaignNameAnchor = new AnchorCellRenderer("justsms"){
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				final CampaignStatus status = model.get(Campaign.PROP_STATUS);
				if (CampaignStatus.SCHEDULED.equals(status)
						|| CampaignStatus.RUNNING.equals(status)
						|| CampaignStatus.FINISHED.equals(status)){
					return null;
				}
				return super.render(model, property, config, rowIndex, colIndex, store, grid);
			}
		};

		scheduleAction = new Action(null, "Click to unschedule this campaign",
				Resources.INSTANCE.stop(), "unschedule") {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);
				final Campaign campaign = model.getBean();
				button.setEnabled(campaign.isScheduled());
				ImageResource image = (campaign.isScheduled()) ? Resources.INSTANCE.scheduleNoStop()
						: Resources.INSTANCE.scheduleNoStopGrey();
				button.setIcon(createImage(image));

				return button;
			}
		};

		viewMessageLogsAction = new Action("Message Logs",
				"Click to view this campaign's message log",
				Resources.INSTANCE.messageLogs(), "message_logs");

		viewCampaignSummaryAction = new Action("Summary",
				"Click to view this campaign's summary information",
				Resources.INSTANCE.summary(), "view-summary");

		viewRecipientsAction = new Action(Messages.INSTANCE.campaignViewRecipients(),
				"Click to view this campaign's recipients", Resources.INSTANCE.viewRecipients(),
				"view_recipients");

		// void / unvoid button
		toggleVoidAction = new ToggleAction(null, "Click to delete this campaign",
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
		toggleVoidAction.setAltTooltip("Click to undelete this campaign");
		toggleVoidAction.setAltImage(Resources.INSTANCE.add());
		toggleVoidAction.setAltIdPrefix("unvoid");
	}

	@Override
	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		Boolean isAdminView = (viewEntityModel == null ? false : viewEntityModel
				.isPropertyTrue(MobilisrBasePresenter.ADMIN_VIEW));
		String titleText = (isAdminView == true ? Messages.INSTANCE.justSmsListHeaderAdmin()
				: Messages.INSTANCE.justSmsListHeader());
		setTitleLabel(titleText);

		displaySuccessMsg(
				(viewEntityModel == null ? null : viewEntityModel
						.getViewMessage()));

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
	public Button getShowVoidedButton() {
		return showVoided;
	}

	@Override
	public void setTitleLabel(String headerTitle) {
		super.setTitleLabel(headerTitle);
	}

	@Override
	public Action getScheduleAction() {
		return scheduleAction;
	}

	@Override
	public Action getViewCampaignSummaryAction() {
		return viewCampaignSummaryAction;
	}

	@Override
	public Action getViewRecipientsAction() {
		return viewRecipientsAction;
	}

	@Override
	public Action getToggleVoidAction() {
		return toggleVoidAction;
	}

	@Override
	public Action getViewMessageLogsAction() {
		return viewMessageLogsAction;
	}

	@Override
	public AnchorCellRenderer getCampaignNameAnchor() {
		return campaignNameAnchor;
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
