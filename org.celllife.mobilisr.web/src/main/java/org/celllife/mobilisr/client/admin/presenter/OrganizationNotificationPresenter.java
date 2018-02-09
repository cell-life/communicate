package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.view.OrganizationNotificationView;
import org.celllife.mobilisr.client.admin.view.OrganizationNotificationViewImpl;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.PConfigDialog;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=OrganizationNotificationViewImpl.class)
public class OrganizationNotificationPresenter extends DirtyPresenter<OrganizationNotificationView,AdminEventBus> {
	
	@Inject
	private OrganizationServiceAsync service;
	
	@Inject
	private AdminServiceAsync adminService;
	
	private BaseListLoader<ListLoadResult<Organization>> loader;

	protected boolean isLoaded = false;

	@Override
	public void bindView() {
		RpcProxy<List<Organization>> proxy = new RpcProxy<List<Organization>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Organization>> callback) {
				service.listAllOrganizations(false, callback);
			}
		};

		loader = new BaseListLoader<ListLoadResult<Organization>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		getView().setOrganisationStore(store);
		
		getView().getFormSubmitButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (getView().checkForm()) {
					submitForm();
				} else {
					MessageBoxWithIds.alert("Incomplete form", 
							"Please complete all the fields in the form.", null);
				}
			}
		});
		
		getView().getFormCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().setDirty(false);
				getEventBus().showOrgList(null);
			}
		});
		
		getView().addFieldSetListener(new Listener<BaseEvent>(){
			@Override
			public void handleEvent(BaseEvent be) {
				if (!isLoaded){
					isLoaded = true;
					loader.load();
				}
			}
		});
		
		getView().getTestButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (getView().checkForm()) {
					sendTestEmail();
				} else {
					MessageBoxWithIds.alert("Incomplete form", 
							"Please complete all the fields in the form.", null);
				}
			}
		});
	}

	protected void sendTestEmail() {
		Pconfig config = new Pconfig(null, "Test notification email");
		final StringParameter mailto = new StringParameter("Email","Email to:");
		mailto.setTooltip("Email addresses to send test message to");
		mailto.setRegex("([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}");
		mailto.setErrorMessage("Invalid email addresses");
		mailto.setDefaultValue(UserContext.getUser().getEmailAddress());
		config.addParameter(mailto);

		final PConfigDialog dialog = new PConfigDialog(null, config, false);
		dialog.getSaveButton().setText(Messages.INSTANCE.sendTestMessage());
		dialog.getSaveButton().addSelectionListener(
			new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					BusyIndicator.showBusyIndicator(Messages.INSTANCE.sending());
					ViewModel<OrganisationNotificationViewModel> vm = getView().getFormObject();
					OrganisationNotificationViewModel model = vm.getModelObject();
					model.setTestEmail(mailto.getValue());
					adminService.sendNewOrganizationNotification(model, new MobilisrAsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							BusyIndicator.hideBusyIndicator();
						}
					});
					dialog.hide();
				}
			});
		dialog.show();
	}

	protected void submitForm() {
		final ViewModel<OrganisationNotificationViewModel> formObject = getView().getFormObject();
		OrganisationNotificationViewModel ovm = formObject.getModelObject();
		adminService.sendNewOrganizationNotification(ovm, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				getView().setDirty(false);
				getEventBus().showOrgList(null);
			}
		});
	}

	public void onShowOrganizationNotificationView() {
		if (!UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_SEND_NOTIFICATIONS)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.ORGANISATIONS_SEND_NOTIFICATIONS
							.name()));
			return;
		}
		getEventBus().setNavigationConfirmation(this);
		eventBus.setRegionRight(this);
		getView().setFormObject(new ViewModel<OrganisationNotificationViewModel>(new OrganisationNotificationViewModel()));
		isLoaded = false;
	}

}
