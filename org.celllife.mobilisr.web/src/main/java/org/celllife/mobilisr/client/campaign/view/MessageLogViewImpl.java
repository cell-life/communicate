package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.campaign.MessageLogView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.Filter;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;


public class MessageLogViewImpl extends EntityListTemplateImpl implements MessageLogView {

	private MyGXTButton exportBtn;
	private GridFilters filters;
	private SimpleComboBox<String> filterDirectionCombo;

	@Override
	public void createView() {
		exportBtn = new MyGXTButton("csvExportMessages", "Export Messages",
				Resources.INSTANCE.csv(), IconAlign.LEFT, ButtonScale.SMALL);
		
		filterDirectionCombo = new SimpleComboBox<String>();
		filterDirectionCombo.setFieldLabel("Filter by message direction:");
		filterDirectionCombo.setWidth(50);
		filterDirectionCombo.setTriggerAction(TriggerAction.ALL);
		filterDirectionCombo.setEmptyText("Filter by direction");
		filterDirectionCombo.setName("direction_filter");
		filterDirectionCombo.add(SmsLog.SMS_DIR_IN);
		filterDirectionCombo.add(SmsLog.SMS_DIR_OUT);
		filterDirectionCombo.setVisible(false);
		filterDirectionCombo.setEditable(false);		

		layoutListTemplate(Messages.INSTANCE.campaignMessageLog(), null, true);
	}

	@Override
	public void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig dateConfig = new ColumnConfig(SmsLog.PROP_DATE_TIME, "Date", 50);
		dateConfig.setDateTimeFormat(DateTimeFormat.getFormat(" yyyy-MM-dd HH:mm:ss"));
		configs.add(dateConfig);

		configs.add(new ColumnConfig(SmsLog.PROP_MSISDN, "Sender", 50));
		configs.add(new ColumnConfig(SmsLog.PROP_MESSAGE, "Message", 200));
		ColumnConfig statusColumn = new ColumnConfig(SmsLog.PROP_STATUS, "Status", 50);
		statusColumn.setRenderer(getStatusRenderer());
		configs.add(statusColumn);

		renderEntityListGrid(store, filter, configs, null, "Search by mobile number or message");
		
		filters = new GridFilters();
		filters.addFilter(new DateFilter(SmsLog.PROP_DATE_TIME));
		filters.addFilter(new StringFilter(SmsLog.PROP_MSISDN));
		filters.addFilter(new StringFilter(SmsLog.PROP_MESSAGE));

		ListStore<ModelData> statusStore = new ListStore<ModelData>();
		statusStore.add(status(SmsStatus.QUEUED_SUCCESS.name()));
		statusStore.add(status(SmsStatus.QUEUE_FAIL.name()));
		statusStore.add(status(SmsStatus.WASP_SUCCESS.name()));
		statusStore.add(status(SmsStatus.WASP_FAIL.name()));
		statusStore.add(status(SmsStatus.TX_SUCCESS.name()));
		statusStore.add(status(SmsStatus.TX_FAIL.name()));
		ListFilter listFilter = new ListFilter(SmsLog.PROP_STATUS, statusStore);
		listFilter.setDisplayProperty("status");
		filters.addFilter(listFilter);
		getEntityListGrid().addPlugin(filters);
		
		configureToolbar();
	}
	
	private ModelData status(String status) {
		ModelData model = new BaseModelData();
		model.set("status", status);
		return model;
	}

	private void configureToolbar() {
		getTopToolBar().add(new FillToolItem());
		getTopToolBar().add(filterDirectionCombo);
		getTopToolBar().add(new SeparatorToolItem());
		getTopToolBar().add(exportBtn);
	}

	@Override
	public void setTitleText(String headingText){
		titleLabel.setText("Message Log for " + headingText);
	}

	@Override
	public Button getNewEntityButton() {
		return null;
	}

	@Override
	public Button getExportButton() {
		return exportBtn;
	}
	
	@Override
	public List<FilterConfig> getFilterConfigs(){
		List<Filter> filterData = filters.getFilterData();
		return filters.buildQuery(filterData);
	}
	
	@Override
	public void clearFilters(){
		filters.clearFilters();
	}

	static public GridCellRenderer<BeanModel> getStatusRenderer() {
		GridCellRenderer<BeanModel> renderer = new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {

				SmsLog smslog = model.getBean();
				SmsStatus status = smslog.getStatus();
				String text = smslog.getStatus().getText();
				if (status.isFailure()){
					String failreason = smslog.getFailreason() == null ?
							"Unknown reason" : smslog.getFailreason();
					text += ": " + failreason ;
				}
				ImageResource image = Resources.INSTANCE.flagRed();
				switch (status){
				case QUEUED_SUCCESS:
					image = Resources.INSTANCE.flagYellow();
					break;
				case WASP_SUCCESS:
					image = Resources.INSTANCE.flagBlue();
					break;
				case TX_SUCCESS:
					image = Resources.INSTANCE.flagGreen();
					break;
				case RX_SUCCESS:
					image = Resources.INSTANCE.flagGreen();
					break;
				case RX_CHANNEL_FAIL:
					image = Resources.INSTANCE.flagRed();
					text = smslog.getStatus() + ": " + smslog.getFailreason();
					break;
				case RX_FILTER_FAIL:
					image = Resources.INSTANCE.flagYellow();
					text = smslog.getStatus() + ": " + smslog.getFailreason();
					break;
				default:
					image = Resources.INSTANCE.flagRed();
				}

				Image img = new Image(image);
				img.setTitle(text);
				img.setAltText(text);
				img.getElement().setId("status-" + smslog.getId());
				return img;
			}
		};
		return renderer;
	}
	
	@Override
	public void setFormObject(ViewModel<? extends MobilisrEntity> vm) {
		setTitleText(vm.getModelObject().toString());
		clearFilters();
		filterDirectionCombo.setVisible(vm.getPropertyBoolean(SHOW_DIRECTION_FILTER));
		String directionFilter = (String) vm.getProperty(FILTER_DIRECTION);
		if (directionFilter != null){
			filterDirectionCombo.setSimpleValue(directionFilter);
		}
	}
	
	@Override
	public SimpleComboBox<String> getFilterDirectionCombo() {
		return filterDirectionCombo;
	}

}
