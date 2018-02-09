package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.celllife.mobilisr.constants.ChannelType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for Channel
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Entity
@Table(name="channel")
public class Channel extends VoidableEntity implements Serializable {

	private static final long serialVersionUID = -8348646830476637688L;

	public static final String PROP_NAME = "name";
	public static final String PROP_TYPE = "type";
	public static final String PROP_HANDLER = "handler";
	public static final String PROP_SHORT_CODE = "shortCode";
	public static final String PROP_DATE_ACTIVATED = "dateActivated";
	public static final String PROP_DATE_DEACTIVATED = "dateDeactivated";
	public static final String PROP_ORGANIZATIONS = "organizations";
	public static final String PROP_FILTERS = "filters";
	public static final String PROP_SMS_LOGS = "smsLogs";
	public static final String PROP_CONFIG = "config";
	
	@Column(name="name", nullable = false, unique = true, length=35)
	@Index(name = "CHANNEL_NAME", columnNames = { "name" })
	private String name;

	@Column(name="type", nullable = false, length=10)
	@Index(name = "CHANNEL_TYPE", columnNames = { "type" })
	@Enumerated(EnumType.STRING)
	private ChannelType type;

	@Column(name="handler", nullable = true, length=30)
	private String handler;

	@Column(name="shortcode")
	@Index(name = "CHANNEL_SHORTCODE", columnNames = { "shortcode" })
	private String shortCode;
	
	@Column(name="dateactivated", nullable = true)
	private Date dateActivated;

	@Column(name="datedeactivated", nullable = true)
	private Date dateDeactivated;
	
	@ManyToOne(fetch = FetchType.LAZY, optional=true)
	@JoinColumn(name="config_id")
	private ChannelConfig config;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "channels")
	private List<Organization> organizations = new ArrayList<Organization>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "channel")
	@ForeignKey(name="fk_channel_trigger_in", inverseName="fk_trigger_channel_in")
	private List<MessageFilter> filters = new ArrayList<MessageFilter>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "channel")
	@ForeignKey(name="fk_channel_smslog", inverseName="fk_smslog_channel")
	private List<SmsLog> smsLogs = new ArrayList<SmsLog>();
	
	public Channel() {
	}

	public Channel(String channelName, ChannelType channelType,
			String channelHandler, String shortCode) {
		super();
		this.name = channelName;
		this.type = channelType;
		this.shortCode = shortCode;
		this.handler = channelHandler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ChannelType getType() {
		return type;
	}

	public void setType(ChannelType type) {
		this.type = type;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}

	public List<MessageFilter> getMessageFilters() {
		return filters;
	}

	public void setMessageFilters(List<MessageFilter> smsTriggers) {
		this.filters = smsTriggers;
	}

	public List<SmsLog> getSmsLogs() {
		return smsLogs;
	}

	public void setSmsLogs(List<SmsLog> smsLogs) {
		this.smsLogs = smsLogs;
	}
	
	public Date getDateActivated() {
		return dateActivated;
	}

	public void setDateActivated(Date dateActivated) {
		this.dateActivated = dateActivated;
	}

	public Date getDateDeactivated() {
		return dateDeactivated;
	}

	public void setDateDeactivated(Date dateDeactivated) {
		this.dateDeactivated = dateDeactivated;
	}
	
	@Override
	public void setVoided(Boolean voided) {
		super.setVoided(voided);
		if (voided) {
			setDateDeactivated(new Date());
		} else {
			setDateActivated(new Date());
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Channel: ").append(name)
			.append(" (").append(shortCode).append(")");
		return builder.toString();
	}

	public void setConfig(ChannelConfig config) {
		this.config = config;
	}

	public ChannelConfig getConfig() {
		return config;
	}
}
