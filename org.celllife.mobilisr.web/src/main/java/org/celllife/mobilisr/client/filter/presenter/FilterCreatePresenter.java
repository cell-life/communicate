package org.celllife.mobilisr.client.filter.presenter;

import java.util.Collection;
import java.util.List;

import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.filter.FilterCreateView;
import org.celllife.mobilisr.client.filter.FilterEventBus;
import org.celllife.mobilisr.client.filter.view.FilterCreateViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.EntityStoreProvider;
import org.celllife.mobilisr.client.reporting.EntityStoreProviderImpl;
import org.celllife.mobilisr.client.reporting.PConfigDialog;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.ChannelServiceAsync;
import org.celllife.mobilisr.service.gwt.MessageFilterServiceAsync;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.Style.ButtonArrowAlign;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=FilterCreateViewImpl.class)
public class FilterCreatePresenter extends DirtyPresenter<FilterCreateView,FilterEventBus> {
	
	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private OrganizationServiceAsync orgService;
	
	@Inject
	private MessageFilterServiceAsync msgFilterService;
	
	@Inject
	private ChannelServiceAsync channelService;

	private ListLoader<ListLoadResult<Pconfig>> msgFilterLoader;
	private ListLoader<ListLoadResult<Channel>> channelLoader;
	private EntityStoreProvider entityStoreProvider;

	@Override
	public void bindView() {
		// Organisation service/combo
		RpcProxy<PagingLoadResult<Organization>> orgProxy = new RpcProxy<PagingLoadResult<Organization>>() {

			@Override
			protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<Organization>> callback) {
				PagingLoadConfig config = (PagingLoadConfig) loadConfig;
				config.set(RemoteStoreFilterField.PARM_FIELDS, Organization.PROP_NAME);
				orgService.listAllOrganizations(config, false, callback);
			}
		};

		final ListLoader<PagingLoadResult<Organization>> orgLoader = new BasePagingLoader<PagingLoadResult<Organization>>(
				orgProxy, new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(orgLoader);
		getView().setOrganizationStore(store);

		// MessageFilterType service/combo
		RpcProxy<Collection<Pconfig>> filterProxy = new RpcProxy<Collection<Pconfig>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<Collection<Pconfig>> callback) {
				msgFilterService.listAllFilterTypes(callback);
			}
		};
		msgFilterLoader = new BaseListLoader<ListLoadResult<Pconfig>>(filterProxy,
				new BeanModelReader() );
		ListStore<BeanModel> filterTypeStore = new ListStore<BeanModel>(msgFilterLoader);
		getView().setFilterStore(filterTypeStore);

		// Channel service/combo
		RpcProxy<List<Channel>> channelProxy = new RpcProxy<List<Channel>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Channel>> callback) {
				channelService.listIncomingChannels(callback);
			}
		};
		channelLoader = new BaseListLoader<ListLoadResult<Channel>>(channelProxy,
				new BeanModelReader() );
		ListStore<BeanModel> channelStore = new ListStore<BeanModel>(channelLoader);
		getView().setChannelStore(channelStore);

		configureAddActionButton();

		getView().getFormSubmitButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				submitForm();
			}
		});

		getView().getFormCancelButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				ViewModel<MessageFilterViewModel> viewEntityModel = new ViewModel<MessageFilterViewModel>(null);
				viewEntityModel.putProperty(ADMIN_VIEW, isAdminView());
				eventBus.showFilterListView(viewEntityModel);
			}
		});
		
		getView().getOrganizationCombo().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				if (se.getSelectedItem() != null){
					Organization org = se.getSelectedItem().getBean();
					entityStoreProvider.restrictResultsToOrganization(org);
				} else {
					entityStoreProvider.restrictResultsToOrganization(null);
				}
			}
		});
		
		entityStoreProvider = new EntityStoreProviderImpl(adminService);
		getView().setEntityStoreProvider(entityStoreProvider);
	}

	/**
	 * Helper.
	 */
	private void configureAddActionButton() {
		// Populate the list of all actions
		MobilisrAsyncCallback<Collection<Pconfig>> callback =
				new MobilisrAsyncCallback<Collection<Pconfig>>() {
			@Override
			public void onSuccess(Collection<Pconfig> allActionsList) {
				createActionsMenu(allActionsList);
			}
		};
		msgFilterService.listAllActions(callback);
	}

	/**
	 * @param allActionsList
	 */
	private void createActionsMenu(Collection<Pconfig> allActionsList) {
		// For each action, create a menu-item and listener, and add to the menu
		Menu menu = new Menu();
		for (final Pconfig action : allActionsList) {
			MenuItem tmpMenuItem = new MenuItem(action.getLabel());
			tmpMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					if (!entityStoreProvider.isRestrctedToOrganization()){
						MessageBoxWithIds.alert("No organisation selected", 
								"Please select an organisation before adding actions.", null);
						return;
					}
					final PConfigDialog dialog = new PConfigDialog(entityStoreProvider, action, false);
					dialog.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							getView().addAction(dialog.getPconfig());
							getView().setDirty(true);
							dialog.hide();
						}
					});
					dialog.show();
				}
			});
			menu.add(tmpMenuItem);
		}

		getView().getAddActionButton().setMenu(menu);
		getView().getAddActionButton().setArrowAlign(ButtonArrowAlign.RIGHT);
	}

	private void submitForm() {
		ViewModel<MessageFilterViewModel> formObject = getView().getFormObject();
		final MessageFilterViewModel vm = formObject.getModelObject();
		msgFilterService.saveMessageFilter(vm, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void arg0) {
				getView().setDirty(false);
				ViewModel<MessageFilterViewModel> vem = new ViewModel<MessageFilterViewModel>(null);
				vem.putProperty(ADMIN_VIEW, isAdminView());
				vem.setViewMessage("Successfully saved filter: " + vm.getName());
				getEventBus().showFilterListView(vem);
			}
		});
	}

	public void onShowFilterCreateView(ViewModel<MessageFilterViewModel> vem) {
		getEventBus().setNavigationConfirmation(this);
		isAdminView(vem);
		eventBus.setRegionRight(this);
		getView().setFormObject(vem);
	}

}
