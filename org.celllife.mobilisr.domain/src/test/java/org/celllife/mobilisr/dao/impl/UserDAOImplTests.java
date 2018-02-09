package org.celllife.mobilisr.dao.impl;

import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

public class UserDAOImplTests extends AbstractDBTest {

	@Autowired
	private UserDAO userDao;

	@Autowired
	private SessionFactory sessionFactory;
	
	private TransactionTemplate tx;

    @Autowired
    public void setPtm(PlatformTransactionManager ptm) {
        tx = new TransactionTemplate(ptm);
    }

    @Test
    @Ignore("manual test only")
    public void doTestCache() {
        // Using programmatic transaction management since we need 2 transactions
        // inside the same method

        // 1st attempt
        tx.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                testCache();
            }
        });

        // 2nd attempt
        tx.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                testCache();
            }
        });

    }

	protected void testCache() {
        final String cacheRegion = User.class.getCanonicalName();

        SecondLevelCacheStatistics settingsStatistics = sessionFactory.getStatistics().
            getSecondLevelCacheStatistics(cacheRegion);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        userDao.findAll();
        stopWatch.stop();
        System.out.println("Query time : " + stopWatch.getTotalTimeSeconds());
        System.out.println(settingsStatistics);
	}

}
