package org.celllife.mobilisr.client.campaign.presenter;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.JustSmsSummaryView;
import org.celllife.mobilisr.client.campaign.view.JustSmsSummaryViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.ScheduleServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = JustSmsSummaryViewImpl.class)
public class JustSmsSummaryPresenter extends MobilisrBasePresenter<JustSmsSummaryView, CampaignEventBus> {

	@Inject
	ScheduleServiceAsync schedCampService;
	
	@Override
	public void bindView() {
		getView().getBtnSchedule().addListener(Events.Select, new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						confirmScheduleCampaign();
					}					
				});

		getView().getBtnEditCampaign().addListener(Events.Select, new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						ViewModel<Campaign> viewEntityModel = new ViewModel<Campaign>(getView().getCampaign());
						viewEntityModel.setDirty(true);
						viewEntityModel.putProperty(ADMIN_VIEW, isAdminView());
						getEventBus().showJustSMSView(viewEntityModel);
					}
				});

		getView().getBtnCheckCampaign().addListener(Events.Select, new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						getView().showSendSmsTest();
					}
				});

		getView().getBtnCloseCampaignSummary().addListener(Events.Select, new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						ViewModel<Campaign> model = new ViewModel<Campaign>();
						model.putProperty(ADMIN_VIEW, isAdminView());
						getEventBus().showJustSMSCampaignList(model);
					}
				});
		getView().getSmsSendButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				sendTestSMS();
			}
		});
	}

	public void onShowJustSendSmsCampaignSummaryView( ViewModel<Campaign> viewEntityModel) {
		isAdminView(viewEntityModel);
		getEventBus().setRegionRight(this);
		getView().setFormObject(viewEntityModel);
	}
	
	private void sendTestSMS() {
		User user = UserContext.getUser();
		String campaignTestMessage = getView().getCampaign().getCampaignMessages().get(0).getMessage();
		String smsTestNumber = getView().getSmsTestNumber();
		schedCampService.sendTestSMS(getView().getCampaign(), user, smsTestNumber,
			campaignTestMessage, new MobilisrAsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					updateViewBalance();
				}
			}
		);
	}
	
	private void confirmScheduleCampaign() {
		double balance = getView().getBalanceAfterCampaign();

		if (balance >= 0) {
			MessageBoxWithIds.confirm("Confirm","Your campaign has been saved. Do you want to start it now?", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent ce) {
						if (ce.getButtonClicked().getItemId().equals(Dialog.YES)) {
							scheduleCampaign();
						}
					}
			});
		} else {
			MessageBoxWithIds.info("Insufficient Credit",
					"Your campaign has been saved. "
							+ "Unfortunately you do not have enough credit to run this campaign. "
							+ "You can start this campaign later when you have enough credit",
					null);
			ViewModel<Campaign> model = new ViewModel<Campaign>();
			getEventBus().showJustSMSCampaignList(model);
		}
	}
	
	private void scheduleCampaign() {
		User user = UserContext.getUser();
		final Campaign campaign = getView().getCampaign();
		schedCampService.scheduleCampaign(campaign,user,
			new MobilisrAsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					ViewModel<Campaign> model = new ViewModel<Campaign>().setViewMessage(getView().getCampSchedStatusMsg());
					model.putProperty(ADMIN_VIEW, isAdminView());
					getEventBus().updateOrgBalanceLabel();
					getEventBus().showJustSMSCampaignList(model);
				}
		});
	}

	/**
	 * Convenience helper method for updating balances in the view.
	 */
	private void updateViewBalance() {
		// Update the header balance
		getEventBus().updateOrgBalanceLabel();
		// Update the balance in the view (by refreshing the organization, etc.)
		UserContext.refreshOrgBalance(getView().getCampaign().getOrganization(), 
				new MobilisrAsyncCallback<Organization>() {
					@Override
					public void onSuccess(Organization orgNew) {
						getView().getCampaign().setOrganization(orgNew);
						ViewModel<Campaign> model = new ViewModel<Campaign>(getView().getCampaign());
						getView().setFormObject(model);
					}
				});
		
		MessageBoxWithIds.info("Success", "Message successfully sent.", null);
	}

}
