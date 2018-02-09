package org.celllife.mobilisr.service.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MobilisrSecurityService extends UserDetailsService{

	UserDetails loadUserByApiKey(String apikey)
			throws UsernameNotFoundException, DataAccessException;

	
}
