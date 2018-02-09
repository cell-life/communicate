package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.trg.search.Search;

/**
 * Default implementation of the OrganizationDAO interface
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
@Repository("organizationDAO")
public class OrganizationDAOImpl extends BaseDAOImpl<Organization, Long> implements OrganizationDAO {
	
	@Autowired
	private MobilisrGeneralDAO generalDAO;
		
	@Override
	@Loggable(LogLevel.TRACE)
	public User getLastLoggedInUser(Organization organization) {

		Search s = new Search(User.class);
		s.addFilterEqual(User.PROP_ORGANIZATION, organization);
		s.addFilterNotNull(User.PROP_LAST_LOGIN_DATE);
		s.addSort(User.PROP_LAST_LOGIN_DATE,false);
		s.setMaxResults(1);
		List<?> search = generalDAO.search(s);
		if (search.isEmpty()){
			return null;
		}
		User usr = (User) search.get(0);
		return usr;
	}
}
