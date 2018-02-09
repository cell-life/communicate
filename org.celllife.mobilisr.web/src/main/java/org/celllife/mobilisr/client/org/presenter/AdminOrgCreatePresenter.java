package org.celllife.mobilisr.client.org.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.org.AdminOrgCreateView;
import org.celllife.mobilisr.client.org.view.AdminOrgCreateViewImpl;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter( view=AdminOrgCreateViewImpl.class)
public class AdminOrgCreatePresenter extends DirtyPresenter<AdminOrgCreateView,AdminEventBus> {
	@Inject
	private OrganizationServiceAsync service = null;
	
	@Override
	public void bindView() {
		
		getView().getFormCancelButton().addListener(Events.Select, new Listener<ButtonEvent>(){
			@Override
			public void handleEvent(ButtonEvent be) {
				getEventBus().showOrgList(null);
			}
		});
		
		getView().getFormSubmitButton().addListener(Events.Select, new Listener<ButtonEvent>(){
			@Override
			public void handleEvent(ButtonEvent be) {
				submitForm();
			}
		});
	}
	
	private void submitForm() {
		final ViewModel<Organization> viewModel = getView().getFormObject();
		
		if (viewModel.isDirty()){
			Organization org = (Organization) viewModel.getModelObject();
			service.saveOrUpdateOrganisation(org, new MobilisrAsyncCallback<Organization>() {
				@Override
				public void handleExpectedException(Throwable cause) {
					viewModel.setViewMessage(cause.getMessage());
					onShowOrgCreate(viewModel);
				}
	
				@Override
				public void onSuccess(Organization org) {
					getView().setDirty(false);
					getEventBus().updateOrgBalanceLabel();
					getEventBus().showOrgList(new ViewModel<Organization>(org));
				}
			});
		} else {
			getEventBus().showOrgList(null);
		}
	}

	public void onShowOrgCreate(ViewModel<Organization> viewEntityModel){
		if (!UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_MANAGE)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.ORGANISATIONS_MANAGE
							.name()));
			return;
		}
		getEventBus().setNavigationConfirmation(this);
		getEventBus().setRegionRight(this);		
		getView().setFormObject(viewEntityModel);
	}
	
}
