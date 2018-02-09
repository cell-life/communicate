package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ChannelConfigViewModel;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

/**
 * @author Vikram Bindal (vikram@cell-life.org)
 * @author Simon Kelly (simon@cell-life.org)
 */
public interface ChannelService {

	/**
	 * Retrieve a paged list of all channels in the system
	 * @param loadConfig
	 * @return
	 */
	PagingLoadResult<Channel> listAllChannels(PagingLoadConfig loadConfig);

	/**
	 * Retrieve a paged list of all incoming channels in the system
	 * @param loadConfig
	 * @return
	 */
	List<Channel> listIncomingChannels();

	/**
	 * If channel is active it is deactivated. If it is inactive and type = OUT
	 * then the current active OUT channel is deactivated before this channel is
	 * activated.
	 *
	 * @param channel
	 * @throws ChannelStateException
	 */
	void toggleActiveState(Channel channel) throws ChannelStateException;

	/**
	 * Get an active IN channel with the given short code
	 * 
	 * @param shortcode
	 * @return Channel
	 */
	Channel getActiveInChannelForShortCode(String shortcode);

	int getActiveFilterCountForChannel(Channel channel);

	int getMessageCountForChannel(Channel channel);

	void saveChannelConfig(ChannelConfigViewModel model);

	ChannelConfigViewModel getChannelConfigViewModel(Long channelConfigId);

	List<ChannelConfig> listAllChannelConfigs();

	/**
	 * @return the list of Pconfig objects for all channel handlers.
	 */
	List<Pconfig> getAllChannelHandlerConfigs();

	/**
	 * @return the list of Pconfig objects for channel handlers that require configuration.
	 */
	List<Pconfig> getConfigurableChannelHandlerConfigs();

	List<Pconfig> getChannelHandlerConfigsForType(ChannelType type);

	ChannelViewModel getChannelViewModel(Long channelId);

	void saveChannel(ChannelViewModel model) throws UniquePropertyException;

	List<ChannelConfig> getChannelConfigsForHandler(String handler);

	List<Channel> listOutgoingChannels();

	PagingLoadResult<NumberInfo> listAllNumberInfo(PagingLoadConfig loadConfig);

	void saveNumberInfo(NumberInfo model) throws UniquePropertyException, ChannelStateException;

	List<NumberInfo> getNumberMappingsForChannel(Long channelId);
}
