package org.celllife.mobilisr.service.security.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class MobilisrPermissionHeirachyImpl implements RoleHierarchy {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public Collection<GrantedAuthority> getReachableGrantedAuthorities(
			Collection<GrantedAuthority> authorities) {
		if (authorities == null || authorities.isEmpty()) {
			return AuthorityUtils.NO_AUTHORITIES;
		}

		Set<GrantedAuthority> reachablePermissions = new HashSet<GrantedAuthority>();
		for (GrantedAuthority authority : authorities) {
			MobilisrPermission perm = MobilisrPermission.safeValueOf(authority
					.getAuthority());
			if (perm == null) {
				reachablePermissions.add(authority);
				log.debug("Unknown permission [{}]", authority);
				continue;
			}

			Collection<MobilisrPermission> implied = perm.getImpliedPermissions(true);
			
			for (MobilisrPermission permission : implied) {
				reachablePermissions.add(new GrantedAuthorityImpl(
						permission.getPrefixedName()));
			}
			
            log.debug("getReachableGrantedAuthorities() - From the permissions {}"
                    + " one can reach {}", authorities, reachablePermissions);
		}
		return reachablePermissions;
	}
}
