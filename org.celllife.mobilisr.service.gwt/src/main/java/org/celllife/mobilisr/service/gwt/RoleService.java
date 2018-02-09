package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.RoleService
 */
@RemoteServiceRelativePath("crudRole.rpc")
public interface RoleService extends RemoteService{

	/**
	 * @see org.celllife.mobilisr.service.RoleService#saveOrUpdateRole(Role)
	 */
	Role saveOrUpdateRole(Role role) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.RoleService#listAllRoles()
	 */
	List<Role> listAllRoles() throws MobilisrException, MobilisrRuntimeException;

	void deleteRole(Role role) throws MobilisrException, MobilisrRuntimeException;

	List<User> listOfUsersForRole(Role role) throws MobilisrException,
			MobilisrRuntimeException;
	
}
