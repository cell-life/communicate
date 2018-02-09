package org.celllife.mobilisr.domain;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public interface MobilisrEntity extends BeanModelTag, Serializable {

	public abstract Long getId();

	public abstract void setId(Long id);

	public abstract String getIdentifierString();
	
	/**
	 * @return true if the entity has been persisted i.e. has a non-null ID
	 */
	public abstract boolean isPersisted();
}
