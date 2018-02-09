package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.MessageFilterDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageFilterDAOImplTests extends AbstractDBTest{

	@Autowired
	private MessageFilterDAO messageFilterDAO;
	private Organization organization;

	@Before
	public void setUp() {
		organization = getGeneralDao().findAll(Organization.class).get(1);
	}
	
	@Test
	public void testChannelFilterExists() throws Exception{
		
		boolean doesChannelFilterExist;
		
		Channel keywordChannel = new Channel("ch1", ChannelType.IN, "mockChannel", "123");
		getGeneralDao().save(keywordChannel);
		MessageFilter thisFilter = new MessageFilter("KeywordFilter", "");
		thisFilter.setName("testFilter");
		thisFilter.setOrganization(organization);
		thisFilter.setChannel(keywordChannel);
		messageFilterDAO.save(thisFilter);
		
		Channel testChannel = new Channel("ch2", ChannelType.IN, "mockChannel", "345");
		getGeneralDao().save(testChannel);
		thisFilter = new MessageFilter("ChannelFilter", "");
		thisFilter.setName("testFilter");
		thisFilter.setOrganization(organization);
		thisFilter.setChannel(testChannel);
		messageFilterDAO.save(thisFilter);
		
		doesChannelFilterExist = messageFilterDAO.channelFilterExists(testChannel, "ChannelFilter");
		Assert.assertTrue(doesChannelFilterExist);	
		
		doesChannelFilterExist = messageFilterDAO.channelFilterExists(keywordChannel, "ChannelFilter");
		Assert.assertFalse(doesChannelFilterExist);
	}
	
	@Test
	public void testGetFiltersofType() {
		
		List<MessageFilter> filtersReturned;
				
		Channel myChannel = new Channel("ch1", ChannelType.IN, "mockChannel", "123");
		getGeneralDao().save(myChannel);
		
		//one channel filter
		MessageFilter thisFilter = new MessageFilter("ChannelFilter", "");
		thisFilter.setName("channel filter");
		thisFilter.setOrganization(organization);
		thisFilter.setChannel(myChannel);
		thisFilter.setActive(true);
		messageFilterDAO.save(thisFilter);

		//two regex filters
		for (int i=1; i<=2; i++)
		{
			thisFilter = new MessageFilter("RegexFilter", "");
			thisFilter.setName("regex filter"+i);
			thisFilter.setOrganization(organization);
			thisFilter.setChannel(myChannel);
			thisFilter.setActive(true);
			messageFilterDAO.save(thisFilter);
		}

		//three keyword filters
		for (int i=1; i<=3; i++)
		{
			thisFilter = new MessageFilter("KeywordFilter", "");
			thisFilter.setName("keyword filter"+i);
			thisFilter.setOrganization(organization);
			thisFilter.setChannel(myChannel);
			thisFilter.setActive(true);
			messageFilterDAO.save(thisFilter);
		}
				
		filtersReturned = messageFilterDAO.getActiveFilters(myChannel, "ChannelFilter");
		Assert.assertEquals(1, filtersReturned.size());
		
		filtersReturned = messageFilterDAO.getActiveFilters(myChannel, "KeywordFilter");
		Assert.assertEquals(3, filtersReturned.size());
		
		filtersReturned = messageFilterDAO.getActiveFilters(myChannel, "RegexFilter");
		Assert.assertEquals(2, filtersReturned.size());
	}

}
