package org.celllife.mobilisr.client.org.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.org.AdminOrgListView;
import org.celllife.mobilisr.client.org.view.AdminOrgListViewImpl;
import org.celllife.mobilisr.client.org.view.OrganizationAccountDialog;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = AdminOrgListViewImpl.class)
public class AdminOrgListPresenter extends MobilisrBasePresenter<AdminOrgListView, AdminEventBus> {

	@Inject
	private OrganizationServiceAsync orgService;
	
	@Inject
	private CampaignServiceAsync campaignService;
	
	private MyGXTPaginatedGridSearch<Organization> gridSearch;

	protected boolean showVoided;
	
	@Override
	public void bindView() {
		showVoided = false;
		
		gridSearch = new MyGXTPaginatedGridSearch<Organization>(Organization.PROP_NAME, Constants.INSTANCE.pageSize()) {
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<Organization>> callback) {
				orgService.listAllOrganizations(pagingLoadConfig, showVoided, callback);
			}
		};
		
		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());

		getView().getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {

			@Override
			public void handleEvent(GridEvent<BeanModel> gridEvent) {
				BeanModel beanModel = gridEvent.getModel();
				Organization org =  beanModel.getBean();
				ViewModel<Organization> viewEntityModel = new ViewModel<Organization>(org);
				getEventBus().showOrgCreate(viewEntityModel);
			}
		});
		
		getView().getNewEntityButton().addListener(Events.Select, new Listener<ButtonEvent>() {

					public void handleEvent(ButtonEvent be) {
						ViewModel<Organization> viewEntityModel = new ViewModel<Organization>(new Organization());
						getEventBus().showOrgCreate(viewEntityModel);
					}
				});
		
		getView().getSendNotificationButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				getEventBus().showOrganizationNotificationView();
			}
		});
		
		getView().getCreditAccountAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final Organization org = ce.getModel().getBean();
				final OrganizationAccountDialog dialog = new OrganizationAccountDialog();
				dialog.setFormObject(org);
				dialog.setSaveListener(new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						final int amount = dialog.getAmount();
						final String reason = dialog.getReason();
						if (amount == 0 || reason.isEmpty()){
							return;
						}
						
						int newBalance = org.getAvailableBalance() + amount;
						
						MessageBoxWithIds.confirm("Confirm Transaction", 
								Messages.INSTANCE.orgListAdjustBalanceMessage(amount, newBalance),
								new Listener<MessageBoxEvent>() {
							@Override
							public void handleEvent(MessageBoxEvent be) {
								if (Dialog.YES.equals(be.getButtonClicked().getItemId())){
									creditOrgAccount(org, amount, reason);
								}
							}
						});
					}
				});
				dialog.show();
			}
		});
		
		getView().getToggleStateAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final Organization org = ce.getModel().getBean();
				toggleVoidAndRefreshOrganizations(org);
			}
		});
		
		getView().getShowVoidedButton().addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				showVoided = !showVoided;
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
	}
	
	public void onShowOrgList(ViewModel<Organization> vem) {
		if (!UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_MANAGE)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.ORGANISATIONS_MANAGE
							.name()));
			return;
		}
		if (vem != null && vem.getModelObject() != null){
			Organization org = (Organization) vem.getModelObject();
			getView().displaySuccessMsg("Organisation: \'" +  org.getName() + "\' saved successfully");
		}else{
			getView().clearSuccessMsg();
		}
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
	
	private void creditOrgAccount(Organization org, int amount, String reason){
		User user = UserContext.getUser();
		MobilisrAsyncCallback<Long> callback = new MobilisrAsyncCallback<Long>() {
			@Override
			public void onSuccess(Long ref) {
				eventBus.updateOrgBalanceLabel();
				gridSearch.getLoader().load(gridSearch.getLoader().getOffset(), 
						Constants.INSTANCE.pageSize());
				MessageBoxWithIds.info("Account balance updated", 
						"Transaction reference: " + ref, null);
			}
		};
		
		if (amount > 0){
			orgService.creditOrganizationAcount(org, amount, reason, user,callback);
		} else {
			orgService.debitOrganizationAcount(org, amount, reason, user,callback);
		}
		
	}

	private void toggleVoidAndRefreshOrganizations(final Organization org) {
		
		if (UserContext.getUser().getOrganization().equals(org)){
			MessageBoxWithIds.alert("Error", 
					"You cannot activate or deactivate your own organization!", null);
			
			return;
		}

		campaignService.getNumberActiveCampaigns(org, new MobilisrAsyncCallback<Integer>() {

			@Override
			public void onSuccess(Integer numActiveCampaigns) {
				if (numActiveCampaigns == 0){
					org.setVoided(!org.getVoided());
					orgService.saveOrUpdateOrganisation(org, new MobilisrAsyncCallback<Organization>() {
							@Override
							public void onSuccess(Organization arg0) {
								gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
							}
						});
				}else{
					String msg = (numActiveCampaigns>1)? numActiveCampaigns+  " active campaigns ": "1 active campaign "; 
					
					MessageBoxWithIds.info("Error: Active Campaigns", 
							"There are " + msg + " in this organisation. The organisation cannot be voided until they have stopped.", null);
				}
			}
		});
	}
}
