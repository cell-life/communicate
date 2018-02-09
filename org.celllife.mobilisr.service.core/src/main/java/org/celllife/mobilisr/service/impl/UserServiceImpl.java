package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.security.ApiKeyGenerator;
import org.celllife.mobilisr.service.security.MobilisrUserDetails;
import org.celllife.mobilisr.service.security.impl.RandomApiKeyGenerator;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.celllife.mobilisr.util.MobilisrSecurityUtility;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;
import com.trg.search.SearchResult;

/**
 * Default implementation for the CrudUserService
 * @author Vikram Bindal (vikram@cell-life.org)
 */
@Service("crudUserService")
public class UserServiceImpl extends BaseServiceImpl implements UserService {

	private static final long serialVersionUID = 6356173139214329251L;

	/**
	 * UserDAO that would be responsible for DAO operations related to user
	 */
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private ChannelDAO channelDAO;
	
	@Autowired
	private SessionRegistry sessionRegistry;
	
	/**
	 * PasswordEncoder used for encoding the passwords
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	private ApiKeyGenerator keyGenerator = new RandomApiKeyGenerator();

	@Loggable(LogLevel.TRACE)
	@Override
	public User getUser(Long id) {
		Search search = new Search(User.class);
		search.addFilter(Filter.equal(User.PROP_ID, id));
		search.addFetch(User.PROP_ROLES);
		search.addFetch(User.PROP_ORGANIZATION);
		User user = userDAO.searchUnique(search);
		
		search = new Search(ApiKey.class);
		search.addFilterEqual(ApiKey.PROP_USER, user);
		@SuppressWarnings("unchecked")
		List<ApiKey> keys = getGeneralDao().search(search);
		user.setApiKeys(keys);
		
		return user;
	}
	
	@Override
	public List<User> getUsersForOrganisation(Organization org) {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_ORGANIZATION, org);
		List<User> users = userDAO.search(search);
		return users;
		
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_MANAGE_USERS"})
	public User saveUser(User user, List<Role> roles) throws UniquePropertyException {

		if (user.isPersisted()){
			User persistentUser = userDAO.find(user.getId());
			// encode password if changed
			if( !persistentUser.getPassword().equals(user.getPassword())){
				encodePassword(user);
			}
		} else {
			encodePassword(user);
		}
		
		User existing = userDAO.getUserByUsername(user.getUserName());
		if (existing != null && !existing.getId().equals(user.getId())){
			throw new UniquePropertyException("User with username \'" + user.getUserName() + "\' already exist");
		}
		
		existing = userDAO.getUserByEmail(user.getEmailAddress());
		if (existing != null && !existing.getId().equals(user.getId())){
			throw new UniquePropertyException("User with email address \'" + user.getEmailAddress() + "\' already exist");
		}
		
		try{
			userDAO.saveOrUpdate(user);
			if (roles !=null) userDAO.saveOrUpdateUserRoles(user, roles);
			
		}catch(ConstraintViolationException e){
			String msg = "Unable to save user";
			if (e.getCause() != null){
				msg += ": " + e.getCause().getMessage();
			}
			throw new UniquePropertyException(msg,e);
		}

		List<ApiKey> apiKeys = user.getApiKeys();
		List<ApiKey> deleted = new ArrayList<ApiKey>();
		for (ApiKey apiKey : apiKeys) {
			if (apiKey.isDeleted()){
				deleted.add(apiKey);
			}
		}
		getGeneralDao().remove(deleted.toArray());
		user.getApiKeys().removeAll(deleted);		
		return user;
	}
	
	@Override
	public User saveCurrentUser(User user) throws UniquePropertyException {
		// TODO: we could use spring security run-as manger to call the saveUser method
		// http://static.springsource.org/spring-security/site/docs/3.0.x/reference/runas.html
		
		// User's ID will not change.
		Long uid = user.getId();
		Long cuid = getCurrentLoggedInUser().getId();
		if ( !(uid.equals(cuid)) )
			throw new UniquePropertyException("User does not match currently logged in user.");
		User persistentUser = userDAO.find(user.getId());
		// encode password if changed
		if( !persistentUser.getPassword().equals(user.getPassword())){
			encodePassword(user);
		}
		
		// User might change their username.
		User existing = userDAO.getUserByUsername(user.getUserName());
		if (existing != null && !existing.getId().equals(user.getId())){
			throw new UniquePropertyException("User with username \'" + user.getUserName() 
					+ "\' already exists.");
		}
		
		existing = userDAO.getUserByEmail(user.getEmailAddress());
		if (existing != null && !existing.getId().equals(user.getId())){
			throw new UniquePropertyException("User with email address \'" + user.getEmailAddress()
					+ "\' already exists");
		}
		
		try {
			userDAO.saveOrUpdate(user);
		} catch(ConstraintViolationException e) {
			String msg = "Unable to save user";
			if (e.getCause() != null){
				msg += ": " + e.getCause().getMessage();
			}
			throw new UniquePropertyException(msg,e);
		}
			
		return user;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_MANAGE_USERS"})
	public void deleteUser(Long userId) {
		userDAO.removeById(userId);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<User> listAllUsers() {
		List<User> listOfUsers = (List<User>) userDAO.findAll();
		return listOfUsers;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public User setChannelsForUser(User user, Long[] channelIds) {
		if ((channelIds != null) && (channelIds.length > 0)) {
			Channel[] channels = channelDAO.find(channelIds);
			List<Channel> channelList = Arrays.asList(channels);
			user.getOrganization().setChannels(channelList);
		}

		return user;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public User getUserByUsername(String username) {
		return userDAO.getUserByUsername(username);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<User> listAllUsers(Organization org, PagingLoadConfig loadConfig, Boolean showVoided) {
		
		Search search = ServiceUtil.getSearchFromLoadConfig(User.class, loadConfig, null);
		if (loadConfig.getSortField() == null)
			search.addSort(User.PROP_ORGANIZATION+"."+Organization.PROP_NAME, false);
		
		search.addSort(User.PROP_FIRST_NAME, false);
		
		if (showVoided!=null){
			if (showVoided){
				// users who are voided or users who belong to voided organisations
				search.addFilterOr(Filter.equal(User.PROP_VOIDED, showVoided),
						Filter.equal(User.PROP_ORGANIZATION+"."+Organization.PROP_VOIDED,showVoided));
			} else {
				// users who are not voided AND whose organisation is not voided
				search.addFilterAnd(Filter.equal(User.PROP_VOIDED, showVoided),
						Filter.equal(User.PROP_ORGANIZATION+"."+Organization.PROP_VOIDED,showVoided));
			}
		}
		
		if (org != null){
			search.addFilterEqual(User.PROP_ORGANIZATION, org);
		}
		
		SearchResult<User> searchResult = userDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	public List<Role> listOfRolesForUser(User user) {
		Search search = new Search(User.class);
		search.addFilter(Filter.equal(User.PROP_ID, user.getId()));
		search.addField(User.PROP_ROLES);
		List<Role> listOfRoles = userDAO.search(search);
		return listOfRoles;
		
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public User getCurrentLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null){
			return null;
		}
		MobilisrUserDetails mobilisrUser = (MobilisrUserDetails) authentication.getPrincipal();
		return getUser(mobilisrUser.getUser().getId());
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public int getLoggedInUserCount(){
		return sessionRegistry.getAllPrincipals().size();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<User> getAllLoggedInUsers(){
		List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
		List<User> users = new ArrayList<User>();
		for (Object principal : allPrincipals) {
			if (principal instanceof MobilisrUserDetails){
				MobilisrUserDetails user = (MobilisrUserDetails) principal;
				users.add(user.getUser());
			}
		}
		return users;
	}
	
	private void encodePassword(User user){
		
		String salt = MobilisrSecurityUtility.getRandomToken();
		String encodedPwd = passwordEncoder.encodePassword(user.getPassword(), salt);
		user.setPassword(encodedPwd);
		user.setSalt(salt);
	}	
	
	@Loggable(LogLevel.TRACE)
	@Override
	public Boolean validatePassword(User user, String pw){
		String salt = user.getSalt();
		String encodedPwd = passwordEncoder.encodePassword(pw, salt);
		return user.getPassword().equals(encodedPwd);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<User> findUsersByEmail(String email) {
		return userDAO.searchByPropertyEqual(User.PROP_EMAIL, email);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public String resetPassword(User user) {
		String password = UUID.randomUUID().toString();
		password = password.replace("-", "").substring(0, 8);
		user.setPassword(password);
		encodePassword(user);
		userDAO.saveOrUpdate(user);
		return password;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public ApiKey createApiKey(User user){
		List<?> existingKeys;
		String key;
		do {
			key = keyGenerator.generateKey();
			Search s = new Search(ApiKey.class);
			s.addField(ApiKey.PROP_ID);
			s.addFilterEqual(ApiKey.PROP_KEY, key);
			existingKeys = getGeneralDao().search(s);
		} while (!existingKeys.isEmpty());
		
		ApiKey apiKey = new ApiKey(key, user);
		getGeneralDao().save(apiKey);
		return apiKey;
	}
}
