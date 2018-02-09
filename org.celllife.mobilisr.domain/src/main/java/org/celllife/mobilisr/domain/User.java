package org.celllife.mobilisr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * Domain class for User
 * @author Vikram Bindal
 */
@Entity
@Table(name="user")
//@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class User extends VoidableEntity implements HasOrganization, Serializable {

	private static final long serialVersionUID = -617561580942523224L;

	public static final String PROP_FIRST_NAME = "firstName";
	public static final String PROP_LAST_NAME = "lastName";
	public static final String PROP_USERNAME = "userName";
	public static final String PROP_PASSWORD = "password";
	public static final String PROP_EMAIL = "emailAddress";
	public static final String PROP_MSISDN = "msisdn";
	public static final String PROP_SALT = "salt";
	public static final String PROP_ROLES = "roles";
	public static final String PROP_API_KEYS = "apiKeys";
	public static final String PROP_CAMPAIGN = "campaigns";
	public static final String PROP_CLIENT_ALERTS = "clientAlerts";
	public static final String PROP_LAST_LOGIN_DATE = "lastLoginDate";
	public static final String PROP_LOGIN_SINCE_UPGRADE = "loginSinceUpgrade";
	
	// Calculated properties
	public static final String PROP_FULL_NAME = "fullName";
	
	@Column(name="firstname", nullable = false, length=35)
	@Index(name = "USER_FNAME", columnNames = { "firstname" })
	private String firstName;

	@Column(name="lastname", length=35)
	@Index(name = "USER_LNAME", columnNames = { "lastname" })
	private String lastName;

	@Column(name="emailaddress", nullable=false, unique=true)
	@Index(name = "USER_EMAIL", columnNames = { "emailaddress" })
	private String emailAddress;

	@Column(nullable=false, length=20)
	@Index(name = "USER_MSISDN", columnNames = { "msisdn" })
	private String msisdn;
	
	@Column(name="username", nullable = false, unique = true, length=35)
	@Index(name = "USER_USERNAME", columnNames = { "username" })
	private String userName;

	@Column(nullable = false)
	@Index(name = "USER_PWD", columnNames = { "password" })
	private String password;
	
	@Column(nullable = false, length=50)
	private String salt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="organization_id", nullable = false)
	@ForeignKey(name="fk_user_organization",inverseName="fk_organization_user")
	private Organization organization;
	
	@ManyToMany
	@JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_Id") }, inverseJoinColumns = { @JoinColumn(name = "role_Id") })
	@ForeignKey(name="fk_user_role",inverseName="fk_role_user")
	private List<Role> roles = new ArrayList<Role>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = { CascadeType.ALL })
	private List<ApiKey> apiKeys = new ArrayList<ApiKey>();

	@Column(name="lastlogindate")
	@Index(name="USER_LASTLOGINDATE", columnNames={ "lastlogindate"})
	private Date lastLoginDate;
	
	@Column(name="loginsinceupgrade")
	private Boolean loginSinceUpgrade;

	public User() {
	}

	public User(String firstName, String lastName, String emailAddress,
			String msisdn, String userName, String password, String salt,
			Organization organization) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.msisdn = msisdn;
		this.userName = userName;
		this.password = password;
		this.salt = salt;
		this.organization = organization;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFullName(){
		return this.firstName + " " + this.lastName;
	}
	
	/**
	 * @return the eMail
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param mail
	 *            the eMail to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	/**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [firstName=" + firstName
				+ ", password=" + password + ", userName=" + userName + ", getId()="
				+ getId() + "]";
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public void addRole(Role role) {
		if (!roles.contains(role))
			roles.add(role);
	}

	public List<MobilisrPermission> getPermissions() {
		List<MobilisrPermission> permissions = new ArrayList<MobilisrPermission>();
		if (roles != null && !roles.isEmpty()){
			for (Role role : roles) {
				permissions.addAll(role.getPermissionsList());
			}
		}
		return permissions;
	}

	/**
	 * @param lastLoginDate
	 *            the last login date to set
	 */
	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 * @return the lastLoginDate
	 */
	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public boolean isSuperAdmin() {
		if (roles == null){
			return false;
		} 
		
		for (Role role : roles) {
			if (role.isSuperAdmin()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPermission(MobilisrPermission permission){
		List<MobilisrPermission> permissionsList = getPermissions();
		for (MobilisrPermission perm : permissionsList){
			if (perm.implies(permission)){
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		User other = (User) obj;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public Boolean getLoginSinceUpgrade() {
		return loginSinceUpgrade;
	}

	public void setLoginSinceUpgrade(Boolean loginSinceUpgrade) {
		this.loginSinceUpgrade = loginSinceUpgrade;
	}

	public void setApiKeys(List<ApiKey> apiKeys) {
		this.apiKeys = apiKeys;
	}

	public List<ApiKey> getApiKeys() {
		if (apiKeys == null){
			apiKeys = new ArrayList<ApiKey>();
		}
		return apiKeys;
	}

	public List<ApiKey> getActiveApiKeys() {
		List<ApiKey> keys = new ArrayList<ApiKey>();
		for (ApiKey apiKey : getApiKeys()) {
			if (!apiKey.isVoided()){
				keys.add(apiKey);
			}
		}
		return keys;
	}
	
}
