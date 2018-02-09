package org.celllife.mobilisr.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Index;

@MappedSuperclass
public abstract class VoidableEntity extends AbstractBaseEntity implements Voidable{

	private static final long serialVersionUID = 1658476191095682723L;
	
	@Column(nullable = false)
	@Index(name = "voided", columnNames = { "voided" })
	private Boolean voided = false;
	
	public VoidableEntity() {
		super();
	}
	
	@Override
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	@Override
	public Boolean getVoided() {
		return voided;
	}

	@Override
	public boolean isVoided() {
		return voided;
	}
	
}