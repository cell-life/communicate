package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.pconfig.model.Pconfig;

public class ChannelViewModel implements MobilisrEntity, Serializable {

	private static final long serialVersionUID = -6601514470778342782L;

	private Channel channel;

	private Pconfig handler;

	private String name;

	private ChannelType type;

	private ChannelConfig config;

	private String shortCode;

	public ChannelViewModel() {
	}

	public ChannelViewModel(Channel channel) {
		this.channel = channel;
		init();
	}

	private void init() {
		this.name = channel.getName();
		this.type = channel.getType();
		this.config = channel.getConfig();
		this.shortCode = channel.getShortCode();
	}

	public Channel getChannel() {
		if (channel != null) {
			if (handler != null) {
				channel.setHandler(handler.getResource());
				channel.setName(name);
				channel.setShortCode(shortCode);
				channel.setConfig(config);
			}
		}
		return channel;
	}

	@Override
	public Long getId() {
		return channel.getId();
	}

	@Override
	public String getIdentifierString() {
		return channel.getIdentifierString();
	}

	public String getName() {
		return name;
	}
	
	public ChannelConfig getConfig(){
		return config;
	}
	
	public void setConfig(ChannelConfig config){
		this.config = config;
	}

	@Override
	public boolean isPersisted() {
		return channel.isPersisted();
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
		init();
	}

	@Override
	public void setId(Long id) {
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHandler(Pconfig handler) {
		this.handler = handler;
	}

	public Pconfig getHandler() {
		return handler;
	}
	
	public ChannelType getType(){
		return type;
	}

	public void setShortCode(String shortcode) {
		this.shortCode = shortcode;
	}

	public String getShortCode() {
		return shortCode;
	}
}
