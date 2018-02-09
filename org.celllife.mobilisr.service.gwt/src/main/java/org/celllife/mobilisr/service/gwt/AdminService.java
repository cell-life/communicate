package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see AdminService
 */
@RemoteServiceRelativePath("adminService.rpc")
public interface AdminService extends RemoteService {

	/**
	 * @see AdminService#sendMailNow()
	 */
	void sendMailNow() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see AdminService#getMailQueueCount()
	 */
	int getMailQueueCount() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see AdminService#getLostMessagesCount()
	 */
	int getLostMessagesCount() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @param organization TODO
	 * @see AdminService#getEntityList(Organization, String, PagingLoadConfig)
	 */
	PagingLoadResult<MobilisrEntity> getEntityList(Organization organization, String entityName, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see AdminService#sendNewUserRequest(User, String, String)
	 */
	void sendNewUserRequest(User user, String requestType, String requestText) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see AdminService#getNumberInfoList()
	 */
	List<NumberInfo> getNumberInfoList() throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see AdminService#sendNewOrganizationNotification(OrganisationNotificationViewModel)
	 */
	void sendNewOrganizationNotification(OrganisationNotificationViewModel model) throws MobilisrException, MobilisrRuntimeException;

}
