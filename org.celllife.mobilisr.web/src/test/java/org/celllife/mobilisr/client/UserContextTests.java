package org.celllife.mobilisr.client;

import java.util.Arrays;
import java.util.List;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserContextTests {

	@Mock
	private OrganizationServiceAsync orgServiceMock;

	@Before
	public void before(){
		UserContext.setOrganizationServiceAsync(orgServiceMock);
		UserContext.setUser(getUser());
	}
	
	@Test
	public void testHasPermission_true() {
		Assert.assertTrue(UserContext.hasPermission(MobilisrPermission.ROLE_ADMIN));
		Assert.assertTrue(UserContext.hasPermission(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE));
	}
	
	
	private User getUser() {
		Organization organization = new Organization();
		organization.setName("orgName");
		User user = new User("firstName", "lastName", "emailAddress", "msisdn",
				"userName", "password", "salt", organization);
		
		Role role1 = new Role();
		role1.setName("role 1");
		List<MobilisrPermission> perms = Arrays.asList(
				MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE);
		role1.setPermissionsList(perms);
		
		Role role2 = new Role();
		role1.setName("role 2");
		perms = Arrays.asList(
				MobilisrPermission.ROLE_ADMIN);
		role2.setPermissionsList(perms);
		
		user.setRoles(Arrays.asList(role1, role2));
		return user;
	}
}

