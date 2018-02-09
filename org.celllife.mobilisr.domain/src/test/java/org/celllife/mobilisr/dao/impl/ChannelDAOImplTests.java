package org.celllife.mobilisr.dao.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ChannelDAOImplTests extends AbstractDBTest {

	@Autowired
	private ChannelDAO channelDao;
	
	private Set<Long> activeChannels = new HashSet<Long>();
	private Set<Long> unusedChannels = new HashSet<Long>();
	
	@Before
	public void before(){
		List<Channel> all = getGeneralDao().findAll(Channel.class);
		for (Channel channel : all) {
			unusedChannels.add(channel.getId());
		}
		
		// this IN channel should be ignored by the inactive channel query
		Channel channel = new Channel("channel-in1", ChannelType.IN,"handler-in1",null);
		channelDao.save(channel);
		
		String prefix = "2772";
		channel = new Channel("channel1", ChannelType.OUT,"handler1",null);
		channelDao.save(channel);
		getGeneralDao().save(new NumberInfo(prefix, prefix, "1", channel));
		activeChannels.add(channel.getId());
		
		prefix = "2778";
		channel = new Channel("channel2", ChannelType.OUT,"handler2",null);
		channelDao.save(channel);
		getGeneralDao().save(new NumberInfo(prefix, prefix, "2", channel));
		activeChannels.add(channel.getId());

		prefix = "2782";
		channel = new Channel("channel3", ChannelType.OUT,"handler3",null);
		channelDao.save(channel);
		NumberInfo numberInfo = new NumberInfo(prefix, prefix, "3", channel);
		numberInfo.setVoided(true);
		getGeneralDao().save(numberInfo);
		unusedChannels.add(channel.getId());
	}
	
	@Test
	public void testGetActiveOutgoingChannels(){
		List<Channel> channels = channelDao.getActiveOutgoingChannels();
		Assert.assertEquals(activeChannels.size(), channels.size());
		for (Channel channel : channels) {
			Assert.assertTrue(activeChannels.contains(channel.getId()));
		}
	}
	
	@Test
	public void testGetUnusedChannels(){
		List<Channel> channels = channelDao.getInactiveOutChannels();
		Assert.assertEquals(unusedChannels.size(), channels.size());
		for (Channel channel : channels) {
			Assert.assertTrue(unusedChannels.contains(channel.getId()));
		}
	}
	
}
