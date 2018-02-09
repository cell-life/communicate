package org.celllife.mobilisr.client.filter.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.presenter.RequestNewDialogue;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.filter.FilterEventBus;
import org.celllife.mobilisr.client.filter.FilterListView;
import org.celllife.mobilisr.client.filter.view.FilterListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.MessageFilterServiceAsync;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=FilterListViewImpl.class)
public class FilterListPresenter extends MobilisrBasePresenter<FilterListView, FilterEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private MessageFilterServiceAsync service;
	
	@Inject
	private OrganizationServiceAsync orgService;

	protected Organization filterOrg;
	
	private MyGXTPaginatedGridSearch<MessageFilter> gridSearch;
	
	protected boolean showVoided;

	@Override
	public void bindView() {
		showVoided = false;

		// Bind create filter button
		getView().getNewEntityButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {

				if (isAdminView()) {
					if (UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_CREATE)) {
						editFilter(null);
					} else {
						MessageBoxWithIds.alert("Access Denied", "You do not have permission" + " to create new filters.", null);
					}
				} else {
					final RequestNewDialogue dialogue = new RequestNewDialogue("Filter");
					dialogue.setListener(new Listener<BaseEvent>() {
						@Override
						public void handleEvent(BaseEvent be) {

							dialogue.hide();
							BusyIndicator.showBusyIndicator("Your request is being emailed.");
							adminService.sendNewUserRequest(dialogue.getUser(), "Filter request", dialogue.getRequestText(), new MobilisrAsyncCallback<Void>() {
								@Override
								public void onSuccess(Void arg0) {
									BusyIndicator.hideBusyIndicator();
									MessageBoxWithIds.info("Request sent",
											"Thank you for sending a message to request a new\n"
													+ "filter. A consultant will be in contact with you\n"
													+ "shortly with regards to your request.\n",
											null);
								}
							});
						}
					});
				}
			}
		});

		gridSearch = new MyGXTPaginatedGridSearch<MessageFilter>(
				MessageFilter.PROP_NAME, Constants.INSTANCE.pageSize()) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig config,
					AsyncCallback<PagingLoadResult<MessageFilter>> callback) {
				Organization org = UserContext.getUser().getOrganization();
				if (isAdminView() && UserContext.hasPermission(MobilisrPermission.FILTERS_ADMIN_VIEW)){
					org = filterOrg;
				}
				service.listMessageFilters(org, showVoided, config, callback);
			}
		};

		gridSearch.getStore().setDefaultSort(MessageFilter.PROP_NAME, SortDir.ASC);

		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());
		
		// org filter combo
		RpcProxy<PagingLoadResult<Organization>> orgProxy = new RpcProxy<PagingLoadResult<Organization>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<Organization>> callback) {
				PagingLoadConfig config = (PagingLoadConfig) loadConfig;
				config.set(RemoteStoreFilterField.PARM_FIELDS, Organization.PROP_NAME);
				orgService.listAllOrganizations(config, showVoided, callback);
			}
		};
		
		ListLoader<PagingLoadResult<Organization>> orgLoader = new BasePagingLoader<PagingLoadResult<Organization>>(orgProxy, new BeanModelReader());
		getView().setOrganizationStore(new ListStore<BeanModel>(orgLoader));
		
		getView().getToggleActiveStateAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final MessageFilter messageFilter = ce.getModel().getBean();
				String action = messageFilter.isActive() ? "Deactivate" : "Activate";
				MessageBoxWithIds.confirm(action + " " + messageFilter.getName()
						+ " filter", "Are you sure you want to " + action + " this filter?",
						new Listener<MessageBoxEvent>() {
							@Override
							public void handleEvent(MessageBoxEvent be) {
								if (be.getButtonClicked().getItemId()
										.equals(Dialog.YES)) {
									toggleActiveStatus(messageFilter);
								}
							}
						});
			}
		});
		
		getView().getToggleVoidStateAction().setListener(new SelectionListener<GridModelEvent>() {

			@Override
			public void componentSelected(GridModelEvent ce) {
				final MessageFilter messageFilter = ce.getModel().getBean();
				if (!messageFilter.isVoided() && messageFilter.isActive()){
					MessageBoxWithIds.alert("Filter is active",
						"Please deactivate the filter before deleting it.",
						null);
				} else {
					String action = messageFilter.isVoided() ? "Undelete" : "Delete";
					MessageBoxWithIds.confirm(action + " " + messageFilter.getName()
							+ " filter", "Are you sure you want to " + action + " this filter?",
							new Listener<MessageBoxEvent>() {
								@Override
								public void handleEvent(MessageBoxEvent be) {
									if (be.getButtonClicked().getItemId()
											.equals(Dialog.YES)) {
										toggleVoidStatus(messageFilter);
									}
								}
							});
				}
			}
		});
		
		getView().getViewFilterInboxAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				MessageFilter messageFilter = ce.getModel().getBean();
				showInbox(messageFilter);
			}
		});
		
		getView().getFilterNameAnchor().setSelectionListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				editFilter((MessageFilter) ce.getModel().getBean());
			}
		});
		
		getView().getShowVoidedButton().addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				showVoided = !showVoided;
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
		
		getView().getFilterOrgCombo().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				if (isAdminView()){
					BeanModel selectedItem = se.getSelectedItem();
					if (selectedItem == null){
						filterOrg = null;
					} else {
						filterOrg = selectedItem.getBean();
					}
					gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
				}
			}
		});
	}

	public void onShowFilterListView(ViewModel<?> viewEntityModel) {
		isAdminView(viewEntityModel);;
		getEventBus().setRegionRight(this);
		
		filterOrg = null;
		getView().clearFilterOrgCombo();
		
		getView().setFormObject(viewEntityModel);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}

	private void toggleActiveStatus(final MessageFilter messageFilter) {
		messageFilter.setActive(!messageFilter.isActive());
		service.saveMessageFilter(messageFilter, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void arg0) {
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
			
			@Override
			protected void handleExpectedException(Throwable error) {
				messageFilter.setActive(!messageFilter.isActive());
				super.handleExpectedException(error);
			}
		});
	}
	
	private void toggleVoidStatus(final MessageFilter messageFilter) {
		messageFilter.setVoided(!messageFilter.isVoided());
		service.saveMessageFilter(messageFilter, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void arg0) {
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
			
			@Override
			protected void handleExpectedException(Throwable error) {
				messageFilter.setVoided(!messageFilter.isVoided());
				super.handleExpectedException(error);
			}
		});
	}

	private void editFilter(MessageFilter filter) {
		if (filter == null || !filter.isPersisted()){
			ViewModel<MessageFilterViewModel> vm = new ViewModel<MessageFilterViewModel>(new MessageFilterViewModel(new MessageFilter()));
			vm.putProperty(ADMIN_VIEW, isAdminView());
			getEventBus().showFilterCreateView(vm);
		} else {
			service.getMessageFilterViewModel(filter.getId(), new MobilisrAsyncCallback<MessageFilterViewModel>() {
				@Override
				public void onSuccess(MessageFilterViewModel viewModel) {
					ViewModel<MessageFilterViewModel> vm = new ViewModel<MessageFilterViewModel>(viewModel);
					vm.putProperty(ADMIN_VIEW, isAdminView());
					getEventBus().showFilterCreateView(vm);
				}
			});
		}
	}

	private void showInbox(MessageFilter messageFilter) {
		ViewModel<MobilisrEntity> vem = new ViewModel<MobilisrEntity>(messageFilter);
		vem.putProperty("filterDirection", SmsLog.SMS_DIR_IN);
		vem.putProperty("showDirectionFilter", true);
		getEventBus().showMessageLog(vem);
	}

}
