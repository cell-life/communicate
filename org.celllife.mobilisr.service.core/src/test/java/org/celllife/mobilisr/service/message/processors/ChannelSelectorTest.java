package org.celllife.mobilisr.service.message.processors;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.trg.search.Search;

@RunWith(MockitoJUnitRunner.class)
public class ChannelSelectorTest {

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Mock
	private MobilisrGeneralDAO generalDao;
	private ChannelSelector selector;

	private Map<String, NumberInfo> prefixMap = new HashMap<String, NumberInfo>();

	@Before
	public void setup() {
		selector = new ChannelSelector();
		selector.setGeneralDao(generalDao);

		String prefix = "2";
		Channel channel = new Channel("channel1", ChannelType.OUT, "handler1",
				null);
		channel.setId(1l);
		prefixMap.put(prefix, new NumberInfo(prefix, prefix, "", channel));

		prefix = "27";
		channel = new Channel("channel2", ChannelType.OUT, "handler2", null);
		channel.setId(2l);
		prefixMap.put(prefix, new NumberInfo(prefix, prefix, "", channel));

		prefix = "278";
		channel = new Channel("channel3", ChannelType.OUT, "handler3", null);
		channel.setId(3l);
		prefixMap.put(prefix, new NumberInfo(prefix, prefix, "", channel));
		
		prefix = "279";
		channel = new Channel("channel3", ChannelType.OUT, "handler3", null);
		channel.setId(3l);
		prefixMap.put(prefix, new NumberInfo(prefix, prefix, "", channel));

		when(generalDao.search(any(Search.class))).thenReturn(
				new ArrayList<NumberInfo>(prefixMap.values()));
	}

	/**
	 * This tests both the matching and also the ordering since if 27 is matched
	 * by 2 instead of by 27 the the test will fail
	 */
	@Test
	public void testSelectChannel(){
		for (Entry<String, NumberInfo> px : prefixMap.entrySet()) {
			SmsMt mt = selector.selectChannel(new SmsMt(px.getKey() + "5556666","message","createdfor"));
			collector.checkThat(mt.getChannelId(), is(px.getValue().getChannel().getId()));
		}
	}
	
	@Test
	public void testSelectChannel_noMatchingPrefix(){
		SmsMt mt = selector.selectChannel(new SmsMt("47855556666","message","createdfor"));
		Assert.assertEquals(SmsStatus.QUEUE_FAIL, mt.getStatus());
	}
	
	@Test
	public void testSelectChannel_simulator(){
		selector.simulateMessageSending(true);
		String prefix = prefixMap.keySet().iterator().next();
		SmsMt mt = selector.selectChannel(new SmsMt(prefix + "55556666","message","createdfor"));
		Assert.assertEquals(ChannelSelector.SIMULATOR_CHANNEL, mt.getChannelName());
	}
}
