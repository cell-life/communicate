package org.celllife.mobilisr.client.campaign.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.JustSMSListView;
import org.celllife.mobilisr.client.campaign.view.JustSMSListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.ScheduleServiceAsync;

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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = JustSMSListViewImpl.class)
public class JustSMSListPresenter extends MobilisrBasePresenter<JustSMSListView, CampaignEventBus> {

	@Inject
	private CampaignServiceAsync campaignService;
	
	@Inject
	private ScheduleServiceAsync campaignSchedulerService;
	
	@Inject
	private OrganizationServiceAsync orgService;
	
	private MyGXTPaginatedGridSearch<Campaign> gridSearch;

	protected boolean showVoided;

	protected Organization filterOrg;
	
	@Override
	public void bindView() {
		showVoided = false;
		
		gridSearch = new MyGXTPaginatedGridSearch<Campaign>(Campaign.PROP_NAME, Constants.INSTANCE.pageSize()) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<Campaign>> callback) {
				loadCampaigns(pagingLoadConfig, callback);
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
		
		getView().getNewEntityButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				ViewModel<Campaign> model = new ViewModel<Campaign>();
				model.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showJustSMSView(model);
			}
		});
		
		getView().getScheduleAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				unscheduleCampaign(campaign);
			}
		});
		
		getView().getViewCampaignSummaryAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				showSummaryView(campaign);
			}
		});
		
		getView().getViewRecipientsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				viewRecipients(campaign);
			}
		});
		
		getView().getToggleVoidAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				toggleVoidAndRefreshCampaigns(campaign);
			}
		});
		
		getView().getViewMessageLogsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				viewMessageLogs(campaign);
			}
		});
		
		getView().getCampaignNameAnchor().setSelectionListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				ViewModel<Campaign> model = new ViewModel<Campaign>(campaign);
				model.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showJustSMSView(model);
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

	public void onShowJustSMSCampaignList(ViewModel<Campaign> viewEntityModel) {
		isAdminView(viewEntityModel);
		getEventBus().setRegionRight(this);
		filterOrg = null;
		getView().clearFilterOrgCombo();
		getView().setFormObject(viewEntityModel);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
	
	private void loadCampaigns(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<Campaign>> callback) {
		Organization org = UserContext.getUser().getOrganization();
		if (isAdminView() && UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE)){
			org = filterOrg;
		}
		
		campaignService.listAllCampaigns(org,
				new CampaignType[] { CampaignType.FIXED }, pagingLoadConfig,
				showVoided, callback);
	}

	private void unscheduleCampaign(Campaign campaign) {
		campaignSchedulerService.stopCampaign(campaign, UserContext.getUser(), new MobilisrAsyncCallback<CampaignStatus>() {
			@Override
			public void onSuccess(CampaignStatus status) {
				view.getPagingToolBar().refresh();
			}
		});
	}

	private void showSummaryView(final Campaign campaign) {
		campaignService.findCampMessageByCampaign(campaign, new MobilisrAsyncCallback<List<CampaignMessage>>() {
			@Override
			public void onSuccess(List<CampaignMessage> campaignMessages) {
				campaign.setCampaignMessages(campaignMessages);
				ViewModel<Campaign> model = new ViewModel<Campaign>(campaign);
				model.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showJustSendSmsCampaignSummaryView(model);
			}
		});
	}

	private void toggleVoidAndRefreshCampaigns(Campaign campaign) {

		if (campaign.getStatus().isActiveState()){
				MessageBoxWithIds.info("Error: Active Campaign", 
						"Campaign cannot be deleted while it is " + campaign.getStatus().toString() + ".", null);
				return;
		}
		else{
			campaign.setVoided(!campaign.getVoided());
			campaignService.saveOrUpdateCampaign(campaign, campaign.getCampaignMessages(), new MobilisrAsyncCallback<Campaign>() {
				@Override
				public void onSuccess(Campaign campaign) {
					gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
				}
			});
		}
	}

	private void viewMessageLogs(Campaign campaign) {
		getEventBus().showMessageLog(new ViewModel<Campaign>(campaign));
	}

	private void viewRecipients(Campaign campaign) {
		getEventBus().showCampaignContactList(new ViewModel<Campaign>(campaign));
	}

}
