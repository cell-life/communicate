package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class UserServiceImplTests extends AbstractServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private SessionFactory sessionFactory;

	private SecondLevelCacheStatistics secondLevelCacheStatistics;

	private int totalUsers;

	@Before
	public void setup(){
		totalUsers = userDAO.findAll().size();
	}
	
	@Test
	public void testListUserWithPagination() {
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 5);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		PagingLoadResult<User> list = userService.listAllUsers(null,plc,null);
		stopWatch.stop();

		Assert.assertEquals(totalUsers, list.getTotalLength());
		Assert.assertEquals(5, list.getData().size());
		Assert
				.assertEquals("firstname 0", list.getData().get(1)
						.getFirstName());
		Assert
				.assertEquals("firstname 0", list.getData().get(2)
						.getFirstName());
	}

	@Test
	@Ignore("not a real test case")
	// NOTE: This test actually does not work as hit counts are not registered
	public void testQueryCaching() throws UniquePropertyException {

		Statistics statistics = sessionFactory.getStatistics();
		secondLevelCacheStatistics = statistics
				.getSecondLevelCacheStatistics("org.celllife.mobilisr.domain.User");
		StopWatch stopWatch = new StopWatch();

		for (int i = 0; i < 10; i++) {
			stopWatch.start();
			Session session = sessionFactory.openSession();
			session.createQuery("from User u where u.id =:id").setParameter(
					"id", 89l).setCacheable(true).list();
			session.close();

			stopWatch.stop();

			System.out.println("Query cache hit count: "
					+ statistics.getQueryCacheHitCount());
			System.out.println("Second level stats: "
					+ secondLevelCacheStatistics);
		}
	}

	@Test
	public void testListAllUsersOrderAsc() {
		PagingLoadConfig plc = new BasePagingLoadConfig(3, 2);
		plc.setSortField("lastName");
		PagingLoadResult<User> list = userService.listAllUsers(null,plc,null);
		Assert.assertEquals(totalUsers, list.getTotalLength());
		Assert.assertEquals(2, list.getData().size());
		Assert.assertEquals("lastname 2", list.getData().get(0).getLastName());
		Assert.assertEquals("lastname 3", list.getData().get(1).getLastName());
	}

	@Test
	public void testListAllUsersOrderDesc() {
		PagingLoadConfig plc = new BasePagingLoadConfig(3, 2);
		plc.setSortField("lastName");
		plc.setSortDir(SortDir.DESC);
		PagingLoadResult<User> list = userService.listAllUsers(null,plc,null);
		Assert.assertEquals(totalUsers, list.getTotalLength());
		Assert.assertEquals(2, list.getData().size());
		Assert.assertEquals("lastname 7", list.getData().get(0).getLastName());
		Assert.assertEquals("lastname 6", list.getData().get(1).getLastName());
	}

	@Test
	public void testListAllUsersEndOfList() {
		PagingLoadConfig plc = new BasePagingLoadConfig(5, 6);
		PagingLoadResult<User> list = userService.listAllUsers(null,plc,null);
		Assert.assertEquals(totalUsers, list.getTotalLength());
		Assert.assertEquals(6, list.getData().size());
		Assert.assertEquals("lastname 2", list.getData().get(5).getLastName());
	}

	@Test
	public void testListAllUsersFullList() {
		PagingLoadConfig plc = new BasePagingLoadConfig(0, totalUsers+1);
		PagingLoadResult<User> list = userService.listAllUsers(null,plc,null);
		Assert.assertEquals(totalUsers, list.getTotalLength());
		Assert.assertEquals(totalUsers, list.getData().size());
		Assert
				.assertEquals("firstname 0", list.getData().get(1)
						.getFirstName());
		Assert
				.assertEquals("firstname 5", list.getData().get(8)
						.getFirstName());
	}

	@Test
	public void testListOfRolesForUser() {
		User user = (User) userService.listAllUsers().get(1);
		List<Role> roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(false, roleList.isEmpty());
		String roleName = ((Role) roleList.get(0)).getName();
		Assert.assertEquals("role0", roleName);
	}

	@Test
	public void testSetRolesForUser() throws UniquePropertyException {
		User user = (User) userService.listAllUsers().get(0);
		user = userService.getUser(user.getId());
		List<Role> roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(false, roleList.isEmpty());

		ArrayList<Role> newRoleList = new ArrayList<Role>();
		newRoleList.addAll(roleList);
		user.setMsisdn("277788789");
		user.setEmailAddress("s@s.com");
		user = userService.saveUser(user, newRoleList);

		user = userService.getUser(user.getId());
		Assert.assertEquals(roleList, user.getRoles());

		roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(roleList.size(), newRoleList.size());

		user = userService.saveUser(user, new ArrayList<Role>());
		roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(0, roleList.size());

	}

	@Test
	public void resetPassword() {
		User user = (User) userService.listAllUsers().get(0);
		String newPassword = userService.resetPassword(user);
		Assert.assertTrue("Password matches",userService.validatePassword(user, newPassword));
	}

	@Test
	public void testSetRolesForDetachedUser() throws UniquePropertyException {
		User user = (User) userService.listAllUsers().get(0);
		user = userService.getUser(user.getId());
		List<Role> roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(false, roleList.isEmpty());

		userDAO.detach(user);
		for (Role role : roleList) {
			userDAO.detach(role);
		}
		ArrayList<Role> newRoleList = new ArrayList<Role>();
		newRoleList.addAll(roleList);
		user.setEmailAddress("s@s.com");
		user.setMsisdn("277788789");
		user = userService.saveUser(user, newRoleList);

		user = userService.getUser(user.getId());
		Assert.assertEquals(roleList, user.getRoles());

		roleList = userService.listOfRolesForUser(user);
		Assert.assertEquals(roleList.size(), newRoleList.size());

	}

	@Test
	public void testVoidedUsers() {
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 200);
		plc.setSortField("lastName");
	
		
		PagingLoadResult<User> listAll = userService.listAllUsers(null,plc,null);
		List<User> data = listAll.getData();
		//void half the users
		int j = data.size()/3;
		for (int i =0; i< j; i++){
			User user = userService.getUser(data.get(i).getId());
			user.setVoided(true); 
			try {
				userService.saveUser(user, new ArrayList<Role>());
			} catch (UniquePropertyException e) {
				// ignore exception
				e.printStackTrace();
			}
		}
		 listAll = userService.listAllUsers(null,plc,null); // refresh the list
		int listVoid = userService.listAllUsers(null,plc,true).getTotalLength(); // get void users (should be 1/3 * totalUsers)
		int listUnVoid = userService.listAllUsers(null,plc,false).getTotalLength(); // get unvoid users 2*totalUsers/3)
		
		Assert.assertEquals(listVoid,j);
		Assert.assertEquals(listUnVoid,(data.size()-j));
	}

	@Test
	public void testVoidedOrganization() {
		Organization org = new Organization();
		org.setName("test void org");
		org.setVoided(true);
		getGeneralDao().save(org);
		
		List<User> create = DomainMockFactory._().on(User.class).create(3);
		for (User user : create) {
			user.setId(null);
			user.setOrganization(org);
			getGeneralDao().save(user);
		}
		
		PagingLoadConfig plc = new BasePagingLoadConfig(0, 200);
		plc.setSortField("lastName");
	
		int numVoided = userService.listAllUsers(null,plc,true).getTotalLength();
		int numUnVoided = userService.listAllUsers(null,plc,false).getTotalLength();
		int totalNum = userService.listAllUsers(null,plc,null).getTotalLength();
		
		Assert.assertEquals(3,numVoided);
		Assert.assertEquals(totalUsers,numUnVoided);
		Assert.assertEquals(totalNum, totalUsers + 3);
	}
	
}
