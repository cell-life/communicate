package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.HeaderView;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.view.HeaderViewImpl;
import org.celllife.mobilisr.client.user.view.UserProfileViewImpl;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.UserServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.LazyPresenter;

@Presenter(view=HeaderViewImpl.class)
public class HeaderPresenter extends LazyPresenter<HeaderView, MobilisrEventBus> {

	@Inject
	private UserServiceAsync userServiceAsync;
	
	@Override
	public void bindView() {
		userServiceAsync.getCurrentLoggedInUser(new MobilisrAsyncCallback<User>() {
			@Override
			public void onSuccess(final User user) {
				
				UserContext.setUser(user);
				String userName = user.getFirstName().concat(" ").concat(user.getLastName());
				String orgName = user.getOrganization().getName();
				String welcomeLabelText = "Welcome " + userName + " from " + orgName;
				
				getView().getWelcomeLabel().setText(welcomeLabelText);
				dispUpdatedOrgBalance();
				getView().getAdminButton().displayButtonBasedOnUserPermission();
				//We fire this event from the event bus so that when the app loads up
				//it displays the campaign list. If we do it via eventBus.showProgramRegion in HomePresenter (onStart)
				//then it will not load as async service problem as we donot have the User quite yet.
				//This fix accomodates that
				getEventBus().showHomeRegion();
				
				if (!user.getLoginSinceUpgrade()) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand(){
						@Override
						public void execute() {
							user.setLoginSinceUpgrade(true);
							userServiceAsync.saveCurrentUser(user, null);
						}
					});
					getEventBus().showWhatsNew();
				}
			}
		});
		
		final Timer t = new Timer() {
			@Override
			public void run() {
				dispUpdatedOrgBalance();
			} 
			
		};
		// update balance every 5 minutes
		t.scheduleRepeating(300000);
		
		NavigationListener navListener = new NavigationListener(this);
		
		getView().getContactsButton().addListener(Events.Select, navListener);
		
		getView().getAdminButton().addListener(Events.Select, navListener);
		
		getView().getProfileButton().addListener(Events.Select, new Listener<ButtonEvent>() {
			@Override
			public void handleEvent(ButtonEvent be) {
				new UserProfileViewImpl(userServiceAsync);
			}
		});
		
		getView().getLogoutButton().addListener(Events.Select, navListener);
		
		getView().getProgramsButton().addListener(Events.Select, navListener);
		
	}
	
	public void onStart() {
		//Fire the event
		eventBus.setRegionHeader( getView().getViewWidget() );
	}
	
	private void dispUpdatedOrgBalance() {
		UserContext.refreshOrgBalance(UserContext.getUser().getOrganization(), 
				new AsyncCallback<Organization>() {
					@Override
					public void onSuccess(Organization org) {
						UserContext.getUser().setOrganization(org);
						int balance = org.getAvailableBalance();
						String balanceLabelText = "Balance " + balance + " " + Messages.INSTANCE.orgCredits();
						if (balance < org.getBalanceThreshold()){
							onSetRedStatus("Please top up your credits.");
						}else{
							onClearStatus();
						}
						getView().getBalanceLabel().setText(balanceLabelText);
					}

					@Override
					public void onFailure(Throwable arg0) {
						// ignore failures here
					}
				});
	} 
	
	public void onUpdateOrgBalanceLabel(){
		dispUpdatedOrgBalance();
	}
	
	private void onClearStatus(){
		getView().getStatusLabel().setText("");
		getView().adjustHeight();
	}
	
	private void onSetRedStatus(String status){
		getView().getStatusLabel().setText("Warning: " + status);
		getView().getStatusLabel().setStyleAttribute("color", "red");
		getView().adjustHeight();
	}
}
