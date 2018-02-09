package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.UserService
 */
@RemoteServiceRelativePath("crudUser.rpc")
public interface UserService extends RemoteService{

	/**
	 * @see org.celllife.mobilisr.service.UserService#saveUser(User, List)
	 */
	User saveUser(User user, List<Role> roles) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.UserService#saveCurrentUser(User)
	 */
	User saveCurrentUser(User user) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.UserService#getUser(Long)
	 */
	User getUser(Long id) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.UserService#getCurrentLoggedInUser()
	 */
	User getCurrentLoggedInUser() throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.UserService#getLoggedInUserCount
	 */
	int getLoggedInUserCount() throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.UserService#getAllLoggedInUsers()
	 */
	List<User> getAllLoggedInUsers() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.UserService#validatePassword(User, String)
	 */
	Boolean validatePassword(User user, String pw) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.UserService#listAllUsers(Organization, PagingLoadConfig, Boolean)
	 */
	PagingLoadResult<User> listAllUsers(Organization org, PagingLoadConfig loadConfig, Boolean voidedStatus) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.UserService#createApiKey(User)
	 */
	ApiKey createApiKey(User user) throws MobilisrException, MobilisrRuntimeException;
}
