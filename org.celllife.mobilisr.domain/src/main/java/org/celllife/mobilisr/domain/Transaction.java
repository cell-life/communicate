package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;


/**
 * Domain class for Client Transaction
 * @author Vikram Bindal
 */
@Entity
@Table(name="transaction")
public class Transaction extends AbstractBaseEntity implements  HasOrganization, Serializable {

	private static final long serialVersionUID = -8779891271054161586L;
	
	public static final String PROP_COST = "cost";
	public static final String PROP_RESERVED = "reserved";
	public static final String PROP_DATETIME = "datetime";
	public static final String PROP_MSG = "message";
	public static final String PROP_CREATED_FOR = "createdfor";
	public static final String PROP_CREATED_BY = "createdby";
	public static final String PROP_PARENT = "parent";
	public static final String PROP_USER = "user";
	
	@Column(name="cost", nullable = false)
	private int cost;
	
	@Column(name="reserved", nullable = false)
	private int reserved;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="datetime", nullable = false)
	@Index(name = "TRANSACTION_DATETIME", columnNames = { "datetime" })
	private Date datetime;

	@Column(name="message", nullable = false)
	private String message;

	@Column(name="createdfor", nullable = false, length=60)
	@Index(name = "TRANSACTION_CREATEDFOR", columnNames = { "createdfor" })
	private String createdfor;
	
	@Column(name="createdby", nullable = false, length=60)
	private String createdby;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id",nullable = true)
	private Transaction parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id",nullable = true)
	@ForeignKey(name="fk_transaction_user",inverseName="fk_user_transaction")
	private User user;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="organization_id", nullable = false)
	@ForeignKey(name="fk_transaction_organization",inverseName="fk_organization_transaction")
	private Organization organization;

	public Transaction() {
	}

	public Transaction(int cost, int reserved, String createdFor, String createdBy, String message,
			User user, Organization organization) {
		this.cost = cost;
		this.reserved = reserved;
		this.createdfor = createdFor;
		this.createdby = createdBy;
		this.user = user;
		this.message = message;
		this.datetime = new Date();
		this.organization = organization;
	}
	
	public Date getDatetime() {
		return datetime == null ? null : new Date(datetime.getTime());
	}

	public int getCost() {
		return cost;
	}

	public int getReserved() {
		return reserved;
	}

	public String getCreatedfor() {
		return createdfor;
	}

	public String getCreatedby() {
		return createdby;
	}

	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public Organization getOrganization() {
		return organization;
	}
	
	@Override
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public void setParent(Transaction parent) {
		this.parent = parent;
	}

	public Transaction getParent() {
		return parent;
	}
}
