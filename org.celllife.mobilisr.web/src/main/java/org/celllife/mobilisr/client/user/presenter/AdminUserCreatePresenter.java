package org.celllife.mobilisr.client.user.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.user.AdminUserCreateView;
import org.celllife.mobilisr.client.user.view.AdminUserCreateViewImpl;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.RoleServiceAsync;
import org.celllife.mobilisr.service.gwt.UserServiceAsync;

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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = AdminUserCreateViewImpl.class)
public class AdminUserCreatePresenter extends DirtyPresenter<AdminUserCreateView,AdminEventBus> {

	@Inject
	private OrganizationServiceAsync orgService;

	@Inject
	private UserServiceAsync userService;
	
	@Inject
	private RoleServiceAsync roleServiceAsync;
	
	private ListLoader<ListLoadResult<Role>> roleLoader;

	private User user;
	
	@Override
	public void bindView() {
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

		RpcProxy<List<Role>> roleProxy = new RpcProxy<List<Role>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Role>> callback) {
				roleServiceAsync.listAllRoles(callback);
			}
		};
		
		roleLoader = new BaseListLoader<ListLoadResult<Role>>(roleProxy,new BeanModelReader());
		ListStore<BeanModel> roleStore = new ListStore<BeanModel>(roleLoader);
		getView().setRolesStore(roleStore);
		
		getView().getFormSubmitButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				submitForm();
			}
		});
		
		getView().getFormCancelButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				eventBus.showUserList(null);
			}
		});
		
		getView().getCreateKeyButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				createNewApiKey();
			}
		});
	}

	protected void createNewApiKey() {
		userService.createApiKey(user, new MobilisrAsyncCallback<ApiKey>() {
			@Override
			public void onSuccess(ApiKey result) {
				getView().addApiKey(result);
			}
		});
	}

	private void submitForm() {
		final ViewModel<User> viewModel = getView().getFormObject();
		final User user = (User)viewModel.getModelObject();
		List<Role> selectedRoles = getView().getSelectedRoles();
		userService.saveUser(user, selectedRoles, new MobilisrAsyncCallback<User>() {
			@Override
			protected void handleExpectedException(Throwable error) {
				viewModel.setViewMessage(error.getMessage());
				onShowUserCreate(viewModel);
			}

			@Override
			public void onSuccess(User user) {
				getView().setDirty(false);
				getEventBus().showUserList(new ViewModel<User>(user));
			}
		});		
		
	}
	
	public void onShowUserCreate(ViewModel<User> vm) {
		if (!UserContext.hasPermission(MobilisrPermission.MANAGE_USERS)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.MANAGE_USERS
							.name()));
			return;
		}
		
		user = vm.getModelObject();
		getEventBus().setNavigationConfirmation(this);
		getEventBus().setRegionRight(this);
		getView().setFormObject(vm);
		roleLoader.load();
	}
	
}
