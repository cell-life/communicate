package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.RoleService;

public class RoleServiceImpl extends AbstractMobilisrService implements RoleService {

	private static final long serialVersionUID = 2634618186474154171L;
	private org.celllife.mobilisr.service.RoleService service;
	
	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.RoleService) getBean("crudRoleService");
	}

	@Override
	public Role saveOrUpdateRole(Role role) throws MobilisrException {
		return service.saveOrUpdateRole(role);
	}

	@Override
	public List<Role> listAllRoles() throws MobilisrException {
		return service.listAllRoles();
	}

	@Override
	public void deleteRole(Role role) throws MobilisrException,
			MobilisrRuntimeException {
		service.deleteRole(role);
	}

	@Override
	public List<User> listOfUsersForRole(Role role) throws MobilisrException,
			MobilisrRuntimeException {
		return service.listOfUsersForRole(role);
	}
	
	
}
