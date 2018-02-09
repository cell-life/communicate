package org.celllife.mobilisr.client.campaign.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.presenter.RequestNewDialogue;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.CampaignListView;
import org.celllife.mobilisr.client.campaign.view.CampaignListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
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
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.history.NavigationEventCommand;

@Presenter(view = CampaignListViewImpl.class)
public class CampaignListPresenter extends MobilisrBasePresenter<CampaignListView, CampaignEventBus> {

	@Inject
	private AdminServiceAsync adminService;

	@Inject
	private CampaignServiceAsync campaignService;

	@Inject
	private ScheduleServiceAsync campaignSchedulerService;
	
	@Inject
	private OrganizationServiceAsync orgService;

	private MyGXTPaginatedGridSearch<Campaign> gridSearch;
	private CampaignType filterType;

	protected boolean showVoided;

	protected Organization filterOrg;

	@Override
	public void bindView() {
		showVoided = false;
		
		gridSearch = new MyGXTPaginatedGridSearch<Campaign>(Campaign.PROP_NAME
				+","+Campaign.PROP_DESCRIPTION, Constants.INSTANCE.pageSize()) {
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
				if(ce.getButton().getText().equals(Messages.INSTANCE.campaignAddNew())){
					editCampaign(new Campaign());
				} else if (ce.getButton().getText().equals(Messages.INSTANCE.campaignRequestNew())){
					requestNewCampaign();
				}
			}
		});

		getView().getRebuildSchedulesButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				rebuildCampaignSchedules();
			}
		});

		getView().getCampaignNameAnchor().setSelectionListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				editCampaign(campaign);
			}
		});

		getView().getVoidAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				toggleVoidAndRefreshCampaigns(campaign);
			}
		});

		getView().getStartStopAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final Campaign campaign = ce.getModel().getBean();
				final String btnText =  campaign.isActive() ? "Stop" : "Start";

				MessageBoxWithIds.confirm(btnText + " " + campaign.getName()
						+ " campaign", "Are you sure you want to " + btnText + " this campaign?",
						new Listener<MessageBoxEvent>() {
							@Override
							public void handleEvent(MessageBoxEvent be) {
								if (be.getButtonClicked().getItemId()
										.equals(Dialog.YES)) {
									startStopCampaign(campaign);
								}
							}
						});
			}
		});

		getView().getViewMessageLogsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				viewMessageLogs(campaign);
			}
		});

		getView().getManageRecipientsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				manageRecipients(campaign);
			}
		});

		getView().getViewRecipientsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Campaign campaign = ce.getModel().getBean();
				viewRecipients(campaign);
			}
		});

		getView().getShowVoidedButton().addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				showVoided = !showVoided;
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
		
		getView().getTypeFilterCombo().addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
			@Override
			public void selectionChanged(
					SelectionChangedEvent<SimpleComboValue<String>> se) {
				SimpleComboValue<String> item = se.getSelectedItem();
				String value = item.getValue();
				filterType = CampaignType.safeValueOf(value);
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

	private void requestNewCampaign() {
		final RequestNewDialogue dialogue = new RequestNewDialogue("Campaign");
		dialogue.setListener(new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {

				dialogue.hide();
				BusyIndicator.showBusyIndicator("Your request is being emailed.");
				adminService.sendNewUserRequest(dialogue.getUser(), "Campaign request", dialogue.getRequestText(), new MobilisrAsyncCallback<Void>() {
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
	
	public void onShowCampaignList(ViewModel<Campaign> viewEntityModel) {
		getEventBus().setRegionRight(this);
		isAdminView(viewEntityModel);
		filterOrg = null;
		getView().clearFilterOrgCombo();
		getView().setFormObject(viewEntityModel);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}

	private void loadCampaigns(PagingLoadConfig pagingLoadConfig,
			AsyncCallback<PagingLoadResult<Campaign>> callback) {
		
		Organization org = UserContext.getUser().getOrganization();
		if (isAdminView() && UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE)){
			org = filterOrg;
		}

		CampaignType[] types =  null;
		if (filterType == null){
			types = new CampaignType[]{CampaignType.FLEXI, CampaignType.DAILY};
 		}else{
			types = new CampaignType[]{filterType};
		}
		
		campaignService.listAllCampaigns(org, types, pagingLoadConfig, showVoided, callback );
	}

	private void startStopCampaign(final Campaign campaign) {
		User user = UserContext.getUser();
		if (campaign.isActive()){
			campaignSchedulerService.stopCampaign(campaign, user, new MobilisrAsyncCallback<CampaignStatus>() {
				@Override
				public void onSuccess(CampaignStatus status) {
					view.getPagingToolBar().refresh();
					if (status == CampaignStatus.STOPPING) {
						MessageBoxWithIds.alert(campaign.getName()
								+ " campaign", "The campaign will be stopped when all active recipients have received their messages.",null);
					}
				}
			});
		} else {
			campaignSchedulerService.scheduleCampaign(campaign, user, new MobilisrAsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					view.getPagingToolBar().refresh();
				}
			});
		}
	}

	private void editCampaign(Campaign campaign){
		if (UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE,
				MobilisrPermission.CAMPAIGNS_EDIT)){
			ViewModel<Campaign> model = new ViewModel<Campaign>(campaign);
			model.putProperty(ADMIN_VIEW, isAdminView());
			getEventBus().showCampaignWizard(model);
		}
	}

	private void toggleVoidAndRefreshCampaigns(Campaign campaign) {
		if (campaign.getStatus().isActiveState()){
				MessageBoxWithIds.info("Error: Active Campaign",
						"Campaign cannot be deleted while it is " + campaign.getStatus().toString() + ".", null);
				return;
		}
		else{
			campaignService.toggleCampaignVoidState(campaign, new MobilisrAsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
				}
			});
		}
	}

	private void rebuildCampaignSchedules() {
		MessageBoxWithIds.confirm("Proceed?", "Are you sure you want to rebuild the campaign" +
			" schedules for all the active campaigns?", new Listener<MessageBoxEvent>() {
				@Override
				public void handleEvent(MessageBoxEvent be) {
					if (be.getButtonClicked().getItemId()
							.equals(Dialog.YES)) {
						campaignSchedulerService.rescheduleAllRelativeCampaigns(UserContext.getUser(), new MobilisrAsyncCallback<Void>() {
							@Override
							public void onSuccess(Void arg0) {
								MessageBoxWithIds.info("Success", "Successfully rebuild schedules", null);
							}
						});
					}
				}
			});
	}

	private void manageRecipients(Campaign campaign) {
		if(campaign.isActive()){
			ViewModel<Campaign> vem = new ViewModel<Campaign>(campaign);
			vem.putProperty(CampaignListPresenter.ADMIN_VIEW, isAdminView());
			getEventBus().showCampRecipientManage(vem, new Listener<AppEvent>() {
				@Override
				public void handleEvent(final AppEvent be) {
					BusyIndicator.showBusyIndicator();
					Boolean isDirty = be.getData("dirty");
					final NavigationEventCommand navEvent = be.getData("navigationEvent");
					AsyncCallback<Void> callback = new MobilisrAsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							BusyIndicator.hideBusyIndicator();
							if (navEvent != null) {
								navEvent.fireEvent();
							} else {
								ViewModel<Campaign> vem = new ViewModel<Campaign>();
								vem.putProperty(CampaignListPresenter.ADMIN_VIEW, isAdminView());
								getEventBus().showCampaignList(vem);
							}
						}
					};
					if (isDirty){
						Campaign campaign = be.getData();
						campaignService.rescheduleRelativeCampaign(campaign,
							UserContext.getUser(),callback);
					} else {
						callback.onSuccess(null);
					}
					
				}
			}, false);
		}
	}

	private void viewMessageLogs(Campaign campaign) {
		getEventBus().showMessageLog(new ViewModel<Campaign>(campaign));
	}

	private void viewRecipients(Campaign campaign) {
		getEventBus().showCampaignContactList(new ViewModel<Campaign>(campaign));
	}

}
