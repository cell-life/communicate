package org.celllife.mobilisr.client.campaign.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.WizardView;
import org.celllife.mobilisr.client.campaign.view.MobilisrEntityEvent;
import org.celllife.mobilisr.client.campaign.view.WizardViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.ScheduleServiceAsync;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter( view=WizardViewImpl.class)
public class WizardPresenter extends DirtyPresenter<WizardView,CampaignEventBus> {

	@Inject
	private ExportServiceAsync exportService;
	
	@Inject
	private OrganizationServiceAsync orgService;

	@Inject
	private CampaignServiceAsync campaignService;

	@Inject
	private ScheduleServiceAsync scheduleService;

	public static final String LIST_ORGANISATIONS = "listOrganizations";
	public static final String ORGANISATION_STORE = "organizationStore";
    public static final String CAMPAIGN_STORE = "campaignStore";
	public static final String GRIDSEARCH_CAMPAIGNS = "campaignStore";
	public static final String CAMPAIGN_SERVICE_ASYNC = "campaignServiceAsync";
	public static final String ORGANISATION_SERVICE_ASYNC = "organizationServiceAsync";
	public static final String CONTACT_SERVICE_ASYNC = "contactServiceAsync";
	public static final String PRESENTER = "Presenter";
	public static final String CAMPAIGN_SCHEDULE_SERVICE_ASYNC = "scheduleServiceAsync";

	public static final String EXPORT_SERVICE_ASYNC = "exportServiceAsync";

	@Override
	public void createPresenter() {
		getView().putItem(CAMPAIGN_SERVICE_ASYNC, campaignService);
		getView().putItem(CAMPAIGN_SCHEDULE_SERVICE_ASYNC, scheduleService);
		getView().putItem(EXPORT_SERVICE_ASYNC, exportService);
	}

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

        RpcProxy<PagingLoadResult<Campaign>> campaignProxy = new RpcProxy<PagingLoadResult<Campaign>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<Campaign>> callback) {
                PagingLoadConfig config = (PagingLoadConfig) loadConfig;
                config.set(RemoteStoreFilterField.PARM_FIELDS, Campaign.PROP_NAME);
                CampaignType[] campaignTypes = {CampaignType.FLEXI,CampaignType.DAILY};
                campaignService.listAllCampaigns(UserContext.getUser().getOrganization(), campaignTypes,config,false,callback);
            }
        };

		ListLoader<PagingLoadResult<Organization>> orgLoader = new BasePagingLoader<PagingLoadResult<Organization>>(
				orgProxy, new BeanModelReader());

        ListLoader<PagingLoadResult<Campaign>> campaignLoader = new BasePagingLoader<PagingLoadResult<Campaign>>(
                campaignProxy, new BeanModelReader());
		
		ListStore<BeanModel> store = new ListStore<BeanModel>(orgLoader);
		getView().setOrganizationStore(store);
		getView().putItem(ORGANISATION_STORE, store);

        ListStore<BeanModel> campaignStore = new ListStore<BeanModel>(campaignLoader);
        getView().setCampaignStore(campaignStore);
        getView().putItem(CAMPAIGN_STORE, campaignStore);

		getView().addListener(MobilisrEvents.SAVE, new Listener<MobilisrEntityEvent>() {
			@Override
			public void handleEvent(final MobilisrEntityEvent be) {
				Campaign campaign3 = (Campaign) be.getEntityObject();
				final boolean goNext = be.isGoNext();
				campaignService.saveOrUpdateCampaign(campaign3, campaign3.getCampaignMessages(),new MobilisrAsyncCallback<Campaign>() {
					@Override
					public void onSuccess(Campaign campaign) {
						getView().setDirty(false);
						ViewModel<Campaign> vem = new ViewModel<Campaign>();
						vem.setModelObject(campaign);
						if(goNext){
							getView().goNext(vem);
						}else{
							getView().goCurrent(vem);
						}
					}
				});
			}
		});

		getView().addListener(MobilisrEvents.CANCEL, new Listener<MobilisrEntityEvent>() {
			@Override
			public void handleEvent(MobilisrEntityEvent be) {
				ViewModel<Campaign> vem = new ViewModel<Campaign>();
				vem.putProperty(ADMIN_VIEW, true);
				getEventBus().showCampaignList(vem);
			}
		});

		getView().addListener(MobilisrEvents.WizardFinish, new Listener<MobilisrEntityEvent>() {
			@Override
			public void handleEvent(MobilisrEntityEvent be) {
				BusyIndicator.showBusyIndicator();
				ViewModel<Campaign> vem = new ViewModel<Campaign>((Campaign) be.getEntityObject());
				Campaign camp = (Campaign) vem.getModelObject();
				campaignService.saveOrUpdateCampaign(camp, camp.getCampaignMessages(), new MobilisrAsyncCallback<Campaign>() {
					@Override
					public void onSuccess(final Campaign campaign) {
						BusyIndicator.hideBusyIndicator();
						
						if (campaign.isActive()){
							// in case any new messages have been added or other changes
							// made that require the schedules to be updated
							scheduleService.scheduleCampaign(campaign, UserContext.getUser(), new MobilisrAsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
								}
							});
						}

						getView().setDirty(false);
						ViewModel<Campaign> vem = new ViewModel<Campaign>();
						vem.putProperty(ADMIN_VIEW, isAdminView());
						vem.setModelObject(campaign);
						getEventBus().showCampaignList(vem);
						
					}
				});

			}
		});
	}

	public void onShowCampaignWizard(ViewModel<Campaign> viewEntityModel){
		if (!UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
				MobilisrPermission.CAMPAIGNS_CREATE,
				MobilisrPermission.CAMPAIGNS_EDIT)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE
							.name()));
			return;
		}
		getEventBus().setNavigationConfirmation(this);
		isAdminView(viewEntityModel);
		getEventBus().setRegionRight(this);
		getView().setFormObject(viewEntityModel);
	}

}
