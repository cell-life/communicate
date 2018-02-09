package org.celllife.mobilisr.service.wasp;

import java.util.List;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.service.exception.ChannelStateException;



public interface ChannelManager {

	void startServicesForChannel(Channel channel) throws ChannelStateException;

	void stopServicesForChannel(Channel channel) throws ChannelStateException;

	List<ChannelHandler> getChannelHandlers();

	ChannelHandler getHandler(String handlerName);

	void stopServicesForUnusedChannels();

}