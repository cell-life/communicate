package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;

public interface MessageFilterDAO extends BaseDAO<MessageFilter, Long> {
	
	public boolean channelFilterExists(Channel thisChannel, String type);
	
	public List<MessageFilter> getActiveFilters(Channel channel);

	public List<MessageFilter> getActiveFilters(Channel channel, String type);
	
	public List<MessageFilter> getActiveFilters(Channel channel, String type, Organization organization);

	public MessageFilter getFilterByNameAndOrganization(String name, Organization organization);


}
