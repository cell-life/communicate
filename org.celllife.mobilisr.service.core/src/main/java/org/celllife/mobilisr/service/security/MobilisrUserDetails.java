package org.celllife.mobilisr.service.security;

import java.util.Collection;

import org.celllife.mobilisr.domain.User;
import org.springframework.security.core.GrantedAuthority;

public class MobilisrUserDetails extends org.springframework.security.core.userdetails.User {

	private User user;
	private String salt;
	
	private static final long serialVersionUID = -7202476368518960136L;

	public MobilisrUserDetails(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked, Collection<GrantedAuthority> authorities, org.celllife.mobilisr.domain.User user) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
		this.user = user;
		setSalt(user.getSalt());
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public User getUser() {
		return user;
	}
	
	@Override
	public boolean equals(Object rhs) {
		return super.equals(rhs);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
