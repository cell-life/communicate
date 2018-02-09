package org.celllife.mobilisr.client.campaign.presenter;

import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.JustSMSCreateView;
import org.celllife.mobilisr.client.campaign.view.JustSMSCreateViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = JustSMSCreateViewImpl.class)
public class JustSMSCreatePresenter extends DirtyPresenter<JustSMSCreateView,CampaignEventBus> {

	@Inject
	private CampaignServiceAsync service;

	@Override
	public void bindView() {
		getView().getManageRecipientsButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				manageRecipients();
			}
		});
		
		getView().getFormSubmitButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				saveOrUpdateCampaignAndProceed();
			}
		});

		getView().getFormCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().setFormObject(new ViewModel<Campaign>(new Campaign()));
			}
		});
		
		getView().getCampaignListButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				ViewModel<Campaign> model = new ViewModel<Campaign>();
				model.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showJustSMSCampaignList(model);
			}
		});
	}
	
	private void manageRecipients() {
		BusyIndicator.showBusyIndicator();
		Campaign campaign = getView().getCampaign();
		service.saveOrUpdateFixedCampaign(campaign, new MobilisrAsyncCallback<Campaign>() {
			@Override
			public void onSuccess(Campaign campaign) {
				
				BusyIndicator.hideBusyIndicator();
				getView().setDirty(false);
				ViewModel<Campaign> vem = new ViewModel<Campaign>(campaign);
				vem.putProperty(ADMIN_VIEW, isAdminView());
				
				getEventBus().showCampRecipientManage(vem, new Listener<AppEvent>() {
					@Override
					public void handleEvent(final AppEvent be) {
						BusyIndicator.showBusyIndicator();
						
						Campaign campaign = be.getData();
						
						// update campaign contact count
						service.saveOrUpdateFixedCampaign(campaign, new MobilisrAsyncCallback<Campaign>() {
							@Override
							public void onSuccess(Campaign campaign) {
								BusyIndicator.hideBusyIndicator();
								ViewModel<Campaign> vem = new ViewModel<Campaign>(campaign);
								vem.putProperty(ADMIN_VIEW, isAdminView());
								
								onShowJustSMSView(vem);
							}
						});
					}
				}, true);
			}
		});
	}

	public void onShowJustSMSView(final ViewModel<Campaign> viewEntityModel) {
		BusyIndicator.showBusyIndicator();
		getEventBus().setNavigationConfirmation(this);
		isAdminView(viewEntityModel);
		Campaign campaign = (Campaign) viewEntityModel.getModelObject();
		if (campaign == null){
			campaign = new Campaign();
		}
		AsyncCallback<Campaign> callback = new MobilisrAsyncCallback<Campaign>() {
			@Override
			public void onSuccess(Campaign campaign) {
				getEventBus().setRegionRight(JustSMSCreatePresenter.this);
				viewEntityModel.setModelObject(campaign);
				getView().setFormObject(viewEntityModel);
				BusyIndicator.hideBusyIndicator();
			}
		};
		if (campaign.isPersisted()){
			service.loadCampaignWithMessages(campaign.getId(), callback);
		} else {
			callback.onSuccess(campaign);
		}
	}

	private void saveOrUpdateCampaignAndProceed() {
		Campaign campaign = getView().getCampaign();
		if (campaign.getContactCount() <= 0){
			MessageBoxWithIds.alert("Please add some contacts", 
					"You have not selected any contacts to send the message to.", null);
		} else if (campaign.getCampaignMessages().isEmpty() ||
				(campaign.getCampaignMessages().get(0).getMsgLength().intValue() == 0) ) {
			MessageBoxWithIds.alert("Message is empty", 
					"Please type a message in the message box.", null);
		} else {
			BusyIndicator.showBusyIndicator();
			service.saveOrUpdateFixedCampaign(campaign, new MobilisrAsyncCallback<Campaign>() {
					@Override
					public void onSuccess(Campaign campaign) {
						BusyIndicator.hideBusyIndicator();
						getView().setDirty(false);
						ViewModel<Campaign> model = new ViewModel<Campaign>(campaign);
						model.putProperty(ADMIN_VIEW, isAdminView());
						getEventBus().showJustSendSmsCampaignSummaryView(model);
					}
				});
		}
	}
}
