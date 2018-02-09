package org.celllife.mobilisr.service.wasp;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.mobilisr.service.impl.BaseServiceImpl;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.util.LogUtil;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Service;

@Service
public class ChannelManagerImpl extends BaseServiceImpl implements ApplicationListener<ApplicationContextEvent>, ChannelManager {
	
	private static final Logger log = LoggerFactory.getLogger(ChannelManagerImpl.class);
	
	@Autowired
	private ChannelDAO channelDao;
	
	@Autowired
	private List<ChannelHandler> channelHandlers;
	
	/**
	 * Map of <channel handler name> -> List<Channel Ids> used to keep track of which channel
	 * handlers are being used by which channel
	 */
	private Map<String, Set<Long>> serviceMap = new ConcurrentHashMap<String, Set<Long>>();
	
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextStartedEvent){
			startChannelServices();
		} else if (event instanceof ContextRefreshedEvent){
			startChannelServices();
		} else if (event instanceof ContextStoppedEvent){
			stopChannelServices();
		} else if (event instanceof ContextClosedEvent){
			stopChannelServices();
		}
	}

	private void startChannelServices() {
		List<Channel> outChannels = channelDao.getActiveOutgoingChannels();
		startServicesForChannels(ChannelType.OUT, outChannels);
		
		List<Channel> inChannels = channelDao.getActiveInChannels();
		startServicesForChannels(ChannelType.IN, inChannels);
	}

	private void startServicesForChannels(ChannelType type, List<Channel> channels) {
		if (channels == null || channels.isEmpty()){
			log.info("No active {} channels configured", type);
		} else {
			log.info("Starting {} channel services", type);
			for (Channel channel : channels) {
				try {
					startServicesForChannel(channel);
				} catch (ChannelStateException e) {
					log.error(LogUtil.getMarker_notifyAdmin(),
							"Error starting / stopping channel services", e);
				}	
			}
		}
	}

	@Override
	public void startServicesForChannel(Channel channel) throws ChannelStateException{
		if (channel == null || channel.getHandler() == null || channel.getHandler().isEmpty()) {
			return;
		}
		
		if (serviceMap.containsKey(channel.getHandler())) {
			log.info("Services for channel [{}] already started", channel.getName());
			addChannelToServiceMap(channel);
			return;
		}
		
		log.info("Starting services for channel [{}]", channel.getName());
		ChannelHandler handler = getHandler(channel.getHandler());
		if (handler == null){
			throw new ChannelStateException("No Channel with name ["
					+ channel.getHandler() + "] found.");
		}
		
		if (!handler.supportsChannelType(channel.getType())){
			throw new ChannelStateException("Channel with name ["
					+ channel.getHandler() + "] does not support type ["
					+ channel.getType() + "]");
		}
		
		try {
			ChannelConfig config = channel.getConfig();
			if (config != null){
				config = getGeneralDao().find(ChannelConfig.class, config.getId());
				String yaml = config.getProperties();
				List<Parameter<?>> params = YamlUtils.loadParameterList(yaml);
				Pconfig pconfig = new Pconfig();
				pconfig.setParameters(params);
				handler.configure(pconfig);
			}
			handler.start();
			
			addChannelToServiceMap(channel);
			log.info("Services for channel [{}] started successfully", channel.getName());
		} catch (Exception e) {
			handler.stop();
			
			channel.setVoided(true);
			channelDao.saveOrUpdate(channel);
			
			log.error("Error starting services for channel: " 
					+ channel.getName(), e);
			throw new ChannelStateException("Error starting services for channel: " 
					+ channel.getName() + "\n " + e.getMessage(), e);
		}
	}
	
	private void addChannelToServiceMap(Channel channel) {
		if (!serviceMap.containsKey(channel.getHandler())){
			serviceMap.put(channel.getHandler(), new HashSet<Long>());
		}
		serviceMap.get(channel.getHandler()).add(channel.getId());
	}

	@Override
	public ChannelHandler getHandler(String handlerName) {
		if (handlerName == null || handlerName.isEmpty()){
			return null;
		}

		for (ChannelHandler handler : channelHandlers) {
			if (handler.getConfigDescriptor().getResource().equals(handlerName)){
				return handler;
			}
		}
		return null;
	}

	@Override
	public void stopServicesForChannel(Channel channel) throws ChannelStateException{
		if (channel == null || channel.getHandler() == null || channel.getHandler().isEmpty()) {
			return;
		}
		
		Set<Long> channels = serviceMap.get(channel.getHandler());
		if (channels != null){
			channels.remove(channel.getId());
			if (channels.isEmpty()){
				stopAllServicesForChannelHandler(channel.getHandler());
			}
		}
	}
	
	private void stopChannelServices() {
		for (String key : serviceMap.keySet()) {
			try {
				stopAllServicesForChannelHandler(key);
			} catch (ChannelStateException e) {
				log.error(LogUtil.getMarker_notifyAdmin(),
						"Error starting / stopping channel services", e);
			}
		}
	}
	
	private void stopAllServicesForChannelHandler(String channelHandler) throws ChannelStateException{
		if (channelHandler == null) {return;}
		
		log.info("Stopping channel service: [{}]", channelHandler);
		ChannelHandler handler = getHandler(channelHandler);
		handler.stop();
		serviceMap.remove(channelHandler);
	}
	
	@Override
	public void stopServicesForUnusedChannels() {
		List<Channel> unusedChannels = channelDao.getInactiveOutChannels();
		for (Channel channel : unusedChannels) {
			try {
				stopServicesForChannel(channel);
				channel.setVoided(true);
				channelDao.saveOrUpdate(channel);
			} catch (ChannelStateException e) {
				log.error(LogUtil.getMarker_notifyAdmin(),
						"Error starting / stopping channel services", e);
			}
		}
	}

	public Map<String, Set<Long>> getServiceMap() {
		return Collections.unmodifiableMap(serviceMap);
	}
	
	public void setChannelHandlers(List<ChannelHandler> channelHandlers) {
		this.channelHandlers = channelHandlers;
	}
	
	@Override
	public List<ChannelHandler> getChannelHandlers() {
		return Collections.unmodifiableList(channelHandlers);
	}

	public void setChannelDao(ChannelDAO channelDao) {
		this.channelDao = channelDao;
	}
}
