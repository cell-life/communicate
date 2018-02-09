package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;

public interface ChannelDAO extends BaseDAO<Channel, Long> {

	/**
	 * Get all Channels that are assigned to an active NumberInfo
	 * 
	 * @return List of Channels
	 */
	List<Channel> getActiveOutgoingChannels();

	/**
	 * Get the active Channel for the shortcode
	 * 
	 * @param shortcode
	 * @return the Channel or null
	 */
	Channel getActiveInChannelForShortCode(String shortcode);

	/**
	 * Get all active IN channels
	 * 
	 * @return List of channels
	 */
	List<Channel> getActiveInChannels();

	/**
	 * Get all OUT Channels that are not assigned to any NumberInfos
	 * 
	 * @return List of Channels
	 */
	List<Channel> getInactiveOutChannels();

	List<Channel> getChannelsByType(ChannelType out);
}
