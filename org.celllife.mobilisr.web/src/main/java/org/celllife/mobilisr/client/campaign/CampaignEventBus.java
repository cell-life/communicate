package org.celllife.mobilisr.client.campaign;

import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.campaign.presenter.CampaignListPresenter;
import org.celllife.mobilisr.client.campaign.presenter.JustSMSCreatePresenter;
import org.celllife.mobilisr.client.campaign.presenter.JustSMSListPresenter;
import org.celllife.mobilisr.client.campaign.presenter.JustSmsSummaryPresenter;
import org.celllife.mobilisr.client.campaign.presenter.ManageRecipientsPresenter;
import org.celllife.mobilisr.client.campaign.presenter.MessageLogPresenter;
import org.celllife.mobilisr.client.campaign.presenter.WizardPresenter;
import org.celllife.mobilisr.client.campaign.view.JustSMSCreateViewImpl;
import org.celllife.mobilisr.client.contacts.presenter.CampaignContactListPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrEntity;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.event.EventBus;

@Events(module=CampaignModule.class, startView=JustSMSCreateViewImpl.class)
public interface CampaignEventBus extends EventBus {
	
	@Event(handlers = JustSMSCreatePresenter.class, navigationEvent = true)
	public void showJustSMSView(ViewModel<Campaign> vem);

	@Event(handlers = JustSmsSummaryPresenter.class, navigationEvent = true)
	public void showJustSendSmsCampaignSummaryView(ViewModel<Campaign> vem);

	@Event(handlers = JustSMSListPresenter.class, navigationEvent = true)
	public void showJustSMSCampaignList(ViewModel<Campaign> vem);

	@Event(handlers = CampaignListPresenter.class, navigationEvent = true)
	public void showCampaignList(ViewModel<Campaign> vem);

	/**
	 * ViewModel may contian the following properties:
	 * <ul>
	 * <li>filterDirection (string): IN or OUT. If specified the view will only
	 * show message logs with the direction specified
	 * <li>showDirectionFilter (boolean): if true the direction filter will
	 * appear in the list toolbar
	 * 
	 * @param vem
	 */
	@Event(handlers = MessageLogPresenter.class, navigationEvent = true)
	public void showMessageLog(ViewModel<? extends MobilisrEntity> vem);

	@Event(handlers = CampaignContactListPresenter.class, navigationEvent = true)
	public void showCampaignContactList(ViewModel<Campaign> vem);

	@Event(handlers = WizardPresenter.class, navigationEvent = true)
	public void showCampaignWizard(ViewModel<Campaign> vem);

	/**
	 * This event requires a listener which will be called when the event /
	 * action is complete.
	 * 
	 * The listener will be called when either the user presses the 'Done'
	 * button or the user navigates away from the screen. When the listener is
	 * called the event will contain the following:
	 * 
	 * <ul>
	 * <li>data - the campaign
	 * <li>data['dirty'] - the dirty state of the view
	 * <li>data['navigationEvent'] - the navigation event (or null)
	 * </ul>
	 * 
	 * @param vem
	 * @param completionListener
	 * @param showInWindow
	 *            if true display the view in a popup window
	 */
	@Event(handlers = ManageRecipientsPresenter.class, navigationEvent = true)
	public void showCampRecipientManage(ViewModel<Campaign> vem,
			Listener<AppEvent> completionListener, boolean showInWindow);

	// =============== PARENT EVENTS

	@Event(forwardToParent = true)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);
	
	@Event(forwardToParent = true)
	public void updateOrgBalanceLabel();
	
	@Event(forwardToParent = true)
	public void showErrorView(String message);
}
