package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name = "messagefilter")
public class MessageFilter extends VoidableEntity implements Serializable,
		HasOrganization, PropertyConfig {

	public static final String PROP_TYPE = "type";
	public static final String PROP_PROPS = "props";
	public static final String PROP_CHANNEL = "channel";
	public static final String PROP_ACTIONS = "actions";
	public static final String PROP_ACTIONS_LABEL = "actionsLabel";
	public static final String PROP_NAME = "name";
	public static final String PROP_RANK = "rank";
	public static final String PROP_ACTIVE = "active";

	private static final long serialVersionUID = 5119416100561534106L;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;
	
	@Column(name = "name", nullable = false, length = 100)
	@Index(name = "FILTER_NAME", columnNames = { "name" })
	private String name;

	@Column(name = "type", nullable = false, length = 20)
	@Index(name = "SMSTRIG_TRIGTYPE", columnNames = { "type" })
	private String type;

	@Column(name = "props")
	private String props;

	@Column(name = "actionslabel")
	private String actionsLabel;

	@Column(name = "rank", nullable = false)
	private int rank;
	
	@Column(nullable = false)
	private boolean active = false;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = false)
	@ForeignKey(name = "fk_messagefilter_channel")
	private Channel channel;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "organization_id", nullable = false)
	@ForeignKey(name = "fk_smstrigger_organization", inverseName = "fk_organization_smstrigger")
	private Organization organization;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "filter")
	@IndexColumn(name = "order_index")
	private List<FilterAction> actions = new ArrayList<FilterAction>();

	public MessageFilter() {
	}

	public MessageFilter(String name, Organization organization) {
		this.name = name;
		this.organization = organization;
	}

	public MessageFilter(String triggerType, String tiggerProps) {
		this.type = triggerType;
		this.props = tiggerProps;
	}

	public List<FilterAction> getActions() {
		return actions;
	}

	public String getActionsLabel() {
		return actionsLabel;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getName() {
		return name;
	}

	public Organization getOrganization() {
		return organization;
	}

	public String getProperties() {
		return getProps();
	}

	public String getProps() {
		return props;
	}

	public int getRank() {
		return rank;
	}

	public String getType() {
		return type;
	}

	public Long getVersion() {
		return version;
	}

	public boolean isActive() {
		return active;
	}

	public void setActions(List<FilterAction> actions) {
		this.actions = actions;
	}

	/**
	 * The actionsLabel gets displayed to the user in the list of MessageFilters
	 * 
	 * @param actionsLabel
	 */
	public void setActionsLabel(String actionsLabel) {
		this.actionsLabel = actionsLabel;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * This is the channel that the filter belongs to.
	 * 
	 * @param channel
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public void setProperties(String properties) {
		setProps(properties);
	}

	/**
	 * The configuration properties for the filter.
	 * 
	 * @param props
	 * 
	 * @see org.celllife.mobilisr.service.filter.Filter
	 */
	public void setProps(String props) {
		this.props = props;
	}

	/**
	 * The rank allows sorting of the filters so that they can be processed in
	 * order
	 * 
	 * @param rank
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * The type of MessageFilter
	 * 
	 * @param type
	 * 
	 * @see org.celllife.mobilisr.service.filter.Filter
	 */
	public void setType(String type) {
		this.type = type;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Filter: ").append(name);
		return builder.toString();
	}
}
