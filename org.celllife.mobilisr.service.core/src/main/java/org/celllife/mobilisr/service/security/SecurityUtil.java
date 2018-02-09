package org.celllife.mobilisr.service.security;

import java.util.ArrayList;
import java.util.Collection;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class SecurityUtil {
	
	private Authentication authentication;

	public void performSystemLogin(){
		authentication = SecurityContextHolder.getContext().getAuthentication();

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new GrantedAuthorityImpl(MobilisrPermission.ROLE_SYSTEM.name()));
		
		User user = new User("Communicate", "", true, true, true, true, grantedAuthorities);
		
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
				user, "",
				user.getAuthorities());
		
		SecurityContextHolder.getContext().setAuthentication(result);
	}
	
	public void clearSystemLogin(){
		SecurityContextHolder.getContext().setAuthentication(authentication);
		authentication = null;
	}

}
