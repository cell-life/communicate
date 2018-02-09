package org.celllife.mobilisr.client.admin.presenter;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.LostMessagesView;
import org.celllife.mobilisr.client.admin.view.LostMessagesViewImpl;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.gwt.MessageLogServiceAsync;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseBooleanFilterConfig;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.FilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=LostMessagesViewImpl.class)
public class LostMessagesPresenter extends MobilisrBasePresenter<LostMessagesView, AdminEventBus> {
	private static final int PAGE_SIZE = 75;

	private MyGXTPaginatedGridSearch<SmsLog> gridSearch;

	@Inject
	private MessageLogServiceAsync logService;


	@Override
	public void bindView() {

		gridSearch = new MyGXTPaginatedGridSearch<SmsLog>(SmsLog.PROP_MSISDN
				+ "," + SmsLog.PROP_CREATEDFOR + "," + SmsLog.PROP_MESSAGE,
				PAGE_SIZE) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig,
					AsyncCallback<PagingLoadResult<SmsLog>> callback) {
				SmsStatus[] statuses = new SmsStatus[2];
				statuses[0] = SmsStatus.RX_CHANNEL_FAIL;
				statuses[1] = SmsStatus.RX_FILTER_FAIL;
				FilterConfig config = new BaseBooleanFilterConfig();
				config.setType("boolean");
				config.setValue(false);
				config.setField(SmsLog.PROP_VOIDED);
				((FilterPagingLoadConfig)pagingLoadConfig).getFilterConfigs().add(config);
				if (pagingLoadConfig.getSortField() == null || pagingLoadConfig.getSortField().trim().equals("")) {
					pagingLoadConfig.setSortField(SmsLog.PROP_DATE_TIME);
					pagingLoadConfig.setSortDir(SortDir.DESC);
				}
				logService.getMessageLogsByStatus(statuses , pagingLoadConfig, callback);
			}
		};

		getView().getPagingToolBar().setPageSize(PAGE_SIZE);
		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidgets(gridSearch.getStore(), gridSearch.getFilter());

		addListeners();
	}

	/**
	 * Helper.
	 */
	private void addListeners() {
		// Re-process button
		getView().getReprocessButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				List<BeanModel> items = getView().getSelectionModel().getSelectedItems();
				final List<Long> logEntryIds = new ArrayList<Long>();
				for (BeanModel item : items) {
					SmsLog logEntry = item.getBean();
					logEntryIds.add(logEntry.getId());
				}
				logService.reprocessMessages(logEntryIds, new MobilisrAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void v) {
						String msg = "Messages queued for re-processing: " + logEntryIds.size();
						getView().displaySuccessMsg(msg);
						gridSearch.getLoader().load(gridSearch.getLoader().getOffset(), PAGE_SIZE);
					}
				});
			}
		});

		// Delete button
		getView().getDeleteButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				List<BeanModel> items = getView().getSelectionModel().getSelectedItems();
				final List<Long> logEntryIds = new ArrayList<Long>();
				for (BeanModel item : items) {
					SmsLog logEntry = item.getBean();
					logEntryIds.add(logEntry.getId());
				}

				String strText = "Are you sure you want to delete " + logEntryIds.size()
				+ " messages?";
				Listener<MessageBoxEvent> confirmCallback = new Listener<MessageBoxEvent>() {
					@Override
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
							logService.voidMessages(logEntryIds, new MobilisrAsyncCallback<Void>() {
								@Override
								public void onSuccess(Void v) {
									String msg = "Messages voided: " + logEntryIds.size();
									getView().displaySuccessMsg(msg);
									gridSearch.getLoader().load(
											gridSearch.getLoader().getOffset(), PAGE_SIZE);
								}
							});
						}
					}
				};
				MessageBoxWithIds.confirm("Delete Messages", strText, confirmCallback);
			}
		});

		// Selection listener to disable buttons when nothing selected.
		getView().getSelectionModel().addListener(Events.SelectionChange,
			new Listener<SelectionChangedEvent<BeanModel>>() {
				@Override
				public void handleEvent(SelectionChangedEvent<BeanModel> be) {
					List<BeanModel> items = getView().getSelectionModel().getSelectedItems();
					if (items.size() > 0) {
						getView().getDeleteButton().enable();
						getView().getReprocessButton().enable();
					}
					else {
						getView().getDeleteButton().disable();
						getView().getReprocessButton().disable();
					}
				}
			});
	}

	public void onShowLostMessagesView() {
		getEventBus().setRegionRight(this);
		getView().clearFilters();
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, PAGE_SIZE);
		getView().clearSuccessMsg();
	}


}
