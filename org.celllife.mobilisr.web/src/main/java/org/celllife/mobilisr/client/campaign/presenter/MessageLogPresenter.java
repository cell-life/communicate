package org.celllife.mobilisr.client.campaign.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.MessageLogView;
import org.celllife.mobilisr.client.campaign.view.MessageLogViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;
import org.celllife.mobilisr.service.gwt.MessageLogServiceAsync;

import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.BaseStringFilterConfig;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.FilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = MessageLogViewImpl.class)
public class MessageLogPresenter extends MobilisrBasePresenter<MessageLogView, CampaignEventBus> {
	
	@Inject
	private MessageLogServiceAsync logService;
	
	@Inject 
	private ExportServiceAsync exportService;
	private MyGXTPaginatedGridSearch<SmsLog> gridSearch;
	private MobilisrEntity entity;

	private String directionFilter;

	@Override
	public void bindView() {

		gridSearch = new MyGXTPaginatedGridSearch<SmsLog>(SmsLog.PROP_MSISDN
				.concat(",").concat(SmsLog.PROP_MESSAGE),
				Constants.INSTANCE.pageSize()) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<SmsLog>> callback) {
				if (directionFilter != null){
					FilterConfig config = getDirectionFilterConfig();
					
					((FilterPagingLoadConfig) pagingLoadConfig).getFilterConfigs().add(config);
				}
				logService.getMessageLogsForEntity(entity, pagingLoadConfig, callback);
			}
		};

		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());

		getView().getExportButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				exportLogs();
			}
		});
		
		getView().getFilterDirectionCombo().addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
			@Override
			public void selectionChanged(
					SelectionChangedEvent<SimpleComboValue<String>> se) {
				SimpleComboValue<String> item = se.getSelectedItem();
				directionFilter = item.getValue();
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
	}
	
	private FilterConfig getDirectionFilterConfig() {
		FilterConfig config = new BaseStringFilterConfig("string", directionFilter);
		config.setField(SmsLog.PROP_DIR);
		return config;
	}

	public void onShowMessageLog(ViewModel<? extends MobilisrEntity> vm) {
		getEventBus().setRegionRight(this);
		entity = vm.getModelObject();
		directionFilter = (String) vm.getProperty(MessageLogView.FILTER_DIRECTION);
		getView().setFormObject(vm);
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
	
	private void exportLogs() {
		final MessageBox wait = MessageBox.wait("Exporting", "Exporting message logs", null);
		
		List<FilterConfig> filterConfigs = getView().getFilterConfigs();
		if (directionFilter != null) {
			filterConfigs.add(getDirectionFilterConfig());
		}
		
		BaseFilterPagingLoadConfig loadConfig = new BaseFilterPagingLoadConfig();
		loadConfig.setFilterConfigs(filterConfigs);
		
		exportService.exportMessageLogs(entity, loadConfig, new MobilisrAsyncCallback<String>() {
			@Override
			public void onFailure(Throwable error) {
				wait.close();
				super.onFailure(error);
			}

			@Override
			public void onSuccess(String result) {
				wait.close();
				URLUtil.getTextFile(result);
			}
		});
	}	

}
