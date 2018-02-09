package org.celllife.mobilisr.domain;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class MobilisrPermissionTest {

	@Test
	public void testImplies_false() {
		Assert.assertFalse(MobilisrPermission.VIEW_ADMIN_CONSOLE
				.implies(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE));
	}
	
	@Test
	public void testImplies_true() {
		Assert.assertTrue(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE
				.implies(MobilisrPermission.VIEW_ADMIN_CONSOLE));
	}
	
	@Test
	public void testImplies_ROLE_ADMIN() {
		Assert.assertTrue(MobilisrPermission.ROLE_ADMIN
				.implies(MobilisrPermission.VIEW_ADMIN_CONSOLE));
	}
	
	@Test
	public void testGetImpliedPermissions() {
		Collection<MobilisrPermission> implied = MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE.getImpliedPermissions(true);

		Assert.assertEquals(3, implied.size());
		Assert.assertTrue(implied.contains(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE));
		Assert.assertTrue(implied.contains(MobilisrPermission.ORGANISATIONS_MANAGE));
		Assert.assertTrue(implied.contains(MobilisrPermission.VIEW_ADMIN_CONSOLE));
	}
	
	@Test
	public void testSafeValueOf(){
		MobilisrPermission testPerm = MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE;
		MobilisrPermission valueOf = MobilisrPermission.safeValueOf(testPerm.name());
		
		Assert.assertEquals(testPerm, valueOf);
	}
	
	@Test
	public void testSafeValueOf_withPrefix(){
		MobilisrPermission testPerm = MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE;
		MobilisrPermission valueOf = MobilisrPermission.safeValueOf(testPerm.getPrefixedName());
		
		Assert.assertEquals(testPerm, valueOf);
	}
	
	@Test
	public void testSafeValueOf_null(){
		MobilisrPermission valueOf = MobilisrPermission.safeValueOf("nonexistant");
		
		Assert.assertNull(valueOf);
	}
}
