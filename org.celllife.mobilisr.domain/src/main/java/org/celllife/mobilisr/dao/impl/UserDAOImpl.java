package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Repository("userDAO")
public class UserDAOImpl extends BaseDAOImpl<User, Long> implements UserDAO {

	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public void saveOrUpdateUserRoles(User user, List<Role> roles) {
		
		SQLQuery delRoleQuery = getSession().createSQLQuery("delete FROM user_role where user_Id=:userId" );
		delRoleQuery.setParameter("userId", user.getId());
		
		delRoleQuery.executeUpdate();
		
		SQLQuery addRoleQuery = getSession().createSQLQuery("insert into user_role values(:userId, :roleId)");
		
		for (Role role : roles) {
			addRoleQuery.setParameter("userId", user.getId());
			addRoleQuery.setParameter("roleId", role.getId() );
			addRoleQuery.executeUpdate();
		}
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public User getUserByUsername(String userName) {
		Search s = new Search();
		s.addFilterEqual(User.PROP_USERNAME, userName);
		User user = searchUnique(s);
		return user;
	}
	
	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public User getUserByEmail(String email) {
		Search s = new Search();
		s.addFilterEqual(User.PROP_EMAIL, email);
		User user = searchUnique(s);
		return user;
	}
	
	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<User> getUsersByRole(Role role) {
		Search s = new Search();
		s.addFilterEqual(User.PROP_ROLES, role);
		List<User> users = search(s);
		return users;
	}
}
