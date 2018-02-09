package org.celllife.mobilisr.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "apikey")
public class ApiKey extends VoidableEntity implements Serializable {

	private static final long serialVersionUID = 8556851011474829790L;

	public static final String PROP_KEY = "keyValue";
	public static final String PROP_USER = "user";

	@Column(name = "keyvalue", nullable = false, unique = true, length = 100)
	private String keyValue;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	@ForeignKey(name = "fk_apikey_user")
	private User user;

	@Transient
	private boolean deleted;

	public ApiKey() {
	}

	public ApiKey(String key, User user) {
		super();
		this.keyValue = key;
		this.user = user;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String key) {
		this.keyValue = key;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
}
