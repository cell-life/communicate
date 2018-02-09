package org.celllife.mobilisr.service.security.impl;

import java.util.Collection;
import java.util.HashSet;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class MobilisrPermissionHeirarchyImplTests {

	@Test
	public void testPermissionHeirarchy() {
		MobilisrPermissionHeirachyImpl impl = new MobilisrPermissionHeirachyImpl();
		Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new GrantedAuthorityImpl(
				MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE
						.getPrefixedName()));
		Collection<GrantedAuthority> reachableGrantedAuthorities = impl
				.getReachableGrantedAuthorities(authorities);

		Assert.assertEquals(3, reachableGrantedAuthorities.size());
		Assert.assertTrue(reachableGrantedAuthorities
				.contains(new GrantedAuthorityImpl(
						MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE
								.getPrefixedName())));
		Assert.assertTrue(reachableGrantedAuthorities
				.contains(new GrantedAuthorityImpl(
						MobilisrPermission.ORGANISATIONS_MANAGE.getPrefixedName())));
		Assert.assertTrue(reachableGrantedAuthorities
				.contains(new GrantedAuthorityImpl(
						MobilisrPermission.VIEW_ADMIN_CONSOLE.getPrefixedName())));
	}

}
