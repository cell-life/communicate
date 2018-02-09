package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.RoleDAO;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.RoleService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.trg.search.Filter;
import com.trg.search.Search;

@Service("crudRoleService")
public class RoleServiceImpl implements RoleService {

	private static final long serialVersionUID = 2655644772402552872L;
	private static Logger log = LoggerFactory
			.getLogger(RoleServiceImpl.class);

	@Autowired
	private RoleDAO roleDAO;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<Role> listAllRoles() {
		Search search = new Search(Role.class);
		search.addSortAsc(Role.PROP_NAME);
		List<Role> roleList = roleDAO.search(search);
		return roleList;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_MANAGE_ROLES"})
	public Role saveOrUpdateRole(Role role)	throws UniquePropertyException {
		try {
			roleDAO.saveOrUpdate(role);
		} catch (Exception e) {
			throw new UniquePropertyException("Role with name \'"
					+ role.getName() + "\' already exists");
		}

		return role;
	}

	@Override
	public List<User> listOfUsersForRole(Role role) {
		Search search = new Search(Role.class);
		search.addFilter(Filter.equal(Role.PROP_ID, role.getId()));
		search.addField(Role.PROP_USERS);
		List<User> listOfUsers = roleDAO.search(search);
		return listOfUsers;
		
	}
	
	@Override
	@Secured({"PERM_MANAGE_ROLES"})
	public void deleteRole(Role role) throws MobilisrException, MobilisrRuntimeException {
		
		if (role.getName().equalsIgnoreCase("ADMINISTRATOR")){
			throw new MobilisrException("Cannot delete ADMINISTRATOR Role");
		}
		
		List<User> listOfUsers = listOfUsersForRole(role);
		if (listOfUsers.size() !=0){
			log.debug("Role is in use. Removing role from users.");
			role.setUsers(new ArrayList<User>());
			log.debug("saving role with empty User list");
			roleDAO.save(role);
		}
		
		log.debug("Removing role");
		roleDAO.remove(role);	
	}
}
