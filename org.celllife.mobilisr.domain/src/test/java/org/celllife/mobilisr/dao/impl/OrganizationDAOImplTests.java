package org.celllife.mobilisr.dao.impl;

import java.util.Date;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class OrganizationDAOImplTests extends AbstractDBTest {

	@Autowired
	private OrganizationDAO orgDao;
	
	private Organization organization;

	private User user;

	private int initialBalance;
	
	@Before
	public void before(){
		organization = orgDao.findAll().get(1);
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_ORGANIZATION, organization);
		user = (User) getGeneralDao().search(search).get(0);
		
		initialBalance = organization.getBalance();
		createTransactions();
	}
	
	private void createTransactions() {
		Transaction ct = new Transaction(0, 5, "transactionOrigin", "transactionSource",
				"message", user, user.getOrganization());
		getGeneralDao().save(ct);
		
		ct = new Transaction(0, 15, "transactionOrigin", "transactionSource",
				"message", user, user.getOrganization());
		getGeneralDao().save(ct);
		
		ct = new Transaction(0, 100, "transactionOrigin", "transactionSource",
				"message", user, user.getOrganization());
		getGeneralDao().save(ct);
		
		ct = new Transaction(0, 100, "transactionOrigin", "transactionSource",
				"message", user, user.getOrganization());
		getGeneralDao().save(ct);
		
		ct = new Transaction(-50, -50,"transactionOrigin", "transactionSource",
				"transactionMessage", user, user.getOrganization());
		getGeneralDao().save(ct);
	}

	
	@Test
	public void testGetLastLoggedInUser(){
		Date d = new Date();
		user.setLastLoginDate(d);
		getGeneralDao().saveOrUpdate(user);
		User lastLoggedInUser = orgDao.getLastLoggedInUser(organization);
		Assert.assertEquals(lastLoggedInUser.getId(),user.getId());
		
	}
}
