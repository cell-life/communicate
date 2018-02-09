package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Repository("channelDAO")
public class ChannelDAOImpl extends BaseDAOImpl<Channel, Long> implements ChannelDAO {

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Channel> getActiveOutgoingChannels() {
		Search search = new Search(NumberInfo.class);
		search.addFilterEqual(NumberInfo.PROP_VOIDED, false);
		search.addField(NumberInfo.PROP_CHANNEL);
		@SuppressWarnings("unchecked")
		List<Channel> channels = _search(search);
		return channels;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Channel> getChannelsByType(ChannelType type) {
		Search search = new Search(Channel.class);
		search.addFilterEqual(Channel.PROP_TYPE, type);
		List<Channel> channels = search(search);
		return channels;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Channel> getInactiveOutChannels() {
		String queryString = "from Channel where type = :type and id not in " +
				"(select distinct channel.id from NumberInfo where voided = false)";
		Query query = getSession().createQuery(queryString);
		query.setParameter("type", ChannelType.OUT);
		@SuppressWarnings("unchecked")
		List<Channel> channels = query.list();
		return channels;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public Channel getActiveInChannelForShortCode(String shortcode) {
		Search search = new Search(Channel.class);
		search.addFilterEqual(Channel.PROP_TYPE,  ChannelType.IN);
		search.addFilterEqual(Channel.PROP_SHORT_CODE,  shortcode);
		search.addFilterEqual(Channel.PROP_VOIDED, false);
		Channel channel = (Channel) searchUnique(search);
		return channel;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Channel> getActiveInChannels() {
		Search search = new Search(Channel.class);
		search.addFilterEqual(Channel.PROP_TYPE,  ChannelType.IN);
		search.addFilterEqual(Channel.PROP_VOIDED, false);
		List<Channel> channels = search(search);
		return channels;
	}
}
