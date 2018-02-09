package org.celllife.mobilisr.test;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.mchange.v2.c3p0.PooledDataSource;

@Component
public class ResourceMonitor {

	private static final Logger log = LoggerFactory.getLogger(ResourceMonitor.class);
	
	@Autowired
	private PooledDataSource dataSource;
	
	@Autowired(required=false)
	@Qualifier("messageOutTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	public ResourceMonitor() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				do {
					try {
						pollDataSource();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} while (true);
			}
		}).start();
	}
	
	public void pollDataSource() throws SQLException{
		if (dataSource != null){
			int numActive = dataSource.getNumBusyConnectionsDefaultUser();
			int numIdle = dataSource.getNumIdleConnectionsDefaultUser();
			int numTotal = dataSource.getNumConnectionsDefaultUser();
			log.debug("--> CONNECTION POOL STATS: numActive={}, numIdle={}, numTotal={}",
					new Object[]{numActive, numIdle, numTotal});
		}
		
		if (taskExecutor != null){
			int poolSize = taskExecutor.getPoolSize();
			int activeCount = taskExecutor.getActiveCount();
			long completedTaskCount = taskExecutor.getThreadPoolExecutor().getCompletedTaskCount();
			long taskCount = taskExecutor.getThreadPoolExecutor().getTaskCount();
			int qsize = taskExecutor.getThreadPoolExecutor().getQueue().size();
			log.debug("--> MESSAGE THREAD POOL STATS: poolSi={}, numActive={}," +
					" completedTaskCount={}, taskCount={}, queueSize={}", 
					new Object[]{poolSize, activeCount, completedTaskCount, taskCount,
					qsize});
		}
	}
}
