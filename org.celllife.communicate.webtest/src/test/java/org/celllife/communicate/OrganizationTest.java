package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.AdminPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.OrganizationEditPage;
import org.celllife.communicate.page.OrganizationPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;
import org.junit.Test;

public class OrganizationTest extends AbstractBaseTest {

	public OrganizationPage goOrganisation(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAs("admin", "admin");
		AdminPage adminPage = hp.goAdminPage();
		return adminPage.goOrganizationPage();
	}

	@Test
	public void testAddOrganization(){
		OrganizationPage organizationPage = goOrganisation();
		OrganizationEditPage orgCreatePage = organizationPage.goCreatePage();

		String orgName = "Selenium Org";
		orgCreatePage.fillForm(orgName, "A server nearby ..", "QA Person", "27741862863",
				"dave@cell-life.org", "100");

		organizationPage = orgCreatePage.submit();
		String refString = "Organisation: '" + orgName + "' saved successfully";
		String successMessage = organizationPage.getSuccessMessage();

		Assert.assertEquals(refString, successMessage);

		boolean exists = db().checkEntityExists(Organization.class, Organization.PROP_NAME, orgName);
		Assert.assertTrue(exists);
	}

	@Test
	public void testVoidOrganization(){
		Organization org = db().createTestEntity(Organization.class);
		OrganizationPage organizationPage = goOrganisation().clickVoidButton(org.getId());
		boolean organizationInList = organizationPage.isInList(org.getId());
		Assert.assertFalse(organizationInList);

		org = db().getUniqueEntity(Organization.class, Organization.PROP_ID, org.getId());
		Assert.assertTrue(org.isVoided());
	}

	@Test
	public void testUnVoidOrganization(){
		Organization org = db().createTestEntity(Organization.class);
		org.setVoided(true);
		db().saveOrgUpdate(org);

		OrganizationPage organizationPage = goOrganisation().showVoided()
				.clickUnvoidButton(org.getId());
		boolean organizationInList = organizationPage.isInList(org.getId());
		Assert.assertFalse(organizationInList);

		org = db().getUniqueEntity(Organization.class, Organization.PROP_ID, org.getId());
		Assert.assertFalse(org.isVoided());
	}

	@Test
	public void testVoidAdminOrganization(){
		Organization adminOrg = db().getUniqueEntity(Organization.class, Organization.PROP_NAME, "Admin organisation");

		OrganizationPage organizationPage = goOrganisation().clickVoidButton(adminOrg.getId());
		String messageBoxText = organizationPage.getMessageBoxText();
		Assert.assertNotNull("Expected error dialog",messageBoxText);
	}

	@Test
	public void testVoidOrganizationWithActiveCampaign(){
		Campaign campaign = db().createTestEntity(Campaign.class);
		campaign.setStatus(CampaignStatus.ACTIVE);
		db().saveOrgUpdate(campaign);

		Long orgId = campaign.getOrganization().getId();

		OrganizationPage page = goOrganisation().clickVoidButton(orgId);
		String messageBoxText = page.getMessageBoxText();
		Assert.assertNotNull("Expected error dialog",messageBoxText);
	}


}
