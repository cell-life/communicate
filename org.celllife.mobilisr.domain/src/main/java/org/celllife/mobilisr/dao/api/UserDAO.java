package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;


public interface UserDAO extends BaseDAO<User, Long> {

	void saveOrUpdateUserRoles(User user, List<Role> roles);

	User getUserByUsername(String userName);

	User getUserByEmail(String emailAddress);

	List<User> getUsersByRole(Role role);
		
}
