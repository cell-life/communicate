package org.celllife.mobilisr.service.impl;

import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.AlertType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.DuplicateTransactionException;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class UserBalanceServiceTests extends AbstractServiceTest {

	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private OrganizationDAO orgDAO;
	
	@Autowired
	private MobilisrGeneralDAO generalDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	private User user = null;
	private Organization org = null;

	private Campaign campaign;

	@Before
	public void setup() {
		user = getUser();
		org = user.getOrganization();
		
		campaign = new Campaign();
		campaign.setOrganization(org);
		campaign.setId(17L);
	}

	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
	
	@Test
	public void testReserveAmount_LTOrgBalance() throws InsufficientBalanceException{
		
		int orgBal = org.getBalance();
		int amountToReserve = orgBal/2;
		
		//Reserve the amount
		Long reserveAmountRef = userBalanceService.reserveAmount(
				campaign.getOrganization(), amountToReserve,
				campaign.getIdentifierString(), user, "message");
		Assert.assertNotNull(reserveAmountRef);
		checkTransaction(reserveAmountRef, 0, amountToReserve);
	}
	
	@Test
	public void testReserveAmount_EQOrgBalance() throws InsufficientBalanceException{
		
		int orgBal = org.getBalance();
		int amountToReserve = orgBal;
		
		//Reserve the amount
		Long reserveAmountRef = userBalanceService.reserveAmount(
				campaign.getOrganization(), amountToReserve,
				campaign.getIdentifierString(), user, "message");
		
		Assert.assertNotNull(reserveAmountRef);
		checkTransaction(reserveAmountRef, 0, amountToReserve);
		
	}
	
	@Test(expected = InsufficientBalanceException.class)
	public void testReserveAmount_GTOrgBalance() throws InsufficientBalanceException{
		
		int orgBal = org.getBalance();
		int amountToReserve = orgBal*2;
		
		//Reserve the amount
		userBalanceService.reserveAmount(
				campaign.getOrganization(), amountToReserve,
				campaign.getIdentifierString(), user, "message");
	}
	
	@Test
	public void testUpdateSingleOrgBalance() throws InsufficientBalanceException, TransactionNotFoundException{
		// setup
		Organization org = user.getOrganization();
		int orgBalance = org.getBalance();
		
		int amountToDebit = 50;
		Transaction transaction = new Transaction(-amountToDebit,0, "createdFor", "createdBy", "test message", user, user.getOrganization());
		generalDAO.save(transaction);
		
		// call test method
		userBalanceService.updateOrgBalances();
		
		// verify
		Organization organization = orgDAO.find(org.getId());
		Assert.assertEquals((orgBalance-amountToDebit), organization.getBalance());
	}
	
	@Test
	public void testUpdateOrgBalanceWithLowThreshold() throws InsufficientBalanceException, TransactionNotFoundException{
		// setup
		Organization org = user.getOrganization();
		org.setContactEmail("test@test.com");
		int orgBalance = org.getAvailableBalance();
		
		org.setBalanceThreshold(orgBalance-2); // set the threshold > 0
		generalDAO.save(org);
		int amountToDebit = orgBalance-1;
		Transaction transaction = new Transaction(-amountToDebit,0, "createdFor", "createdBy", "test message", user, user.getOrganization());
		generalDAO.save(transaction);
		
		// call test method
		userBalanceService.updateOrgBalances();
		
		// verify
		Organization organization = orgDAO.find(org.getId());
		Assert.assertEquals((orgBalance-amountToDebit), organization.getAvailableBalance());
		
		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_ORGANIZATION, organization);
		s.addFilterEqual(MailMessage.PROP_TYPE, AlertType.BALANCE_RUNTIME);
		@SuppressWarnings("unchecked")
		List<MailMessage> alerts = generalDAO.search(s);
		Assert.assertEquals(1, alerts.size());
		
	}
	
	@Test
	public void testUpdateMultipleOrgBalance() throws InsufficientBalanceException, TransactionNotFoundException{
		// setup
		List<Organization> orgs = orgDAO.findAll();
		int[] balances = new int[orgs.size()];
		for (int i = 0; i < orgs.size(); i++) {
			Organization organization = orgs.get(i);
			balances[i] = organization.getBalance();
			campaign = new Campaign();
			campaign.setOrganization(organization);
			campaign.setId(17L);
			Long reserveAmountRef = userBalanceService.reserveAmount(
					campaign.getOrganization(), 10,
					campaign.getIdentifierString(), user, "message");
			
			userBalanceService.debitOrgBalance(10, 0, reserveAmountRef, organization.getIdentifierString(), "createdBy", "to debit", user);
		}
		
		// call test method
		userBalanceService.updateOrgBalances();
		
		// verify
		orgs = orgDAO.findAll();
		for (int i = 0; i < orgs.size(); i++) {
			Organization organization = orgs.get(i);
			Assert.assertEquals((balances[i] - 10), organization.getBalance());
		}
	}
	
	@Test
	public void testClientAlertForOutOfSyncBalance() throws InsufficientBalanceException, TransactionNotFoundException{
		// setup
		Organization org = user.getOrganization();
		org.setContactEmail("test@test.com");
		int orgBalance = org.getBalance();
		
		int amountToDebit = orgBalance-1;
		Transaction transaction = new Transaction(-amountToDebit,0, "createdFor", "createdBy", "test message", user, user.getOrganization());
		generalDAO.save(transaction);
		
		// call test method
		userBalanceService.updateOrgBalances();
		
		// verify
		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_TYPE, AlertType.SYSTEM_ALERT);
		@SuppressWarnings("unchecked")
		List<MailMessage> alerts = generalDAO.search(s);
		Assert.assertEquals(1, alerts.size());
	}
	
	@Test
	public void testDebitOrgBalance() throws TransactionNotFoundException{
		//setup
		Transaction transaction = new Transaction(-10,0, "createdFor", "createdBy", "test message", user, user.getOrganization());
		generalDAO.save(transaction);
		
		// call test method
		Long ref = userBalanceService.debitOrgBalance(5, 6, transaction.getId(), "createdFor", "createdBy", "transMsg", user);
		
		// verify
		Assert.assertNotNull(ref);
		Transaction tn = generalDAO.find(Transaction.class, ref);
		Assert.assertEquals(-5, tn.getCost());
		Assert.assertEquals(-6, tn.getReserved());
		
	}
	
	@Test
	public void testDebitOrgBalance_orgUpdated() throws TransactionNotFoundException{
		//setup
		Transaction transaction = new Transaction(-10,0, "createdFor", "createdBy", "test message", user, user.getOrganization());
		generalDAO.save(transaction);
		int initialBalance = user.getOrganization().getBalance();
		int initialReserved = user.getOrganization().getReserved();
		
		// call test method
		userBalanceService.debitOrgBalance(5, 6, transaction.getId(), "createdFor", "createdBy", "transMsg", user);
		
		// verify
		Organization org = generalDAO.find(Organization.class, user.getOrganization().getId());
		Assert.assertEquals(initialBalance-5, org.getBalance());
		Assert.assertEquals(initialReserved-6, org.getReserved());
		
	}
	
	@Test
	public void testDebitOrgBalance_notFound(){
		// setup
		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_TYPE,  AlertType.SYSTEM_ALERT);
		int alertsBefore = generalDAO.count(s);
		
		user.getOrganization().setContactEmail("test@test.com");
		
		try {
			// call test method
			userBalanceService.debitOrgBalance(5, 6, -1L, "createdFor", "createdBy", "transMsg", user);
			Assert.fail("Expected TransactionNotFoundException");
		} catch (TransactionNotFoundException e) {
			// verify
			int alertsAfter = generalDAO.count(s);
			Assert.assertEquals(alertsBefore+1, alertsAfter);
		}
	}
	
	@Test
	public void testCredit() throws DuplicateTransactionException{
		// call test method
		Long ref = userBalanceService.credit(-17, org, "createdFor", "createdBy", "message", user);
		
		// verify
		Assert.assertNotNull(ref);
		Transaction tn = generalDAO.find(Transaction.class, ref);
		Assert.assertEquals(17, tn.getCost());
		Assert.assertEquals(0, tn.getReserved());
	}
	
	@Test(expected=DuplicateTransactionException.class)
	public void testCredit_duplicateMessage() throws DuplicateTransactionException{
		// call test method
		userBalanceService.credit(10, org, "createdFor", "createdBy", "message 1", user);
		userBalanceService.credit(11, org, "createdFor", "createdBy", "message 1", user);
	}
	
	private void checkTransaction(Long transactionId, int cost, int reserved) {
		Transaction t = generalDAO.find(Transaction.class, transactionId);
		Assert.assertEquals(cost, t.getCost());
		Assert.assertEquals(reserved, t.getReserved());
	}
}
