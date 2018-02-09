package org.celllife.mobilisr.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.celllife.mobilisr.dao.api.TransactionDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.BalanceOutOfSyncException;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class TransactionDAOImplTests extends AbstractDBTest {
	
	@Autowired
	private TransactionDAO transactionDao;
	
	private User user;

	private Long id;
	
	@Before
	public void beforeTest(){
		Search s = new Search(User.class);
		s.addFilterEqual(User.PROP_USERNAME, "temp_username 0");
		user = (User) getGeneralDao().searchUnique(s);
		
		Transaction transaction = createTransaction(0,10, null);
		id = transaction.getId();
	}

	private Transaction createTransaction(int cost, int reserved, Transaction parent) {
		Transaction transaction = new Transaction(cost,reserved, "createdFor", "createdBy", "message", user, user.getOrganization());
		transaction.setParent(parent);
		transactionDao.save(transaction);
		return transaction;
	}
	
	@Test
	public void testReserve(){
		Organization org = user.getOrganization();
		int initialReserved = org.getReserved();
		Long ref = transactionDao.reserveAmount(9, org, "createdFor", "createdBy", "message", user);
		Assert.assertNotNull(ref);
		
		Transaction find = transactionDao.find(ref);
		Assert.assertEquals(0, find.getCost());
		Assert.assertEquals(9, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialReserved+9, org.getReserved());
	}
	
	@Test
	public void testReserve_orgBalanceOutOfDate(){
		Organization org = user.getOrganization();
		Organization outOfDate = getGeneralDao().find(Organization.class, org.getId());
		
		int initialReserved = org.getReserved();
		Long ref = transactionDao.reserveAmount(9, org, "createdFor", "createdBy", "message", user);
		Assert.assertNotNull(ref);
		
		Transaction find = transactionDao.find(ref);
		Assert.assertEquals(0, find.getCost());
		Assert.assertEquals(9, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialReserved+9, org.getReserved());
		Assert.assertEquals(initialReserved, outOfDate.getReserved());

		transactionDao.reserveAmount(7, outOfDate, "createdFor", "createdBy", "message", user);

		getGeneralDao().refresh(outOfDate);
		Assert.assertEquals(initialReserved+9+7, outOfDate.getReserved());
	}
	
	@Test
	public void testDebitOrgAccount() throws TransactionNotFoundException{
		Organization org = user.getOrganization();
		int initialBalance = org.getBalance();
		int initialReserved = org.getReserved();
		Transaction t = transactionDao.debitOrgAccount(id, 7, 9, "createdFor", "createdBy","message", user);
		Assert.assertNotNull(t);
		
		Transaction find = transactionDao.find(t.getId());
		Assert.assertEquals(-7, find.getCost());
		Assert.assertEquals(-9, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialBalance-7, org.getBalance());
		Assert.assertEquals(initialReserved-9, org.getReserved());
	}
	
	@Test(expected=TransactionNotFoundException.class)
	public void testDebitOrgAccount_missingRef() throws TransactionNotFoundException{
		transactionDao.debitOrgAccount(19L, 7, 0, "createdFor", "createdBy","message", user);
	}
	
	@Test
	public void testCreateTransaction(){
		Organization org = user.getOrganization();
		int initialBalance = org.getBalance();
		
		Long ref = transactionDao.createTransaction(19, user.getOrganization(), "createdFor", "createdBy", "message", user);
		
		Transaction find = transactionDao.find(ref);
		Assert.assertEquals(19, find.getCost());
		Assert.assertEquals(0, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialBalance+19, org.getBalance());
	}
	
	@Test
	public void testUnreserve() throws TransactionNotFoundException{
		Organization org = user.getOrganization();
		int initialReserved = org.getReserved();
		
		Transaction t = createTransaction(0, 10, null);
		Long unreserve = transactionDao.unreserve(t.getId(), "createdFor", "createdBy", "message", user);
		Transaction find = transactionDao.find(unreserve);
		Assert.assertEquals(-10, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialReserved-10, org.getReserved());
	}
	
	@Test
	public void testUnreserve_noneToUnreserve() throws TransactionNotFoundException{
		Transaction t = createTransaction(0, 10, null);
		createTransaction(0, -10, t);
		Long unreserve = transactionDao.unreserve(t.getId(), "createdFor", "createdBy", "message", user);
		Assert.assertNull(unreserve);
	}
	
	@Test
	public void testUnreserve2() throws TransactionNotFoundException{
		Organization org = user.getOrganization();
		int initialReserved = org.getReserved();
		
		Long unreserve = transactionDao.unreserve(5, org, "createdFor", "createdBy", "message", user);
		Transaction find = transactionDao.find(unreserve);
		Assert.assertEquals(-5, find.getReserved());
		
		getGeneralDao().refresh(org);
		Assert.assertEquals(initialReserved-5, org.getReserved());
	}
	
	@Test
	public void testGetSummaryTransaction(){
		List<Transaction> findAll = transactionDao.findAll();
		for (Transaction transaction : findAll) {
			transactionDao.remove(transaction);
		}
		
		createTransaction(0, 13, null);
		createTransaction(-3, -3, null);
		createTransaction(-1, -1, null);
		createTransaction(0, 2, null);
		TransactionSummary summaryTransaction = transactionDao.getSummaryTransaction(user.getOrganization());
		Assert.assertEquals(13-3-1+2, summaryTransaction.getReserved().intValue());
		Assert.assertEquals(0-3-1-0, summaryTransaction.getCost().intValue());
		Assert.assertEquals(4, summaryTransaction.getTransactionCount().intValue());
	}
	
	@Test
	public void testGetSummaryTransaction_single() throws TransactionNotFoundException{
		Transaction transaction = createTransaction(-9, 3, null);
		TransactionSummary summaryTransaction = transactionDao.getSummaryTransaction(transaction.getId());
		Assert.assertEquals(3, summaryTransaction.getReserved().intValue());
		Assert.assertEquals(-9, summaryTransaction.getCost().intValue());
		Assert.assertEquals(1, summaryTransaction.getTransactionCount().intValue());
	}
	
	@Test
	public void testGetSummaryTransaction_multiple(){
		Transaction t1 = createTransaction(-9, 3, null);
		Transaction t2 = createTransaction(1, 2, null);
		Transaction t3 = createTransaction(-3, 7, null);
		Transaction t4 = createTransaction(5, -13, null);
		List<Long> refList = Arrays.asList(t1.getId(), t2.getId(), t3.getId(), t4.getId());
		TransactionSummary summaryTransaction = transactionDao.getSummaryTransaction(refList);
		Assert.assertEquals(-1, summaryTransaction.getReserved().intValue());
		Assert.assertEquals(-6, summaryTransaction.getCost().intValue());
		Assert.assertEquals(4, summaryTransaction.getTransactionCount().intValue());
	}
	
	@Test
	public void testGetSummaryTransaction_createdBy(){
		List<Transaction> findAll = transactionDao.findAll();
		transactionDao.remove(findAll.toArray(new Transaction[findAll.size()]));
		
		createTransaction(-9, 3, null);
		createTransaction(1, 2, null);
		createTransaction(-3, 7, null);
		createTransaction(5, -13, null);
		TransactionSummary summaryTransaction = transactionDao.getSummaryTransaction("createdFor");
		Assert.assertEquals(-1, summaryTransaction.getReserved().intValue());
		Assert.assertEquals(-6, summaryTransaction.getCost().intValue());
		Assert.assertEquals(4, summaryTransaction.getTransactionCount().intValue());
	}
	
	/*@Test(timeout=12000)
	public void testSynchronized() throws Exception {
		final Organization org = user.getOrganization();
		try {
			// setup the balance
			List<Transaction> findAll = transactionDao.findAll();
			transactionDao.remove(findAll.toArray(new Transaction[findAll.size()]));
			transactionDao.createTransaction(10000, user.getOrganization(), "you", "me", "setup balance", user);
			transactionDao.reconciliateOrganizationBalance(org);
		} catch (BalanceOutOfSyncException e) {
			// this exception is allowed because we are setting up the balance
		}
		// create a lot of threads to simulate billing activity 
		int numberOfThreads = 500;
		debitCounter = 1;
		for (int i=0; i<numberOfThreads; i++) {
			final int id = i;
			new Thread()
		    {
		        public void run() {
		        	try {
						Thread.sleep((long)(new Random().nextInt(4000) + 1000));
						debit(id, org, user);
					} catch (Exception e) {
						e.printStackTrace();
						Assert.fail(e.getMessage());
					}
		        }
		    }.start();
		}
		do {
			Thread.sleep(100);
		} while (debitCounter <= numberOfThreads);
	}
	
	static int debitCounter = 1;

	void debit(int id, Organization org, User user) throws TransactionNotFoundException, InsufficientBalanceException {
		// do the reserve + debit
		Long reference = transactionDao.reserveAndCheckAmount(7, org, "createdFor", "createdBy", "message"+id, user);
		transactionDao.debitOrgAccount(reference, 7, 7, "createdFor", "createdBy", "message"+id, user);
		// now check the balance
		try {
			transactionDao.reconciliateOrganizationBalance(org);
		} catch (BalanceOutOfSyncException e) {
			Assert.fail("balance was not reconcilated (exception)");
		}
		debitCounter++;
	}*/
	
}
