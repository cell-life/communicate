package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public class OrganisationNotificationViewModel implements BeanModelTag, Serializable {

	private static final long serialVersionUID = 4110556762719248420L;
	
	private String subject;
	private String message;
	private boolean includeUsers = false;
	private boolean sendToAll = false;
	
	private List<Long> organisationList;

	private String testEmail;

	public String getMessage() {
		return message;
	}

	public List<Long> getOrganisationList() {
		if (organisationList == null){
			organisationList = new ArrayList<Long>();
		}
		return organisationList;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isSendToAll() {
		return sendToAll;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setOrganisationList(List<Long> organisationList) {
		this.organisationList = organisationList;
	}

	public void setSendToAll(boolean sendToAll) {
		this.sendToAll = sendToAll;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isIncludeUsers() {
		return includeUsers;
	}

	public void setIncludeUsers(boolean includeUsers) {
		this.includeUsers = includeUsers;
	}
	
	public void setTestEmail(String testEmail) {
		this.testEmail = testEmail;
	}

	public String getTestEmail() {
		return testEmail;
	}
}