package org.celllife.mobilisr.service.wasp;

import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Set;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.pconfig.model.Pconfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChannelManagerImplTests {
	
	private ChannelManagerImpl channelManager;
	
	@Mock
	private MobilisrGeneralDAO generalDao;

	@Mock
	private ChannelDAO channelDao;

	@Before
	public void setup(){
		channelManager = new ChannelManagerImpl();
		channelManager.setGeneralDao(generalDao);
		channelManager.setChannelDao(channelDao);
		channelManager.setChannelHandlers(Arrays.asList(new ChannelHandler[]{
				new CommunicateHttpInHandler()
		}));
		
		when(generalDao.find(eq(ChannelConfig.class), any(Long.class)))
				.thenReturn(new ChannelConfig());
	}
	
	@Test
	public void testStartServicesForChannel() throws ChannelStateException{
		Channel channel = getChannel();
		channelManager.startServicesForChannel(channel);
		Assert.assertTrue(channelManager.getServiceMap().containsKey(channel.getHandler()));
	}
	
	@Test
	public void testStartServicesForChannel_startTwice() throws ChannelStateException{
		Channel channel = getChannel();
		channelManager.startServicesForChannel(channel);
		
		Set<Long> channelsForHandler = channelManager.getServiceMap().get(channel.getHandler());
		Assert.assertNotNull(channelsForHandler);
		Assert.assertEquals(1, channelsForHandler.size());
		Assert.assertTrue(channelsForHandler.contains(channel.getId()));
		
		channelManager.startServicesForChannel(channel);
		channelsForHandler = channelManager.getServiceMap().get(channel.getHandler());
		Assert.assertNotNull(channelsForHandler);
		Assert.assertEquals(1, channelsForHandler.size());
		Assert.assertTrue(channelsForHandler.contains(channel.getId()));
	}
	
	@Test
	public void testStartServicesForChannel_startTwo() throws ChannelStateException{
		Channel channel = getChannel();
		channelManager.startServicesForChannel(channel);
		
		Set<Long> channelsForHandler = channelManager.getServiceMap().get(channel.getHandler());
		Assert.assertNotNull(channelsForHandler);
		Assert.assertEquals(1, channelsForHandler.size());
		Assert.assertTrue(channelsForHandler.contains(channel.getId()));
		
		channel.setId(19L);
		channelManager.startServicesForChannel(channel);
		channelsForHandler = channelManager.getServiceMap().get(channel.getHandler());
		Assert.assertNotNull(channelsForHandler);
		Assert.assertEquals(2, channelsForHandler.size());
		Assert.assertTrue(channelsForHandler.contains(channel.getId()));
	}
	
	@Test(expected=ChannelStateException.class)
	public void testStartServicesForChannel_noChannel() throws ChannelStateException{
		Channel channel = getChannel();
		channel.setHandler("nonexistent");
		channelManager.startServicesForChannel(channel);
	}
	
	@Test(expected=ChannelStateException.class)
	public void testStartServicesForChannel_wrongType() throws ChannelStateException{
		Channel channel = getChannel();
		channel.setType(ChannelType.OUT);
		channelManager.startServicesForChannel(channel);
	}
	
	@Test(expected=ChannelStateException.class)
	public void testStartServicesForChannel_configFail() throws ChannelStateException{
		channelManager.setChannelHandlers(Arrays.asList(new ChannelHandler[]{
				new CommunicateHttpInHandler(){
					public void configure(Pconfig config) {
						throw new IllegalArgumentException();
					};
				}
		}));
		
		Channel channel = getChannel();
		channelManager.startServicesForChannel(channel);
		
		verify(channelDao).saveOrUpdate(argThat(new ArgumentMatcher<Channel>() {
			@Override
			public boolean matches(Object argument) {
				return ((Channel)argument).isVoided();
			}
		}));
	}
	
	@Test
	public void testStopServicesForChannel() throws ChannelStateException{
		testStartServicesForChannel();
		Channel channel = getChannel();
		channelManager.stopServicesForChannel(channel);
		Assert.assertFalse(channelManager.getServiceMap().containsKey(channel.getHandler()));
	}
	
	@Test
	public void testStopServicesForChannel_notStarted() throws ChannelStateException{
		channelManager.stopServicesForChannel(getChannel());
	}

	@Test
	public void testStopServicesForChannel_multiple() throws ChannelStateException{
		testStartServicesForChannel_startTwo();
		Channel channel = getChannel();
		channel.setId(19L);
		channelManager.stopServicesForChannel(channel);
		
		Set<Long> channelsForHandler = channelManager.getServiceMap().get(channel.getHandler());
		Assert.assertNotNull(channelsForHandler);
		Assert.assertEquals(1, channelsForHandler.size());
		Assert.assertFalse(channelsForHandler.contains(channel.getId()));
		Assert.assertTrue(channelsForHandler.contains(getChannel().getId()));
	}
	
	public Channel getChannel(){
		Pconfig pconfig = new CommunicateHttpInHandler().getConfigDescriptor();
		Channel channel = new Channel("test channel", ChannelType.IN,
				pconfig.getResource(), "1235");
		channel.setConfig(new ChannelConfig());
		channel.setId(17L);
		return channel;
	}

}
