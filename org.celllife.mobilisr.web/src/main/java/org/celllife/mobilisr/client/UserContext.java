package org.celllife.mobilisr.client;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganizationService;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserContext {

	private static User user;
	private static OrganizationServiceAsync OrganizationServiceAsync;
	
	public static void setUser(User loggedUser){
		UserContext.user = loggedUser;
	}

	public static User getUser() {
		return user;
	}
	
	public static void refreshOrgBalance(Organization org, AsyncCallback<Organization> callback){
		initCrudOrgAsyncService();
		OrganizationServiceAsync.refreshOrganization(org, callback);
	}
	
	public static boolean hasPermission(MobilisrPermission... permission){
		for (MobilisrPermission perm : permission) {
			if (user.hasPermission(perm)){
				return true;
			}
		}
		return false;
	}
	
	private static void initCrudOrgAsyncService() {
		if(OrganizationServiceAsync == null){
			OrganizationServiceAsync = (OrganizationServiceAsync) GWT.create(OrganizationService.class);
		}
	}
	
	/**
	 * Convenience method for testing
	 * 
	 * @param OrganizationServiceAsync
	 */
	static void setOrganizationServiceAsync(OrganizationServiceAsync OrganizationServiceAsync) {
		UserContext.OrganizationServiceAsync = OrganizationServiceAsync;
	}
}
