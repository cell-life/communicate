package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.ChannelService
 */
@RemoteServiceRelativePath("channelService.rpc")
public interface ChannelService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#listAllChannels(PagingLoadConfig)
	 */
	PagingLoadResult<Channel> listAllChannels(PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#listAllNumberInfo(PagingLoadConfig)
	 */
	PagingLoadResult<NumberInfo> listAllNumberInfo(PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#listIncomingChannels()
	 */
	List<Channel> listIncomingChannels() throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#listOutgoingChannels()
	 */
	List<Channel> listOutgoingChannels() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#toggleActiveState(Channel)
	 */
	void toggleActiveState(Channel channel) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getActiveInChannelForShortCode(String)
	 */
	Channel getActiveInChannelForShortCode(String shortcode) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getActiveFilterCountForChannel(Channel)
	 */
	int getActiveFilterCountForChannel(Channel channel) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getMessageCountForChannel(Channel)
	 */
	int getMessageCountForChannel(Channel channel) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getMessageCountForChannel(Channel)
	 */
	void saveChannelConfig(ChannelConfigViewModel model) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getChannelConfigViewModel(Long)
	 */
	ChannelConfigViewModel getChannelConfigViewModel(Long channelConfigId) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#listAllChannelConfigs()
	 */
	List<ChannelConfig> listAllChannelConfigs() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getAllChannelHandlerConfigs()
	 */
	List<Pconfig> getAllChannelHandlerConfigs() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getConfigurableChannelHandlerConfigs()
	 */
	List<Pconfig> getConfigurableChannelHandlerConfigs() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getConfigurableChannelHandlerConfigs()
	 */
	List<Pconfig> getChannelHandlerConfigsForType(ChannelType type)	throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getChannelViewModel(Long)
	 */
	ChannelViewModel getChannelViewModel(Long channelId) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ChannelService#saveChannel(ChannelViewModel)
	 */
	void saveChannel(ChannelViewModel model) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getChannelConfigsForHandler(String)
	 */
	List<ChannelConfig> getChannelConfigsForHandler(String handler) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#saveNumberInfo(NumberInfo)
	 */
	void saveNumberInfo(NumberInfo model) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.ChannelService#getNumberMappingsForChannel(Long)
	 */
	List<NumberInfo> getNumberMappingsForChannel(Long channelId) throws MobilisrException, MobilisrRuntimeException;
}
