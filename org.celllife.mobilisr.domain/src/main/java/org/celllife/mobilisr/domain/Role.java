package org.celllife.mobilisr.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="role")
public class Role extends AbstractBaseEntity {

	private static final long serialVersionUID = 8551546490907608534L;

	public static final String PROP_NAME = "name";
	public static final String PROP_USERS = "users";
	public static final String PROP_PERMISSIONS = "permissions";

	@Column(nullable = false, unique = true, length=35)
	private String name;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "role_Id") }, inverseJoinColumns = { @JoinColumn(name = "user_Id") })
	@ForeignKey(name="fk_role_user",inverseName="fk_user_role")
	private List<User> users = new ArrayList<User>();
	
	@Column(name="permissions", columnDefinition="TEXT")
	private String permissions;
	
	public Role() {
	}
	
	public Role(String name, String permissions) {
		super();
		this.name = name;
		this.permissions = permissions;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	
	public void setPermissionsList(List<MobilisrPermission> permissionsList){
		if (permissionsList.isEmpty()){
			this.permissions = null;
			return;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		for(MobilisrPermission permission: permissionsList){
			stringBuffer.append(permission.name());
			stringBuffer.append(";");
		}
		
		this.permissions = stringBuffer.substring(0, stringBuffer.length()-1);
	}
	
	public List<MobilisrPermission> getPermissionsList(){
		List<MobilisrPermission> permissionList = new ArrayList<MobilisrPermission>();
		if (permissions == null || permissions.trim().isEmpty()){
			return permissionList;
		}
		
		String[] availPermissions = permissions.split(";");
		for(String permissionName: availPermissions){
			permissionList.add(MobilisrPermission.valueOf(permissionName));
		}
		
		return permissionList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
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
		Role other = (Role) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		return true;
	}

	public boolean isSuperAdmin() {
		return permissions.contains(MobilisrPermission.ROLE_ADMIN.name());
	}
}
