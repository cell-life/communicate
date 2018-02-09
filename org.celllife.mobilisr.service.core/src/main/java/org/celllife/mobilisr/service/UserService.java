package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.exception.UniquePropertyException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * CrudClientService responsible for managing all the CRUD operations
 * @author Vikram Bindal (vikram@cell-life.org)
 */
public interface UserService extends RemoteService{

	/**
	 * Method responsible for saving the user
	 * @param user	User object that must be saved
	 * @param roles	Roles for this user.
	 * @return		persisted User
	 */
	User saveUser(User user, List<Role> roles) throws UniquePropertyException;

	/**
	 * Method responsible for saving the current user.
	 * Variation on saveUser().
	 * @param user	User object that must be saved
	 * @return		persisted User
	 */
	User saveCurrentUser(User user) throws UniquePropertyException;

	/**
	 * Method responsible for deleting the object from the DB
	 * @param userId	Id of the user that must be deleted
	 */
	void deleteUser(Long userId);

	/**
	 * Method responsible for obtaining the User for a given Id
	 * @param id	Id for which the User must be fetched
	 * @return 		User for the particular id
	 */
	User getUser(Long id);
	
	/**
	 * Method responsible for obtaining the currently logged in user
	 * from Spring Security
	 * @return		User that is currently logged into the system
	 */
	User getCurrentLoggedInUser();

	/**
	 * @return the number of users currently logged in
	 */
	int getLoggedInUserCount();
	
	/**
	 * @return a list of all users currently logged into Mobilisr
	 */
	List<User> getAllLoggedInUsers();

	/**
	 * Method responsible for obtaining the list of all the Users in DB
	 * @return List containing all the User objects
	 */
	List<User> listAllUsers();

	User setChannelsForUser(User user, Long[] channelIds);
	
	/**
	 * Method responsible for obtaining the User via its username.
	 * This is mostly used via the Spring Security once the User credentials are validated
	 * @param username	Username for which the user must be fetched
	 * @return			User for a given username
	 */
	User getUserByUsername(String username);
	
	List<User> findUsersByEmail(String email);
	
	List<Role> listOfRolesForUser(User user);
	
	Boolean validatePassword(User user, String pw);

	String resetPassword(User user) ;

	PagingLoadResult<User> listAllUsers(Organization org, PagingLoadConfig loadConfig, Boolean voidedStatus);

	List<User> getUsersForOrganisation(Organization org);

	ApiKey createApiKey(User user);
}
