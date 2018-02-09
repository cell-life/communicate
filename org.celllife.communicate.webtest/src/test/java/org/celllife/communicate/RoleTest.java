package org.celllife.communicate;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.RolePage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;
import org.junit.Test;

public class RoleTest extends AbstractBaseTest {
	
	public RolePage goRoles(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goAdminPage().goRolePage();
	}
	
	@Test
	public void testDeleteRole(){
		Role role = db().createTestEntity(Role.class);
		
		RolePage page = goRoles().deleteRole(role.getId());
		boolean isInList = page.isInList(role.getId());
		Assert.assertFalse(isInList);
		
		role = db().getUniqueEntity(Role.class, role.getId());
		Assert.assertNull(role);
	}
	
	@Test
	public void testDeleteRole_assignedToUser(){
		Role role = db().createTestEntity(Role.class);
		role.setUsers(Arrays.asList(db().getAdminUser()));
		db().saveOrgUpdate(role);
		
		RolePage page = goRoles().deleteRoleExpectConfirmation(role.getId());
		boolean isInList = page.isInList(role.getId());
		Assert.assertFalse(isInList);
		
		role = db().getUniqueEntity(Role.class, role.getId());
		Assert.assertNull(role);
	}
	
	@Test
	public void testCreateRole(){
		String name = "test role";
		RolePage page = goRoles().createRole(name, MobilisrPermission.CAMPAIGNS_CREATE);
		
		Role role = db().getUniqueEntity(Role.class, Role.PROP_NAME, name);
		Assert.assertNotNull(role);
		
		boolean isInList = page.isInList(role.getId());
		Assert.assertTrue(isInList);
		
		List<MobilisrPermission> permissionsList = role.getPermissionsList();
		Assert.assertEquals(1, permissionsList.size());
		Assert.assertTrue(permissionsList.contains(
				MobilisrPermission.CAMPAIGNS_CREATE));
	}

}
