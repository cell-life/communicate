package org.celllife.mobilisr.client.admin.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.admin.LostMessagesView;
import org.celllife.mobilisr.client.campaign.view.MessageLogViewImpl;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.SmsLog;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnHeader.Head;
import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;

public class LostMessagesViewImpl extends EntityListTemplateImpl implements
		LostMessagesView {

	CheckBoxSelectionModel<BeanModel> sm;
	private Button reprocessBtn;
	private Button deleteBtn;
	private GridFilters filters;

	@Override
	public void createView() {
		sm = new CheckBoxSelectionModel<BeanModel>();
		reprocessBtn = new MyGXTButton("reprocessBtn",
				Messages.INSTANCE.lostMessagesReprocess(), Resources.INSTANCE.refresh(),
				IconAlign.LEFT, ButtonScale.SMALL);
		deleteBtn = new MyGXTButton("deleteBtn",
				Messages.INSTANCE.delete(), Resources.INSTANCE.delete(),
				IconAlign.LEFT, ButtonScale.SMALL);

		layoutListTemplate(Messages.INSTANCE.lostMessagesHeader(), null, true);
		String msg = "Select (tick) the rows you wish to action" +
				" then select either Re-process or Delete";
		setSubtitleLabel(msg);
	}

	@Override
	public void buildWidgets(ListStore<BeanModel> store,
			RemoteStoreFilterField<BeanModel> filter) {
		List<ColumnConfig> configs = getColumnConfigs();

		renderEntityListGrid(store, filter, configs, null,
				"Search (Sender, Channel/Receiver, Message)");
		getEntityListGrid().setSelectionModel(sm);
		getEntityListGrid().addPlugin(sm);
		getTopToolBar().add(new SeparatorToolItem());
		getTopToolBar().add(new FillToolItem());
		getTopToolBar().add(reprocessBtn);
		reprocessBtn.disable();
		getTopToolBar().add(deleteBtn);
		deleteBtn.disable();

		filters = new GridFilters();
		DateFilter dateFilter = new DateFilter(SmsLog.PROP_DATE_TIME);
		filters.addFilter(dateFilter);

		ListStore<ModelData> statusStore = new ListStore<ModelData>();
		statusStore.add(status(SmsStatus.RX_CHANNEL_FAIL.name()));
		statusStore.add(status(SmsStatus.RX_FILTER_FAIL.name()));
		ListFilter listFilter = new ListFilter(SmsLog.PROP_STATUS, statusStore);
		listFilter.setDisplayProperty("status");
		filters.addFilter(listFilter);
		getEntityListGrid().addPlugin(filters);

		// Adjust column tooltips
		Listener<GridEvent<BeanModel>> listener = new Listener<GridEvent<BeanModel>>() {
			public void handleEvent(GridEvent<BeanModel> e) {
				if (e.getType() == Events.ViewReady) {
					setColumnTooltip(sm.getColumn(), "Select/Deselect All");
				}
			}
		};
		getEntityListGrid().addListener(Events.ViewReady, listener);
	}

	private ModelData status(String status) {
		ModelData model = new BaseModelData();
		model.set("status", status);
		return model;
	}

	private List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add(sm.getColumn());

		ColumnConfig dateConfig = new ColumnConfig(SmsLog.PROP_DATE_TIME, "Date /Time", 50);
		dateConfig.setDateTimeFormat(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss"));
		configs.add(dateConfig);

		configs.add(new EntityIDColumnConfig(SmsLog.PROP_MSISDN, "Sender #", 40, "sender"));
		configs.add(new EntityIDColumnConfig(SmsLog.PROP_CREATEDFOR,
				"Channel / Receiver", 40, "channel_receiver"));
		configs.add(new EntityIDColumnConfig(SmsLog.PROP_MESSAGE, "Message", 200, "message"));

		ColumnConfig statusColumn = new ColumnConfig(SmsLog.PROP_STATUS, "Status", 20);
		statusColumn.setRenderer(MessageLogViewImpl.getStatusRenderer());
		configs.add(statusColumn);

		configs.add(new EntityIDColumnConfig(SmsLog.PROP_ATTEMPTS, "Attempts", 20, "attempts"));

		return configs;
	}

	private void setColumnTooltip(ColumnConfig config, String tooltip) {
		int idx = getEntityListGrid().getColumnModel().indexOf(config);
		Head h = getEntityListGrid().getView().getHeader().getHead(idx);
		if (h != null) {
			h.getElement().setTitle(tooltip);
		}
	}

	@Override
	public CheckBoxSelectionModel<BeanModel> getSelectionModel() {
		return sm;
	}

	@Override
	public Button getDeleteButton() {
		return deleteBtn;
	}

	@Override
	public Button getReprocessButton() {
		return reprocessBtn;
	}
	
	@Override
	public void clearFilters(){
		filters.clearFilters();
	}

}
