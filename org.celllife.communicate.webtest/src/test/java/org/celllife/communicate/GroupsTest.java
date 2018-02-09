package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.GroupAddPage;
import org.celllife.communicate.page.GroupsPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.junit.Test;

public class GroupsTest extends AbstractBaseTest {
	
	public GroupsPage goGroups(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin(); // think this needs to be any user
		return hp.goContactsPage().goGroupsPage();		
	}
	
	@Test
	public void testCreateGroup(){
		GroupsPage groupsPage = goGroups();
		GroupAddPage addNewGroupPage = groupsPage.goCreatePage();				
		addNewGroupPage.fillForm("my test", "this is a test group");
		groupsPage = addNewGroupPage.save();
		
		String successMessage = groupsPage.getSuccessMessage();
		Assert.assertEquals("ContactGroup: 'my test' saved successfully", successMessage);
	}
	
	@Test
	public void testDeleteGroup() {
		
		ContactGroup contactGroup = db().createTestEntity(ContactGroup.class);
		contactGroup.setGroupName("test del");
		contactGroup.setGroupDescription("test desc");		
		Organization org = db().getAdminOrganization();
		contactGroup.setOrganization(org);
		db().saveOrgUpdate(contactGroup);
		
		GroupsPage groupsPage = goGroups();		
		GroupsPage groupsPage2 = groupsPage.deleteGroup(contactGroup);
		Assert.assertFalse(groupsPage2.isInList(contactGroup.getId()));
		
	}
	
}