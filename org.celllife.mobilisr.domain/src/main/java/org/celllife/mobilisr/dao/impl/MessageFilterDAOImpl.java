package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MessageFilterDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Repository("messageFilterDAO")
public class MessageFilterDAOImpl extends BaseDAOImpl<MessageFilter, Long> implements
		MessageFilterDAO {
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public boolean channelFilterExists(Channel thisChannel, String type) {
		Search s = new Search();
		s.addFilterEqual(MessageFilter.PROP_CHANNEL, thisChannel);
		s.addFilterEqual(MessageFilter.PROP_TYPE, type);
		List<Channel> resultList = search(s);		
		return (resultList.size() > 0);
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public List<MessageFilter> getActiveFilters(Channel channel) {
		return getActiveFilters(channel, null, null);
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public List<MessageFilter> getActiveFilters(Channel channel, String type) {
		return getActiveFilters(channel, type, null);
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public List<MessageFilter> getActiveFilters(Channel channel,
			String type, Organization organization) {
		Search s = new Search();
		s.addFilterEqual(MessageFilter.PROP_VOIDED, false);
		s.addFilterEqual(MessageFilter.PROP_ACTIVE, true);
		
		if (channel != null){
			s.addFilterEqual(MessageFilter.PROP_CHANNEL, channel);
		}
		
		if (type != null){
			s.addFilterEqual(MessageFilter.PROP_TYPE, type);
		}
		
		if (organization != null){
			s.addFilterEqual(MessageFilter.PROP_ORGANIZATION, organization);
		}
		
		s.addSort(MessageFilter.PROP_RANK, false);
		return search(s);
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public MessageFilter getFilterByNameAndOrganization(String name,
			Organization organization) {
		Search s = new Search();
		s.addFilterEqual(MessageFilter.PROP_NAME, name);
		s.addFilterEqual(MessageFilter.PROP_ORGANIZATION, organization);
		MessageFilter filter = searchUnique(s);
		return filter;
	}
}
