package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.CreateFilterPage;
import org.celllife.communicate.page.FilterInboxPage;
import org.celllife.communicate.page.FilterPage;
import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.dao.hibernate.GeneralDAO;

public class AdminFilterTest extends AbstractBaseTest {

	@Autowired
	private GeneralDAO dao;
	
	public FilterPage goAdminFilters(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goAdminPage().goAdminFiltersPage();
	}
	
	@Test
	public void testCreateNewFilter(){	
		CreateFilterPage newFilterPage = goAdminFilters().createNewFilter();
		newFilterPage.fillForm("Admin organisation", "webtest", "Simulation incoming", "Match all filter");
		FilterPage filterPage = newFilterPage.clickSubmit();
		
		String message = filterPage.getSuccessMessage();
		Assert.assertEquals("Successfully saved filter: webtest", message); 
		
		MessageFilter filter = db().getUniqueEntity(MessageFilter.class, MessageFilter.PROP_NAME, "webtest");
		Assert.assertTrue(filterPage.isInList(filter.getId()));
	}
	
	@Test
	public void testDeactivateFilter(){		
		Channel chIn = new Channel("Test Channel", ChannelType.IN, "test_channel", "11111");
		dao.save(chIn);
		
		Organization org = db().getAdminOrganization();
		MessageFilter messageFilter = new MessageFilter("webtest", org);
		messageFilter.setChannel(chIn);
		messageFilter.setType("Keyword filter");
		dao.save(messageFilter);		
			
		MessageFilter filter = db().getUniqueEntity(MessageFilter.class, MessageFilter.PROP_NAME, "webtest");
		FilterPage newFilterPage = goAdminFilters();
		
		newFilterPage.deactivateFilter(filter.getId());		
		MessageFilter testFilter = db().getUniqueEntity(MessageFilter.class, MessageFilter.PROP_NAME, "webtest");
		Assert.assertTrue(testFilter.getVoided());
		
		newFilterPage.activateFilter(filter.getId());
		MessageFilter testFilter2 = db().getUniqueEntity(MessageFilter.class, MessageFilter.PROP_NAME, "webtest");
		Assert.assertFalse(testFilter2.getVoided());		
		
	}
	
	@Test
	public void testInboxPage(){
		Channel chIn = new Channel("Test Channel", ChannelType.IN, "test_channel", "11111");
		dao.save(chIn);
		
		Organization org = db().getAdminOrganization();
		MessageFilter messageFilter = new MessageFilter("webtest", org);
		messageFilter.setChannel(chIn);
		messageFilter.setType("Keyword filter");
		dao.save(messageFilter);		
			
		MessageFilter filter = db().getUniqueEntity(MessageFilter.class, MessageFilter.PROP_NAME, "webtest");
		
		FilterInboxPage newInboxPage = goAdminFilters().viewFilters(filter.getId());
		Assert.assertTrue(newInboxPage.isFilterInboxPage());		
		
	}
	
}