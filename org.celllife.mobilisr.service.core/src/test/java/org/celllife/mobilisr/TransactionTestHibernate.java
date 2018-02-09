package org.celllife.mobilisr;

import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.trg.search.Search;

@Ignore("This class is for experimenting with Hibernate transaction management")
public class TransactionTestHibernate extends AbstractServiceTest {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Test
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void testThreadTransaction(){
		System.out.println("asfasfasfds");
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		search.addFetch(User.PROP_ORGANIZATION);
		final User user = userDAO.searchUnique(search);
		
		Thread writeThread = new Thread(new Runnable(){

			@Override
			public void run() {
				TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				transactionTemplate.execute(new TransactionCallback<Object>() {

					@Override
					public Object doInTransaction(TransactionStatus arg0) {
						try{
							testThreadTransactionMethod2(user);
						}catch(Exception e){
							e.printStackTrace();
						}
						return null;
					}
				});
				
			}
			
		});
		
		Thread readThread = new Thread(new Runnable(){

			@Override
			public void run() {
				TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
				transactionTemplate.execute(new TransactionCallback<Object>() {

					@Override
					public Object doInTransaction(TransactionStatus arg0) {
						try{
							testThreadTransactionMethod1(user);
						}catch(Exception e){
							e.printStackTrace();
						}
						return null;
					}
				});
			}
			
		});
		
		writeThread.start();
		readThread.start();
		
		while(true){}
	}
	
//	@Transactional(propagation=Propagation.REQUIRED)
	public void testThreadTransactionMethod2(User user) throws InterruptedException{
		System.out.println("method 2");
		for (int i = 0; i < 5; i++) {
			Transaction clientTransaction = new Transaction(1, 1, "testing", "testing", "testing", user, user.getOrganization());
			getGeneralDao().save(clientTransaction);
			System.out.println("saved");
			
			Thread.sleep(1000);
		}
	}
	
//	@Transactional(propagation=Propagation.REQUIRED)
	public void testThreadTransactionMethod1(User user) throws InterruptedException{
		System.out.println("method 1");
		while(true){
//			double value = getGeneralDao().calcOrgBalanceByClientTransactions(user.getOrganization());
			//List<ClientAlert> clientAlertList = getGeneralDao().findAll(ClientAlert.class);
			System.out.println("Hello");
			Thread.sleep(1000);
		}
	}
	
	@Test
	//@Transactional(propagation=Propagation.REQUIRED)
	public void testThreadTransactionMethod1(){
		System.out.println("asfasfasfds");
	}
}
