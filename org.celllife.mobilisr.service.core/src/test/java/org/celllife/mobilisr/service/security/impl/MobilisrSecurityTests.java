package org.celllife.mobilisr.service.security.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.dao.api.RoleDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.DuplicateTransactionException;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.MobilisrSecurityException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.security.MobilisrUserDetails;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.ExpectedException;

import com.trg.search.Search;

public class MobilisrSecurityTests extends AbstractServiceTest{

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private UserService crudUserService;
	
	@Autowired
	private OrganizationDAO organizationDAO;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	private Organization org;
	
	@Autowired
	private UserBalanceService balanceService;
	
	@Before
	public void init(){
		Search search = new Search();
		search.addFilterEqual(Organization.PROP_NAME, "test org 0");
		org = organizationDAO.searchUnique(search);
	}
	
	@Test
	public void testSaveUser() throws UniquePropertyException{
		
		User user = new User();
		user.setFirstName("dev_user 101");
		user.setUserName("dev_test_user101");
		user.setPassword("dev_test_pwd101");
		user.setOrganization(org);
		user.setMsisdn("27783442542");
		user.setEmailAddress("u@u.com");
		
		User savedUser = crudUserService.saveUser(user, new ArrayList<Role>());
		
		Assert.assertNotNull(savedUser.getId());		
	}
	
	@Test
	public void testSaveUser_newApiKey() throws UniquePropertyException{
		
		User user = new User();
		user.setFirstName("dev_user 101");
		user.setUserName("dev_test_user101");
		user.setPassword("dev_test_pwd101");
		user.setOrganization(org);
		user.setMsisdn("27783442542");
		user.setEmailAddress("u@u.com");
		user.getApiKeys().add(new ApiKey("test", user));
		
		User savedUser = crudUserService.saveUser(user, new ArrayList<Role>());
		
		User user2 = crudUserService.getUser(savedUser.getId());
		Assert.assertEquals(1, user2.getApiKeys().size());
		Assert.assertEquals("test", user.getApiKeys().get(0).getKeyValue());
	}
	
	@Test
	public void testSaveUser_deletedApiKey() throws UniquePropertyException{
		
		User user = new User();
		user.setFirstName("dev_user 101");
		user.setUserName("dev_test_user101");
		user.setPassword("dev_test_pwd101");
		user.setOrganization(org);
		user.setMsisdn("27783442542");
		user.setEmailAddress("u@u.com");
		user.getApiKeys().add(new ApiKey("test1", user));
		user.getApiKeys().add(new ApiKey("test2", user));
		
		User savedUser = crudUserService.saveUser(user, new ArrayList<Role>());
		
		savedUser.getApiKeys().get(0).setDeleted(true);
		crudUserService.saveUser(savedUser, new ArrayList<Role>());
		
		User user2 = crudUserService.getUser(savedUser.getId());
		Assert.assertEquals(1, user2.getApiKeys().size());
		Assert.assertEquals("test2", user.getApiKeys().get(0).getKeyValue());
	}
	
	@Test
	public void testSaveUser_existing() throws UniquePropertyException{
		User user = userDAO.getUserByUsername("username 0");
		
		User user2 = new User();
		user2.setId(Long.valueOf(user.getId()));
		user2.setFirstName(user.getFirstName());
		user2.setUserName(user.getUserName());
		user2.setPassword(user.getPassword());
		user2.setSalt(user.getSalt());
		user2.setOrganization(user.getOrganization());
		user2.setMsisdn(user.getMsisdn());
		user2.setEmailAddress(user.getEmailAddress());
		
		User savedUser = crudUserService.saveUser(user2, new ArrayList<Role>());
		
		Assert.assertNotNull(savedUser.getId());
		// doesn't throw any exceptions
	}
	
	@Test
	public void createNewUserWithSameUsername() throws UniquePropertyException{
		
		User user = new User();
		user.setFirstName("dev_user 101");
		String userName = "username 0";
		user.setUserName(userName);
		user.setPassword("dev_test_pwd101");
		user.setEmailAddress("u@u.com");
		user.setMsisdn("23424242");
		user.setOrganization(org);
		
		try {
			crudUserService.saveUser(user,new ArrayList<Role>());
			Assert.fail("Expected UniquePropertyException");
		} catch (UniquePropertyException e) {
			Assert.assertTrue(e.getMessage().contains(userName));
		}
	}
	
	@Test
	public void createNewUserWithSameEmail() throws UniquePropertyException{
		
		User user = new User();
		user.setFirstName("dev_user 101");
		user.setUserName("unique username");
		user.setPassword("dev_test_pwd101");
		String emailAddress = "fsfs0@dfsfs.com";
		user.setEmailAddress(emailAddress);
		user.setMsisdn("23424242");
		user.setOrganization(org);
		
		try {
			crudUserService.saveUser(user,new ArrayList<Role>());
			Assert.fail("Expected UniquePropertyException");
		} catch (UniquePropertyException e) {
			Assert.assertTrue(e.getMessage().contains(emailAddress));
		}
	}
	
	@Test
	public void updateUserDetailWithSamePwd() throws UniquePropertyException{
		
		String newFirstName = "MOD fName";
		
		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, "username 0");
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		String oldPwd = user.getPassword();
		user.setEmailAddress("u@u.com");
		user.setMsisdn("23424242");
		user.setFirstName(newFirstName);
		
