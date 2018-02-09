package org.celllife.mobilisr.client.role.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.role.AdminRoleCreateView;
import org.celllife.mobilisr.client.role.AdminRoleListView;
import org.celllife.mobilisr.client.role.AdminRoleUI;
import org.celllife.mobilisr.client.role.view.AdminRoleUIImpl;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.RoleServiceAsync;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = AdminRoleUIImpl.class)
public class AdminRoleUIPresenter extends DirtyPresenter<AdminRoleUI,AdminEventBus> {

	private AdminRoleListView adminRoleListView;
	private AdminRoleCreateView adminRoleCreateView;
	
	@Inject
	private RoleServiceAsync roleServiceAsync;

	private BeanModel selectedRole;

	ListLoader<ListLoadResult<Role>> loader;


	@Override
	public void bindView() {
		adminRoleListView = getView().getAdminRoleListView();
		adminRoleCreateView = getView().getAdminRoleCreateView();

		RpcProxy<List<Role>> proxy = new RpcProxy<List<Role>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Role>> callback) {
				roleServiceAsync.listAllRoles(callback);
			}
		};

		StoreFilterField<BeanModel> filter = new StoreFilterField<BeanModel>() {

			@Override
			protected boolean doSelect(Store<BeanModel> store,
					BeanModel parent, BeanModel record, String property,
					String filter) {

				String name = parent.get("name");
				name = name.toLowerCase();
				if (name.indexOf(filter.toLowerCase()) != -1) {
					return true;
				}
				return false;

			}
		};

		loader = new BaseListLoader<ListLoadResult<Role>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		adminRoleListView.buildWidget(store, filter);

		adminRoleListView.getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {

					@Override
					public void handleEvent(GridEvent<BeanModel> gridEvent) {
						BeanModel beanModel = gridEvent.getModel();
						Role role = beanModel.getBean();
						selectedRole = beanModel;

						ViewModel<Role> viewEntityModel = new ViewModel<Role>();
						viewEntityModel.setModelObject(role);

						adminRoleListView.clearSuccessMsg();

						adminRoleCreateView.setFormObject(viewEntityModel);

						adminRoleListView.getDeleteRoleButton().enable();
					}

				});

		adminRoleListView.getNewRoleButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						adminRoleListView.clearSuccessMsg();
						adminRoleCreateView.setFormObject(new ViewModel<Role>(new Role()));
						adminRoleListView.getDeleteRoleButton().disable();
					}
				});

		adminRoleListView.getDeleteRoleButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (selectedRole == null){
					MessageBoxWithIds.alert("Error: No role selected",
							"Select a role before clicking delete.",
							null);
				}else{

					roleServiceAsync.listOfUsersForRole((Role) selectedRole.getBean(), new MobilisrAsyncCallback<List<User>>(){
						@Override
						public void onSuccess(List<User> userList) {
							if (userList.size()>0){
								MessageBoxWithIds.confirm(
									"Warning: Deleting role",
									"There are users who have this role assigned to them." +
									" Are you sure you want to delete this role?",
									new Listener<MessageBoxEvent>() {
										public void handleEvent(MessageBoxEvent ce) {
											if (ce.getButtonClicked().getItemId()
													.equals(Dialog.YES)) {
												deleteRole();
											}else{
												return;
											}
										}
								});
							}else{
								deleteRole();
							}
						}});
				}
			}
		});

		adminRoleCreateView.getFormSubmitButton().addSelectionListener(	new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {

				ViewModel<Role> viewEntityModel = (ViewModel<Role>) adminRoleCreateView.getFormObject();
				Role role = (Role) viewEntityModel.getModelObject();
				roleServiceAsync.saveOrUpdateRole(role, new MobilisrAsyncCallback<Role>() {
					@Override
					public void onSuccess(Role role) {
						getView().setDirty(false);
						adminRoleListView.displaySuccessMsg("Role: \'" +  role.getName() + "\' saved successfully");
						adminRoleCreateView.setFormObject(new ViewModel<Role>(new Role()));
						loader.load();
					}

					@Override
					public void onFailure(Throwable cause) {
						adminRoleCreateView.setErrorMessage(cause.getMessage());
					}
				});
			}

		});

		adminRoleCreateView.getFormCancelButton().addSelectionListener( new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().setDirty(false);
				// Clear the sucess msg if it is displayed
				adminRoleListView.clearSuccessMsg();
				// Clear the form
				adminRoleCreateView.setFormObject(new ViewModel<Role>(new Role()));
			}
		});

	}

	private void deleteRole() {
		roleServiceAsync.deleteRole((Role)selectedRole.getBean(), new MobilisrAsyncCallback<Void>(){
			@Override
			public void onSuccess(Void arg0) {
				getView().setDirty(false);
				adminRoleCreateView.setFormObject(new ViewModel<Role>(new Role()));
				adminRoleListView.getEntityListGrid().getStore().remove(selectedRole);
			}

		});
	}

	public void onShowRoleView() {
		if (!UserContext.hasPermission(MobilisrPermission.MANAGE_ROLES)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.MANAGE_ROLES
							.name()));
			return;
		}
		getEventBus().setNavigationConfirmation(this);
		eventBus.setRegionRight(this);
		adminRoleListView.clearSuccessMsg();
		adminRoleCreateView.setFormObject(new ViewModel<Role>(new Role()));
		loader.load();
	}

}
