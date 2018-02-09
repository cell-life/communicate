package org.celllife.mobilisr.service.qrtz.impl;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class DummyJob extends QuartzJobBean {

	private int timeout;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		try{
			Thread.sleep(timeout);
		} catch (Exception e) {
		}
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}