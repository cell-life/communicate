package org.celllife.mobilisr.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import net.sf.gilead.pojo.gwt.LightEntity;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.GenericGenerator;


/**
 * Domain class for Base
 *
 * @author Vikram Bindal
 *
 */
@MappedSuperclass
public abstract class AbstractBaseEntity extends LightEntity implements MobilisrEntity {

	private static final long serialVersionUID = 9213657450891883766L;

	public static final String PROP_ID = "id";

	@Id
	@GenericGenerator(name = "unique_id", strategy = "native")
	@GeneratedValue(generator = "unique_id")
	// @see http://community.jboss.org/wiki/HibernateFAQ-TipsAndTricks#How_can_I_retrieve_the_identifier_of_an_associated_object_without_fetching_the_association
	@AccessType("property")
	@Column(unique = true, nullable = false, length = 35)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifierString(){
		return getIdentifierString(getId());
	}

	public String getIdentifierString(Long id){
		return this.getClass().getName() + ":" + id;
	}

	public boolean isPersisted(){
		return id != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBaseEntity other = (AbstractBaseEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getIdentifierString();
	}
}
