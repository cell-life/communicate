package org.celllife.mobilisr.client.filter.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.filter.FilterCreateView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.EntityStoreProvider;
import org.celllife.mobilisr.client.reporting.PConfigDialog;
import org.celllife.mobilisr.client.template.view.BaseFormView;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.ComboBoxFieldBinding;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class FilterCreateViewImpl extends BaseFormView<MessageFilterViewModel> implements FilterCreateView {

	private ComboBox<BeanModel> orgComboBox;
	private MyGXTTextField filterName;
	private ComboBox<BeanModel> filterTypeCombo;
	private MyGXTLabelField typeInfo;
	private ComboBox<BeanModel> channelCombo;
	private MyGXTButton addActionButton;
	private ListStore<BeanModel> actionsStore;
	private EntityStoreProvider entityStoreProvider;
	private SelectionChangedListener<BeanModel> typeComboListener;

	@Override
	public void createView() {
		super.createView();
		setBorders(false);
		
		orgComboBox = new MyGXTComboBox<BeanModel>("Select an Organisation",
				Organization.PROP_NAME, true);
		orgComboBox.setName(MessageFilter.PROP_ORGANIZATION);
		filterName = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.filterName(),
				"name", false, "e.g. My Recruitment line");
		filterName.setId("filterName");
		filterTypeCombo = new ComboBox<BeanModel>();
		filterTypeCombo.setName(MessageFilterViewModel.PROP_TYPE_DESCRIPTOR);
		typeInfo = new MyGXTLabelField("","");
		channelCombo = new ComboBox<BeanModel>();
		channelCombo.setName("channel");
		addActionButton = new MyGXTButton("addActionButton",
				Messages.INSTANCE.filterAddNewAction(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		actionsStore = new ListStore<BeanModel>();
		
		addTitleLabel("");
		
		configureFormPanel();
		
		add(getFormPanel(), new RowData(1, 1));

		createFilterDetails();

		createActionsGrid();

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		addAndConfigFormButtons(formButtons, true);
		
		createFormBinding(getFormPanel(), true);
		// Add additional binding for this field since it is added in a FieldAdapter
		// so does not get automatically bound
		addFieldBinding(new ComboBoxFieldBinding(filterTypeCombo, "typeDescriptor"));
	}
	
	public void createActionsGrid() {
		Label actionGridLabel = new Label("Actions Added");
		getFormPanel().add(actionGridLabel, new MarginData(5, 10, 5, 10));

		getFormPanel().add(addActionButton, new MarginData(10));

		ContentPanel container = new ContentPanel();
		container.setHeaderVisible(false);
		container.setLayout(new FitLayout());
		container.setSize("100%", "100%");
		container.setBodyBorder(false);
		container.setBorders(true);

		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");

		ColumnModel cm = new ColumnModel(getColumnConfigs() );

		Grid<BeanModel> actionsGrid = new Grid<BeanModel>(actionsStore, cm);
		actionsGrid.setLoadMask(true);
		actionsGrid.setView(gridView);
		actionsGrid.setBorders( true );
		actionsGrid.setHeight("100%");
		actionsGrid.setStripeRows( true );

		container.add(actionsGrid);
		getFormPanel().add(container, new RowData(1, 1, new Margins(0, 10, 0, 10)));
	}

	/**
	 * Helper.
	 */
	private void createFilterDetails() {
		LayoutContainer main = new LayoutContainer();
		main.setLayout(new ColumnLayout());
		main.setStyleAttribute("paddingTop", "10px");

		LayoutContainer left = new LayoutContainer();
		left.setStyleAttribute("paddingRight", "10px");
		FormLayout layout = new FormLayout();
		layout.setLabelSeparator("");
		layout.setLabelWidth(140);
		left.setLayout(layout);

		LayoutContainer right = new LayoutContainer();
		right.setStyleAttribute("paddingLeft", "10px");
		layout = new FormLayout();
		layout.setLabelSeparator("");
		layout.setLabelWidth(140);
		right.setLayout(layout);

		orgComboBox.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userOrganisation());
		orgComboBox.setAllowBlank(false);
		orgComboBox.setId("orgComboBox");
		orgComboBox.setEnabled(UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_EDIT));
		left.add(orgComboBox);

		channelCombo.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.filterChannel());
		channelCombo.setForceSelection(true);
		channelCombo.setAllowBlank(false);
		channelCombo.setEmptyText("Select channel");
		channelCombo.setDisplayField(Channel.PROP_NAME);
		channelCombo.setId("channelCombo");
		channelCombo.setEnabled(UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_EDIT));
		left.add(channelCombo);

		right.add(filterName);

		filterTypeCombo.setForceSelection(true);
		filterTypeCombo.setAllowBlank(false);
		filterTypeCombo.setEmptyText("Select filter type");
		filterTypeCombo.setDisplayField("label");
		filterTypeCombo.setId("filterTypeCombo");
		filterTypeCombo.setEnabled(UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_EDIT));
		
		LayoutContainer c1 = new LayoutContainer();
		c1.setLayout(new HBoxLayout());

		c1.add(filterTypeCombo, new HBoxLayoutData());
		MyGXTButton configureTypeBtn = new MyGXTButton("configureType","",Resources.INSTANCE.applicationForm(),
				IconAlign.LEFT, ButtonScale.SMALL);
		configureTypeBtn.setToolTip("Configure filter type");
		configureTypeBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						BeanModel selection = filterTypeCombo.getValue();
						if (selection != null){
							Pconfig type = selection.getBean();
							configureFilterType(type);
						}
					}
				});
		c1.add(configureTypeBtn, new HBoxLayoutData(0, 0, 0, 10));
		configureTypeBtn.setEnabled(UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_EDIT));

		AdapterField typeAdapter = new AdapterField(c1);
		typeAdapter.setFieldLabel(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.filterType());
		right.add(typeAdapter);

		right.add(typeInfo);

		addFilterTypeComboListener();

		main.add(left, new ColumnData(.5));
		main.add(right, new ColumnData(.5));
		getFormPanel().add(main, new MarginData(5, 10, 20, 10));
	}

	private void addFilterTypeComboListener() {
		typeComboListener = new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				setDirty(true);
				if (se.getSelectedItem() != null){
					Pconfig type = se.getSelectedItem().getBean();
					configureFilterType(type);
				}
			}
		};
		filterTypeCombo.addSelectionChangedListener(typeComboListener);
	}

	private void updateTypeInfo(Pconfig type){
		String detailString = (type == null) ? "" : getPconfigDetailString(type);
		typeInfo.setText(detailString);
	}

	private List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		ColumnConfig labelConfig = new ColumnConfig(Pconfig.PROP_LABEL, Messages.INSTANCE.filterAction(), 50);
		configs.add(labelConfig);

		AnchorCellRenderer renderer = new AnchorCellRenderer("action");
		renderer.setSelectionListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Pconfig action = ce.getModel().getBean();
				editAction(action);
			}
		});
		labelConfig.setRenderer(renderer);

		ColumnConfig actionDetails = new ColumnConfig( "actionDetails",
				Messages.INSTANCE.filterActionDetails(), 100 );
		actionDetails.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex,
					int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
				Pconfig action = model.getBean();
				String details = getPconfigDetailString(action);
				return details;
			}
		});
		configs.add(actionDetails);

		ColumnConfig actionButtons = new ColumnConfig("actionButtons", "", 40);
		ButtonGridCellRenderer actionButtonsRenderer = new ButtonGridCellRenderer();
		SelectionListener<GridModelEvent> listener = new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel model = ce.getModel();
				actionsStore.remove(model);
				setDirty(true);
			}
		};
		actionButtonsRenderer.addAction( new Action("", "Click to remove this action",
				Resources.INSTANCE.delete(), "remove-action", listener) );
		actionButtons.setRenderer(actionButtonsRenderer);
		configs.add(actionButtons);

		return configs;
	}

	private String getPconfigDetailString(Pconfig action) {
		StringBuffer strDetails = new StringBuffer();
		List<Parameter<?>> parameters = action.getParameters();
		if (parameters == null || parameters.isEmpty()){
			return "";
		}
		for (Parameter<?> param : parameters) {
			if (param instanceof LabelParameter){
				continue;
			}
			Object value = param.getValue();
			if (value == null){
				continue;
			}
			if (param instanceof DateParameter && value != null){
				value = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format((Date)value);
			} else if (param instanceof EntityParameter){
				value = ((EntityParameter) param).getValueLabel();
			}
			strDetails.append("<i>");
			strDetails.append(param.getLabel() );
			strDetails.append("</i> ");
			if (value != null) {
				strDetails.append(value);
			}
			strDetails.append(" ");
		}
		String details = strDetails.toString();
		return details;
	}

	protected void editAction(Pconfig action){
		final PConfigDialog dialog = new PConfigDialog(entityStoreProvider, action, false);
		dialog.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Pconfig pconfig = dialog.getPconfig();
				actionsStore.update(ModelUtil.convertEntityToBeanModel(pconfig));
				dialog.hide();
				setDirty(true);
			}
		});
		dialog.show();
	}

	@Override
	public MyGXTButton getAddActionButton() {
		return addActionButton;
	}

	@Override
	public void addAction(Pconfig actionItem) {
		actionsStore.add(ModelUtil.convertEntityToBeanModel(actionItem));
	}

	@Override
	public void setFormObject(ViewModel<MessageFilterViewModel> vm) {
		filterTypeCombo.removeSelectionListener(typeComboListener);
		super.setFormObject(vm);
		MessageFilterViewModel theViewModel = (MessageFilterViewModel) vm.getModelObject();

		updateTypeInfo(theViewModel.getTypeDescriptor());

		actionsStore.removeAll();
		actionsStore.add(ModelUtil.convertEntityListToBeanList(theViewModel.getActionDescriptors()));

		if (vm.isModeCreate()) {
			titleLabel.setText(Messages.INSTANCE.filterCreateHeader());
		} else {
			titleLabel.setText("Filter: " + theViewModel.getName());
		}
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				filterTypeCombo.addSelectionChangedListener(typeComboListener);
			}
		});
	}

	@Override
	public ViewModel<MessageFilterViewModel> getFormObject() {
		ViewModel<MessageFilterViewModel> formObject = super.getFormObject();
		MessageFilterViewModel vm = formObject.getModelObject();

		List<Pconfig> actionDescriptors = ModelUtil.convertBeanListToEntityList(actionsStore.getModels());
		vm.setActionDescriptors(actionDescriptors);

		formObject.setModelObject(vm);
		return formObject;
	}

	@Override
	public void setOrganizationStore(ListStore<BeanModel> store) {
		orgComboBox.setStore(store);
	}

	@Override
	public void setFilterStore(ListStore<BeanModel> store) {
		filterTypeCombo.setStore(store);
	}

	@Override
	public void setChannelStore(ListStore<BeanModel> store) {
		channelCombo.setStore(store);
	}

	@Override
	public void setEntityStoreProvider(EntityStoreProvider entityStoreProvider) {
		this.entityStoreProvider = entityStoreProvider;
	}
	
	@Override
	public ComboBox<BeanModel> getOrganizationCombo(){
		return orgComboBox;
	}

	/**
	 * @param type
	 */
	private void configureFilterType(Pconfig type) {
		final PConfigDialog dialog = new PConfigDialog(entityStoreProvider, type, false);
		dialog.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Pconfig pconfig = dialog.getPconfig();
				updateTypeInfo(pconfig);
				dialog.hide();
				setDirty(true);
			}
		});
		dialog.show();
	}
}
