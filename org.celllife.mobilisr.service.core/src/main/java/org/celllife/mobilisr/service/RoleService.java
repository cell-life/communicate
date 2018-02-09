package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;

import com.google.gwt.user.client.rpc.RemoteService;

public interface RoleService extends RemoteService{

	Role saveOrUpdateRole(Role role) throws UniquePropertyException;
	
	List<Role> listAllRoles();
	
	void deleteRole(Role role) throws MobilisrException, MobilisrRuntimeException;

	List<User> listOfUsersForRole(Role role);

}
