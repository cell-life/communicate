package org.celllife.mobilisr.service.security;

import java.util.Date;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import com.trg.search.Search;

public class AuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {

	
	@Autowired
	private MobilisrGeneralDAO generalDAO;
	
	@Autowired
	private UserDAO userDAO;

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		Object principal = event.getAuthentication().getPrincipal();
		if (principal instanceof MobilisrUserDetails){
			MobilisrUserDetails mobilisrUser = (MobilisrUserDetails) principal;
			Search search = new Search();
			search.addFilterEqual(User.PROP_ID, mobilisrUser.getUser().getId());
			search.addFetch(User.PROP_ROLES);
			User user = userDAO.searchUnique(search);
			user.setLastLoginDate(new Date());
			generalDAO.save(user);
		}
	}

}