		User persistedUser = crudUserService.saveUser(user,new ArrayList<Role>());
		
		Assert.assertNotNull(persistedUser.getId());
		Assert.assertEquals(oldPwd, persistedUser.getPassword());
		Assert.assertEquals(newFirstName, persistedUser.getFirstName());
	}
	
	@Test
	public void updateUserDetailWithNewUsername() throws UniquePropertyException{
		String newUserName = "shrek";
		
		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, "username 0");
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		String oldPwd = user.getPassword();
		user.setEmailAddress("u@u.com");
		user.setMsisdn("23424242");
		user.setUserName(newUserName);
		
		User persistedUser = crudUserService.saveUser(user,new ArrayList<Role>());
		
		Assert.assertNotNull(persistedUser.getId());
		Assert.assertEquals(oldPwd, persistedUser.getPassword());
		Assert.assertEquals(newUserName, persistedUser.getUserName());
	}
	
	@Test
	public void updateUserDetailWithNewPwd() throws UniquePropertyException{
		
		String newFirstName = "MOD fName";
		String newPwd = "MOD PWD";
		
		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, "username 0");
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		String oldPwd = user.getPassword();
		user.setEmailAddress("u@u.com");
		user.setMsisdn("23424242");
		user.setPassword(newPwd);
		user.setFirstName(newFirstName);
		
		User persistedUser = crudUserService.saveUser(user,new ArrayList<Role>());
		
		Assert.assertNotNull(persistedUser.getId());
		Assert.assertNotSame(oldPwd, persistedUser.getPassword());
		Assert.assertNotSame(newPwd, persistedUser.getPassword());
		Assert.assertEquals(newFirstName, persistedUser.getFirstName());
	}
	
	
	@Test
	public void testUserLoginWithCorrectPwd() throws IOException, ServletException, UniquePropertyException{
		
		testSaveUser();
		
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("dev_test_user101", "dev_test_pwd101");
		
		AuthenticationManager entryPoint = (AuthenticationManager) applicationContext.getBean("authenticationManager");
		Authentication authenticate = entryPoint.authenticate(usernamePasswordAuthenticationToken);
		MobilisrUserDetails mobilisrUser = (MobilisrUserDetails) authenticate.getPrincipal();
		
		Assert.assertNotNull(mobilisrUser);
		
	}
	
	@Test
	@ExpectedException(value=BadCredentialsException.class)
	public void testUserLoginWithIncorrectPwd() throws IOException, ServletException, UniquePropertyException{
		
		testSaveUser();
		
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("dev_test_user101", "whatever man");
		
		AuthenticationManager entryPoint = (AuthenticationManager) applicationContext.getBean("authenticationManager");
		entryPoint.authenticate(usernamePasswordAuthenticationToken);
		
	}
	
	@Test
	@ExpectedException(value=BadCredentialsException.class)
	public void testUserLoginWithIncorrectUsername() throws IOException, ServletException, UniquePropertyException{
		
		testSaveUser();
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("dude wheres my car", "whatever man");
		
		AuthenticationManager entryPoint = (AuthenticationManager) applicationContext.getBean("authenticationManager");
		entryPoint.authenticate(usernamePasswordAuthenticationToken);
		
	}
	
	@Test
	public void testUserLoginWithUpdatedPwd() throws IOException, ServletException, UniquePropertyException{
		
		String newPwd = "i am legend";
		
		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, "username 0");
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		String oldPwd = user.getPassword();
		user.setPassword(newPwd);
		user.setEmailAddress("u@u.com");
		user.setMsisdn("23424242");
		User updatedUser = crudUserService.saveUser(user,new ArrayList<Role>());
		
		Assert.assertNotNull(updatedUser);
		Assert.assertNotSame(oldPwd, updatedUser.getPassword());
		
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("username 0", newPwd);
		
		AuthenticationManager entryPoint = (AuthenticationManager) applicationContext.getBean("authenticationManager");
		Authentication authenticate = entryPoint.authenticate(usernamePasswordAuthenticationToken);
		MobilisrUserDetails mobilisrUser = (MobilisrUserDetails) authenticate.getPrincipal();
		
		Assert.assertNotNull(mobilisrUser);
		
	}
	
	@Test(expected=MobilisrSecurityException.class)
	public void testSpringPermissionChecking_fail() throws DuplicateTransactionException, UniquePropertyException{
		String password = "demopassword";
		String username = "username 0";

		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, username);
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		user.setPassword(password);
		user = crudUserService.saveUser(user,new ArrayList<Role>());
		
		login(username,password);
		
		balanceService.credit(10, org, "createdFor", "createdBy", "message", user);
	}
	
	@Test
	public void testSpringPermissionChecking_pass() throws DuplicateTransactionException, UniquePropertyException{
		String password = "demopassword";
		String username = "username 0";

		Search search = new Search();
		search.addFilterEqual(User.PROP_USERNAME, username);
		search.addFetch(User.PROP_API_KEYS);
		User user = userDAO.searchUnique(search);
		user.setPassword(password );
		
		Role role = new Role();
		role.setName("CREDIT_ORGANIZATION_BALANCE");
		role.setPermissionsList(Arrays.asList(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE));
		roleDAO.save(role);
		
		user = crudUserService.saveUser(user,Arrays.asList(role));
		
		login(username,password);
		
		balanceService.credit(10, org, "createdFor", "createdBy", "message", user);
	}

}
