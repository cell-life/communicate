package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

/**
 * This object is used to store configurations for channel handlers. e.g. SMPP
 * connection settings. It is separated from the Channel so that multiple
 * channels can share the same configuration.
 * 
 * @author Simon Kelly
 */
@Entity
@Table(name = "channelconfig")
public class ChannelConfig extends VoidableEntity implements PropertyConfig,
		Serializable {

	private static final long serialVersionUID = -7965895934484878836L;

	public static final String PROP_NAME = "name";
	public static final String PROP_HANDLER = "handler";
	public static final String PROP_PROPERTIES = "properties";
	public static final String PROP_CHANNELS = "channels";

	@Column(name = "name", nullable = false, length = 100, unique = true)
	private String name;

	@Column(name = "handler", nullable = false, length = 50)
	private String handler;

	@Column(name = "properties", columnDefinition = "TEXT", nullable = false)
	private String properties;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "config")
	@ForeignKey(name = "fk_channel_channelconfig", inverseName = "fk_channelconfig_channel")
	private List<Channel> channels = new ArrayList<Channel>();

	public ChannelConfig() {
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public String getHandler() {
		return handler;
	}

	public String getName() {
		return name;
	}

	public String getProperties() {
		return properties;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
}
