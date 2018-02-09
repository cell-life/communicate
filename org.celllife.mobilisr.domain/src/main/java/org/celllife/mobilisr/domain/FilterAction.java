package org.celllife.mobilisr.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="filteraction")
public class FilterAction extends AbstractBaseEntity implements Serializable, PropertyConfig {

	private static final long serialVersionUID = 7498122042599760966L;
	
	public static final String PROP_TYPE = "type";
	public static final String PROP_PROPS = "props";
	public static final String PROP_FILTER = "filter";

	@Column(name="type",nullable = false)
	private String type;
	
	@Column(name="props", columnDefinition="TEXT")
	private String props;

	@Version
	@Column(nullable = false)
	private Long version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="filter_id", nullable = false)
	@ForeignKey(name="fk_action_filter",inverseName="fk_filter_action")
	private MessageFilter filter;

	public FilterAction() {
	}

	public FilterAction(String type, String props) {
		this.type = type;
		this.props = props;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public MessageFilter getFilter() {
		return filter;
	}

	public void setFilter(MessageFilter filter) {
		this.filter = filter;
	}

	public String getProperties() {
		return getProps();
	}

	public void setProperties(String properties) {
		setProps(properties);
	}
}
