package org.celllife.mobilisr.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.celllife.mobilisr.api.validation.MsisdnRule;

@Entity
@Table(name="numberinfo")
public class NumberInfo extends VoidableEntity {

	private static final long serialVersionUID = -617561580942523224L;

	public static final String PROP_NAME = "name";
	public static final String PROP_PREFIX = "prefix";
	public static final String PROP_VALIDATOR = "validator";
	public static final String PROP_CHANNEL = "channel";
	
	@Column(name="name", nullable = false, unique=true, length=100)
	private String name;

	@Column(name="prefix", nullable=false, unique=true, length=4)
	private String prefix;

	@Column(name="validator", nullable=false, length=255)
	private String validator;
	
	@ManyToOne(fetch = FetchType.LAZY, optional=false)
	@JoinColumn(name="channel_id")
	private Channel channel;

	public NumberInfo() {
	}
	
	public NumberInfo(String name, String prefix, String validator, Channel channel) {
		super();
		this.name = name;
		this.prefix = prefix;
		this.validator = validator;
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getValidator() {
		return validator;
	}

	public void setValidator(String validator) {
		this.validator = validator;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}
	
	public MsisdnRule getMsisdnRule(){
		return new MsisdnRule(name, prefix, validator);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NumberInfo [name=").append(name).append(", prefix=")
				.append(prefix).append("]");
		return builder.toString();
	}
}
