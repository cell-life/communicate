package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;

import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.pconfig.model.Pconfig;

public class ChannelConfigViewModel implements MobilisrEntity, Serializable {

	private static final long serialVersionUID = -6601514470778342782L;

	private ChannelConfig channelConfig;

	private Pconfig handler;

	private String name;

	public ChannelConfigViewModel() {
	}

	public ChannelConfigViewModel(ChannelConfig channelConfig) {
		this.channelConfig = channelConfig;
		init(channelConfig);
	}

	public ChannelConfig getChannelConfig() {
		if (channelConfig != null) {
			channelConfig.setName(name);
			channelConfig.setHandler(getHandler().getResource());
		}
		return channelConfig;
	}

	@Override
	public Long getId() {
		return channelConfig.getId();
	}

	@Override
	public String getIdentifierString() {
		return channelConfig.getIdentifierString();
	}

	public String getName() {
		return name;
	}

	private void init(ChannelConfig channelConfig) {
		this.name = channelConfig.getName();
	}

	@Override
	public boolean isPersisted() {
		return channelConfig.isPersisted();
	}

	public void setChannelConfig(ChannelConfig channelConfig) {
		this.channelConfig = channelConfig;
		init(channelConfig);
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
}
