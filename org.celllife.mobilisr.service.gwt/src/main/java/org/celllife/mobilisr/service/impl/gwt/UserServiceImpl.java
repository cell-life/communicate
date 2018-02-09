package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.UserService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class UserServiceImpl extends AbstractMobilisrService implements
		UserService {

	private static final long serialVersionUID = 9039800506311576710L;
	private org.celllife.mobilisr.service.UserService service;
	
	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.UserService) getBean("crudUserService");
	}

	@Override
	public User saveUser(User user, List<Role> roles) throws MobilisrException {
		return service.saveUser(user, roles);
	}
	
	@Override
	public User saveCurrentUser(User user) throws MobilisrException {
		return service.saveCurrentUser(user);
	}

	@Override
	public User getUser(Long id) throws MobilisrException {
		return service.getUser(id);
	}

	@Override
	public User getCurrentLoggedInUser() throws MobilisrException {
		return service.getCurrentLoggedInUser();
	}
	
	@Override
	public int getLoggedInUserCount() throws MobilisrException {
		return service.getLoggedInUserCount();
	}
	
	@Override
	public List<User> getAllLoggedInUsers() throws MobilisrException {
		return service.getAllLoggedInUsers();
	}

	@Override
	public PagingLoadResult<User> listAllUsers(Organization org, PagingLoadConfig loadConfig, Boolean voidedStatus)
			throws MobilisrException {
		return service.listAllUsers(org, loadConfig, voidedStatus);
	}
	
	@Override
	public Boolean validatePassword(User user, String pw)
			throws MobilisrException {
		return service.validatePassword(user, pw);
	}
	
	@Override
	public ApiKey createApiKey(User user) throws MobilisrException,
			MobilisrRuntimeException {
		return service.createApiKey(user);
	}
}
