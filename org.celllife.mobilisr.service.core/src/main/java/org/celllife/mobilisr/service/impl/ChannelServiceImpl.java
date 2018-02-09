package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.ChannelService;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ChannelConfigViewModel;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;
import org.celllife.mobilisr.service.message.processors.ChannelSelector;
import org.celllife.mobilisr.service.wasp.ChannelHandler;
import org.celllife.mobilisr.service.wasp.ChannelManager;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.util.PconfigUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;

@Service("channelService")
public class ChannelServiceImpl extends BaseServiceImpl implements ChannelService {

	private static final Logger log = LoggerFactory.getLogger(ChannelServiceImpl.class);
	
	@Autowired
	private ChannelDAO channelDAO;

	@Autowired
	private ChannelManager channelManager;
	
	@Autowired
	private ChannelSelector channelSelector;
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_CHANNELS_VIEW"})
	public PagingLoadResult<Channel> listAllChannels(PagingLoadConfig loadConfig) {
		return getEntityList(null, Channel.class, loadConfig, true);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<Channel> listIncomingChannels() {
		return channelDAO.getActiveInChannels();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<Channel> listOutgoingChannels() {
		return channelDAO.getChannelsByType(ChannelType.OUT);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_CHANNELS_IN_START_STOP","PERM_CHANNELS_OUT_START_STOP"})
	public void toggleActiveState(Channel channel) throws ChannelStateException{
		if (ChannelType.OUT == channel.getType()){
			throw new IllegalArgumentException("Only IN channels can be activated / deactivated");
		}
		
		if (!channel.isVoided()){
			log.info("Deactivating channel: {}", channel.getName());
			channel.setVoided(true);
			channelDAO.saveOrUpdate(channel);
			channelManager.stopServicesForChannel(channel);
		} else {
			Channel clashingChannel = channelDAO.getActiveInChannelForShortCode(channel.getShortCode());
			if (clashingChannel != null){
				throw new ChannelStateException("You cannot activate this channel" +
						" as there is another active IN channel with the same shortcode.");
			}

			log.info("Activating channel: {}", channel.getName());
			channel.setVoided(false);
			channelDAO.saveOrUpdate(channel);
			channelManager.startServicesForChannel(channel);
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public Channel getActiveInChannelForShortCode(String shortcode) {
		return channelDAO.getActiveInChannelForShortCode(shortcode);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_CHANNELS_IN_EDIT","PERM_CHANNELS_OUT_EDIT"})
	public void saveChannel(ChannelViewModel model) throws UniquePropertyException{
		Channel channel = model.getChannel();
		try {
			channelDAO.saveOrUpdate(channel);
		} catch (ConstraintViolationException e) {
			throw new UniquePropertyException("Another channel already exists with the name '"
					+ channel.getName() + "'");
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public int getActiveFilterCountForChannel(Channel channel){
		Search search = new Search(MessageFilter.class);
		search.addFilterEqual(MessageFilter.PROP_CHANNEL, channel);
		search.addFilterEqual(MessageFilter.PROP_VOIDED, false);
		return getGeneralDao().count(search);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public int getMessageCountForChannel(Channel channel){
		Search search = new Search(SmsLog.class);
		search.addFilterEqual(SmsLog.PROP_CHANNEL, channel);
		return getGeneralDao().count(search);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<ChannelConfig> listAllChannelConfigs(){
		return getGeneralDao().findAll(ChannelConfig.class);
	}
	
	@SuppressWarnings("unchecked")
	@Loggable(LogLevel.TRACE)
	@Override
	public List<ChannelConfig> getChannelConfigsForHandler(String handler){
		Search search = new Search(ChannelConfig.class);
		search.addFilterEqual(ChannelConfig.PROP_HANDLER, handler);
		return getGeneralDao().search(search);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public ChannelViewModel getChannelViewModel(Long channelId){
		Search search = new Search(Channel.class);
		search.addFilterEqual(Channel.PROP_ID, channelId);
		search.addFetch(Channel.PROP_CONFIG);
		Channel channel = channelDAO.searchUnique(search);
		ChannelViewModel viewModel = new ChannelViewModel(channel);
		
		String handlerName = channel.getHandler();
		ChannelHandler handler = channelManager.getHandler(handlerName);
		if (handler != null){
			Pconfig handlerDescriptor = handler.getConfigDescriptor();
			viewModel.setHandler(handlerDescriptor);
		}
		return viewModel;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public ChannelConfigViewModel getChannelConfigViewModel(Long channelConfigId){
		ChannelConfig config = getGeneralDao().find(ChannelConfig.class, channelConfigId);
		ChannelConfigViewModel viewModel = new ChannelConfigViewModel(config);
		
		String handlerName = config.getHandler();
		ChannelHandler handler = channelManager.getHandler(handlerName);
		Pconfig handlerDescriptor = handler.getConfigDescriptor();
		
		String properties = config.getProperties();
		List<Parameter<?>> parameterList = YamlUtils.loadParameterList(properties);
		
		PconfigUtils.merge(handlerDescriptor, parameterList);
		
		viewModel.setHandler(handlerDescriptor);
		return viewModel;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_CHANNEL_CONFIG_MANAGE"})
	public void saveChannelConfig(ChannelConfigViewModel model){
		ChannelConfig config = model.getChannelConfig();
		Pconfig handler = model.getHandler();
		String props = YamlUtils.dumpParameterList(handler.getParameters());
		config.setProperties(props);
		getGeneralDao().saveOrUpdate(config);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<Pconfig> getAllChannelHandlerConfigs(){
		List<Pconfig> configs = new ArrayList<Pconfig>();
		List<ChannelHandler> handlers = channelManager.getChannelHandlers();
		for (ChannelHandler handler : handlers) {
			Pconfig pconfig = handler.getConfigDescriptor();
			configs.add(pconfig);
		}
		return configs;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<Pconfig> getConfigurableChannelHandlerConfigs(){
		List<Pconfig> configs = getAllChannelHandlerConfigs();
		Iterator<Pconfig> iterator = configs.iterator();
		while(iterator.hasNext()){
			Pconfig next = iterator.next();
			if (next.getParameters() == null || next.getParameters().isEmpty()){
				iterator.remove();
			}
		}
		return configs;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<Pconfig> getChannelHandlerConfigsForType(ChannelType type){
		List<Pconfig> configs = new ArrayList<Pconfig>();
		List<ChannelHandler> handlers = channelManager.getChannelHandlers();
		for (ChannelHandler handler : handlers) {
			if (handler.supportsChannelType(type)){
				Pconfig pconfig = handler.getConfigDescriptor();
				configs.add(pconfig);
			}
		}
		return configs;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_NUMBER_INFO_VIEW"})
	public PagingLoadResult<NumberInfo> listAllNumberInfo(PagingLoadConfig loadConfig) {
		return getEntityList(null, NumberInfo.class, loadConfig, true, new SearchModifier() {
			@Override
			public void modify(Search search) {
				search.addFetch(NumberInfo.PROP_CHANNEL);
			}
		});
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_NUMBER_INFO_EDIT"})
	public void saveNumberInfo(NumberInfo model) throws UniquePropertyException, ChannelStateException{
		Search s = new Search(NumberInfo.class);
		s.addFilterEqual(NumberInfo.PROP_NAME, model.getName());
		if (model.isPersisted())
			s.addFilterNotEqual(NumberInfo.PROP_ID, model.getId());
		int duplicateName = getGeneralDao().count(s);
		if (duplicateName > 0){
			throw new UniquePropertyException("The name '"
					+ model.getName() + "' is already in use.");
		}
		
		s = new Search(NumberInfo.class);
		s.addFilterEqual(NumberInfo.PROP_PREFIX, model.getPrefix());
		if (model.isPersisted())
			s.addFilterNotEqual(NumberInfo.PROP_ID, model.getId());
		int duplicatePrefix = getGeneralDao().count(s);
		if (duplicatePrefix > 0){
			throw new UniquePropertyException("The prefix '"
					+ model.getPrefix() + "' is already in use.");
		}
		
		getGeneralDao().saveOrUpdate(model);
		if (!model.isVoided()) {
			Channel channel = model.getChannel();
			channelManager.startServicesForChannel(channel);
			if (channel.isVoided()) {
				channel.setVoided(false);
				getGeneralDao().saveOrUpdate(channel);
			}
		}
		
		channelManager.stopServicesForUnusedChannels();
		channelSelector.reset();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<NumberInfo> getNumberMappingsForChannel(Long channelId){
		Search s = new Search(NumberInfo.class);
		s.addFilterEqual(NumberInfo.PROP_VOIDED, false);
		s.addFilterEqual(NumberInfo.PROP_CHANNEL+"."+Channel.PROP_ID, channelId);
		@SuppressWarnings("unchecked")
		List<NumberInfo> list = getGeneralDao().search(s);
		return list;
	}
}
