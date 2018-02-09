package org.celllife.mobilisr.service.security.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.security.MobilisrSecurityService;
import org.celllife.mobilisr.service.security.MobilisrUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

/**
 * Default implementation for the ClientSecurityService
 * 
 * @author Vikram Bindal (vikram@cell-life.org)
 * 
 */
@Service("mobilisrSecurityService")
@Transactional(readOnly = true)
public class MobilisrSecurityServiceImpl implements MobilisrSecurityService {

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
			DataAccessException {

		MobilisrUserDetails springUser = null;
		
		if (username.isEmpty()) {
			throw new UsernameNotFoundException("Please enter your username");
		} else {
			Search search = new Search(User.class);
			search.addFilterEqual(User.PROP_USERNAME, username);
			search.addFetch(User.PROP_ORGANIZATION);
			User authenticatedUser = userDAO.searchUnique(search);
			if ((authenticatedUser != null) ) {
				if (authenticatedUser.isVoided() || authenticatedUser.getOrganization().getVoided()){
					throw new DisabledException("Your account has been disabled.");
				}
				
				springUser = new MobilisrUserDetails(authenticatedUser.getUserName(), 
						authenticatedUser.getPassword(), true, true, true, true,
						getUserAuthority(authenticatedUser), authenticatedUser);
				
			} else {
				throw new UsernameNotFoundException("Incorrect username, please try again");
			}
		}

		return springUser;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public UserDetails loadUserByApiKey(String apikey) throws UsernameNotFoundException,
			DataAccessException {

		MobilisrUserDetails springUser = null;
		
		if (apikey.isEmpty()) {
			throw new UsernameNotFoundException("Please enter your username");
		} else {
			Search search = new Search(ApiKey.class);
			search.addFilterEqual(ApiKey.PROP_KEY, apikey);
			ApiKey key = (ApiKey) generalDao.searchUnique(search);
			if ((key != null) ) {
				User authenticatedUser = key.getUser();
				if (key.isVoided() || authenticatedUser.isVoided() || authenticatedUser.getOrganization().getVoided()){
					throw new DisabledException("Your account has been disabled.");
				}
				
				springUser = new MobilisrUserDetails(authenticatedUser.getUserName(), 
						authenticatedUser.getPassword(), true, true, true, true,
						getUserAuthority(authenticatedUser), authenticatedUser);
				
			} else {
				throw new UsernameNotFoundException("Incorrect username, please try again");
			}
		}

		return springUser;
	}

	private Collection<GrantedAuthority> getUserAuthority(User user) {
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

		if (user.isSuperAdmin()){
			assignAllPermissionsToUser(grantedAuthorities);
		} else {
			extractUserPermissions(user, grantedAuthorities);
		}

		// TODO: not sure if this is still needed
		grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_APPLICATION"));
		return grantedAuthorities;
	}

	private void extractUserPermissions(User user, Collection<GrantedAuthority> grantedAuthorities) {
		for (Role role : user.getRoles()) {
			List<MobilisrPermission> permissions = role.getPermissionsList();
			for(MobilisrPermission permission: permissions){
				grantedAuthorities.add(new GrantedAuthorityImpl(permission.getPrefixedName()));
			}			
		}
	}

	private void assignAllPermissionsToUser(Collection<GrantedAuthority> grantedAuthorities) {
		for (MobilisrPermission permission : MobilisrPermission.values()) {
			grantedAuthorities.add(new GrantedAuthorityImpl(permission.getPrefixedName()));
		}
	}

}
