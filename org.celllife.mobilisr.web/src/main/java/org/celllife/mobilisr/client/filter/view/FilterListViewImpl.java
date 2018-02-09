package org.celllife.mobilisr.client.filter.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.filter.FilterListView;
import org.celllife.mobilisr.client.filter.presenter.FilterListPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTToggleButton;
import org.celllife.mobilisr.client.view.gxt.ToggleAction;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

public class FilterListViewImpl extends EntityListTemplateImpl implements FilterListView {

	private Button createNewFilterButton;

	private boolean isAdminView = false;

	private AnchorCellRenderer filterNameAnchor;

	private ToggleAction toggleActiveStateAction;
	
	private ToggleAction toggleVoidStateAction;

	private Action viewFilterInboxAction;

	private MyGXTToggleButton showVoided;

	private ComboBox<BeanModel> filterOrgCombo;

	private MyGXTButton clearOrgFilterButton;

	@Override
	public void createView() {
		createNewFilterButton = new MyGXTButton("newFilterButton", null,
				Resources.INSTANCE.add(), IconAlign.LEFT, ButtonScale.SMALL);
		
		showVoided = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.trash(), IconAlign.LEFT, ButtonScale.SMALL);
		showVoided.setToolTip("Show deleted Filters");
		showVoided.setToggledTooltip("Show filters");
		
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
		
		Button[] buttons = new Button[] {createNewFilterButton};
		layoutListTemplate(Messages.INSTANCE.filterListHeader(), buttons, true);
	}

	@Override
	public void buildWidget( final ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {
		createActions();
		List<ColumnConfig> configs = getColumnConfigs();
		renderEntityListGrid(store, filter, configs, null, Messages.INSTANCE.filterSearch());
		
		super.topToolBar.add(new FillToolItem());
		
		if(UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_VIEW)){
			getTopToolBar().add(filterOrgCombo);
			getTopToolBar().add(clearOrgFilterButton);
			getTopToolBar().add(new SeparatorToolItem());
		}
		
		super.topToolBar.add(showVoided);
	}

	private void createActions() {
		filterNameAnchor = new AnchorCellRenderer("messagefilter");
		
		// activate / deactivate button
		toggleActiveStateAction = new ToggleAction("Activate", "Click to activate this filter",
				Resources.INSTANCE.start(), "deactivate", MessageFilter.PROP_ACTIVE) {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);
				final MessageFilter filter = model.getBean();
				if (filter.isVoided()){
					button.disable();
				}
				return button;
			}
		};
		toggleActiveStateAction.setAltText(Messages.INSTANCE.filterDeactivate());
		toggleActiveStateAction.setAltTooltip("Click to deactivate this Message Filter");
		toggleActiveStateAction.setAltImage(Resources.INSTANCE.stop());
		toggleActiveStateAction.setAltIdPrefix("activate");
		
		// void / unvoid button
		toggleVoidStateAction = new ToggleAction(null, "Click to delete this filter",
				Resources.INSTANCE.delete(), "void", MessageFilter.PROP_VOIDED) {
			@Override
			public Button render(BeanModel model) {
				Button button = super.render(model);
				final MessageFilter filter = model.getBean();
				if (filter.isActive()){
					button.disable();
				}
				return button;
			}
		};
		toggleVoidStateAction.setAltTooltip("Click to undelete this filter");
		toggleVoidStateAction.setAltImage(Resources.INSTANCE.add());
		toggleVoidStateAction.setAltIdPrefix("unvoid");
		
		viewFilterInboxAction = new Action(
				Messages.INSTANCE.filterViewInbox(),
				Messages.INSTANCE.filterViewInbox(), 
				Resources.INSTANCE.messageLogs(), "viewInbox");
	}

	List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig filterName = new ColumnConfig(MessageFilter.PROP_NAME,
				Messages.INSTANCE.filterColumnName(), 100);
		
		if (isAdminView){
			if (UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_EDIT)) {
				filterName.setRenderer(filterNameAnchor);
			}
		} else if (UserContext.hasPermission(MobilisrPermission.FILTERS_EDIT)){
			filterName.setRenderer(filterNameAnchor);
		}
		
		configs.add(filterName);

		configs.add(new ColumnConfig(MessageFilter.PROP_TYPE, Messages.INSTANCE.filterColumnType(), 50 ));
		ColumnConfig channelColumn = new ColumnConfig(MessageFilter.PROP_CHANNEL, Messages.INSTANCE.filterColumnChannel(), 50 );
		channelColumn.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				MessageFilter filter = model.getBean();
				Channel channel = filter.getChannel();
				
				return channel.getName() + " (" + channel.getShortCode() + ")";
			}
		});
		configs.add(channelColumn);
		configs.add(new ColumnConfig(MessageFilter.PROP_ACTIONS_LABEL, Messages.INSTANCE.filterActions(), 180 ));
		ColumnConfig actions = new ColumnConfig("messageFilterTasks", Messages.INSTANCE.filterManagement(), 100);
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();
		
		if (isAdminView){
			if (UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_START_STOP)) {
				actionRenderer.addAction(toggleActiveStateAction);
			}
		} else if (UserContext.hasPermission(MobilisrPermission.FILTERS_START_STOP)){
			actionRenderer.addAction(toggleActiveStateAction);
		}

		actionRenderer.addAction(viewFilterInboxAction);
		
		if (isAdminView){
			if (UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_START_STOP)) {
				actionRenderer.addAction(toggleVoidStateAction);
			}
		} else if (UserContext.hasPermission(MobilisrPermission.FILTERS_START_STOP)){
			actionRenderer.addAction(toggleVoidStateAction);
		}
		
		actions.setRenderer(actionRenderer);
		configs.add(actions);
		
		if (isAdminView) {
			configs.add(new ColumnConfig(MessageFilter.PROP_ORGANIZATION.concat(
				".").concat(Organization.PROP_NAME), Messages.INSTANCE.filterOrganisation(), 60));
		}

		return configs;
	}

	@Override
	public void setFormObject(ViewModel<?> viewEntityModel) {
		isAdminView = (viewEntityModel == null ? false : viewEntityModel
				.isPropertyTrue(FilterListPresenter.ADMIN_VIEW));
		String btnText = isAdminView ? Messages.INSTANCE.filterAddNew()
				: Messages.INSTANCE.filterRequestNew();
		getNewEntityButton().setText(btnText);
		setTitleLabel(isAdminView ? Messages.INSTANCE.filterListHeaderAdmin()
				: Messages.INSTANCE.filterListHeader());
		
		displaySuccessMsg(
				(viewEntityModel == null ? null : viewEntityModel
						.getViewMessage()));
		
		List<ColumnConfig> configs = getColumnConfigs();
		reconfigureGrid(configs);
		
		filterOrgCombo.setVisible(isAdminView);
		clearOrgFilterButton.setVisible(isAdminView);
		
		// remove the right margin on the org filter combo
		filterOrgCombo.el().setStyleAttribute("marginRight", "0px");
	}

	@Override
	public Button getNewEntityButton() {
		return createNewFilterButton;
	}
	
	@Override
	public Action getToggleActiveStateAction() {
		return toggleActiveStateAction;
	}
	
	@Override
	public Action getToggleVoidStateAction() {
		return toggleVoidStateAction;
	}
	
	@Override
	public Action getViewFilterInboxAction() {
		return viewFilterInboxAction;
	}
	
	@Override
	public AnchorCellRenderer getFilterNameAnchor() {
		return filterNameAnchor;
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
