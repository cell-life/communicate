package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.ChannelConfigViewModel;
import org.celllife.mobilisr.service.gwt.ChannelService;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class ChannelServiceImpl extends AbstractMobilisrService implements
		ChannelService {

	private static final long serialVersionUID = 9110952201266943216L;
	private org.celllife.mobilisr.service.ChannelService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.ChannelService) getBean("channelService");
	}

	@Override
	public PagingLoadResult<Channel> listAllChannels(PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException {
		return service.listAllChannels(loadConfig);
	}

	@Override
	public PagingLoadResult<NumberInfo> listAllNumberInfo(PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException {
		return service.listAllNumberInfo(loadConfig);
	}

	@Override
	public List<Channel> listIncomingChannels() {
		return service.listIncomingChannels();
	}
	
	@Override
	public List<Channel> listOutgoingChannels() {
		return service.listOutgoingChannels();
	}

	@Override
	public void toggleActiveState(Channel channel) throws MobilisrException, MobilisrRuntimeException {
		service.toggleActiveState(channel);
	}

	@Override
	public Channel getActiveInChannelForShortCode(String shortcode) {
		return service.getActiveInChannelForShortCode(shortcode);
	}

	@Override
	public int getActiveFilterCountForChannel(Channel channel) {
		return service.getActiveFilterCountForChannel(channel);
	}
	
	@Override
	public int getMessageCountForChannel(Channel channel){
		return service.getMessageCountForChannel(channel);
	}

	@Override
	public void saveChannelConfig(ChannelConfigViewModel model)
			throws MobilisrException, MobilisrRuntimeException {
		service.saveChannelConfig(model);
	}

	@Override
	public ChannelConfigViewModel getChannelConfigViewModel(Long channelConfigId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.getChannelConfigViewModel(channelConfigId);
	}

	@Override
	public List<ChannelConfig> listAllChannelConfigs()
			throws MobilisrException, MobilisrRuntimeException {
		return service.listAllChannelConfigs();
	}
	
	@Override
	public List<ChannelConfig> getChannelConfigsForHandler(String handler)
			throws MobilisrException, MobilisrRuntimeException {
		return service.getChannelConfigsForHandler(handler);
	}

	@Override
	public List<Pconfig> getAllChannelHandlerConfigs()
			throws MobilisrException, MobilisrRuntimeException {
		return service.getAllChannelHandlerConfigs();
	}

	@Override
	public List<Pconfig> getConfigurableChannelHandlerConfigs()
			throws MobilisrException, MobilisrRuntimeException {
		return service.getConfigurableChannelHandlerConfigs();
	}
	
	@Override
	public List<Pconfig> getChannelHandlerConfigsForType(ChannelType type)
		throws MobilisrException, MobilisrRuntimeException {
		return service.getChannelHandlerConfigsForType(type);
	}

	@Override
	public ChannelViewModel getChannelViewModel(Long channelId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.getChannelViewModel(channelId);
	}

	@Override
	public void saveChannel(ChannelViewModel model) throws MobilisrException,
			MobilisrRuntimeException {
		service.saveChannel(model);
	}
	
	@Override
	public void saveNumberInfo(NumberInfo model) throws MobilisrException,
			MobilisrRuntimeException {
		service.saveNumberInfo(model);
	}
	
	@Override
	public List<NumberInfo> getNumberMappingsForChannel(Long channelId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.getNumberMappingsForChannel(channelId);
	}
}
