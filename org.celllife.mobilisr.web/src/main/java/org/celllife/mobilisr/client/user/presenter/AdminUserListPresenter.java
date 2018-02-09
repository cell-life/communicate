package org.celllife.mobilisr.client.user.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.user.AdminUserListView;
import org.celllife.mobilisr.client.user.view.AdminUserListViewImpl;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.UserServiceAsync;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter( view=AdminUserListViewImpl.class)
public class AdminUserListPresenter extends MobilisrBasePresenter<AdminUserListView, AdminEventBus> {

	@Inject
	private UserServiceAsync userService;
	
	@Inject
	private OrganizationServiceAsync orgService;
	
	private MyGXTPaginatedGridSearch<User> gridSearch;

	protected boolean showVoided;
	protected Organization filterOrg;
	
	@Override
	public void bindView() {
		showVoided = false;
		
		gridSearch = new MyGXTPaginatedGridSearch<User>(User.PROP_FIRST_NAME + ","
				+ User.PROP_LAST_NAME + "," + User.PROP_USERNAME, Constants.INSTANCE.pageSize()) {
			
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<User>> callback) {
				userService.listAllUsers(filterOrg, pagingLoadConfig, showVoided, callback);
			}

			@Override
			protected ListStore<BeanModel> createStore(PagingLoader<PagingLoadResult<ModelData>> loader) {
				GroupingStore<BeanModel> store = new GroupingStore<BeanModel>(loader);
				store.groupBy(User.PROP_ORGANIZATION); 
				return store;
			}
		};

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
		
		getView().getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {

			@Override
			public void handleEvent(GridEvent<BeanModel> gridEvent) {
				BeanModel beanModel = gridEvent.getModel();
				User user =  beanModel.getBean();
				userService.getUser(user.getId(), new MobilisrAsyncCallback<User>() {
					@Override
					public void onSuccess(User user) {
						ViewModel<User> viewEntityModel = new ViewModel<User>(user);
						getEventBus().showUserCreate(viewEntityModel);
					}
				});				
			}
		});
		
		getView().getNewEntityButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				ViewModel<User> viewEntityModel = new ViewModel<User>(new User());
				getEventBus().showUserCreate(viewEntityModel);
			}
		});
		
		getView().getToggleVoidAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel model = ce.getModel();
				final User user = model.getBean();
				if (user.equals(UserContext.getUser())){
					MessageBoxWithIds.info("Error: Deactivating yourself!",
							"You are not allowed to deactivate your own account.", null);
					return;
				}

				if (user.getId()==1l){
					MessageBoxWithIds.info("Error: Deactivating Administrator!",
							"You cannot deactivate the administrator user!", null);
					return;
				}

				if (user.isVoided()){
					toggleVoidAndRefreshList(user);
					return;
				}

				String strText = "Are you sure you want to deactivate user '" + user.getUserName()
					+ "'? \n\nNote that you can reactivate this user later by selecting 'Inactive'"
					+ " in the filter above the table, and then pressing 'Reactivate User'.";
				MessageBoxWithIds.confirm("Warning: Deleting a user", strText,
					new Listener<MessageBoxEvent>() {
						@Override
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getItemId()
									.equals(Dialog.YES)) {
								toggleVoidAndRefreshList(user);
							}
						}
				});
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
				BeanModel selectedItem = se.getSelectedItem();
				if (selectedItem == null){
					filterOrg = null;
				} else {
					filterOrg = selectedItem.getBean();
				}
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
	}
	
	public void onShowUserList(ViewModel<User> vem){
		if (!UserContext.hasPermission(MobilisrPermission.MANAGE_USERS)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.MANAGE_USERS
							.name()));
			return;
		}
		if (vem != null && vem.getModelObject() != null){
			User user = (User) vem.getModelObject();
			getView().displaySuccessMsg("User: \'" +  user.getFirstName() + " " + user.getLastName() + "\' saved successfully");
		}
		getEventBus().setRegionRight(this);
		getView().setFormObject(null);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}

	private void toggleVoidAndRefreshList(User user) {
		user.setVoided(!user.getVoided());
		userService.saveUser(user, user.getRoles(), new MobilisrAsyncCallback<User>() {
			@Override
			public void onSuccess(User arg0) {
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
				
			}
		});
	}

}
