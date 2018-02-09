package org.celllife.mobilisr.service.qrtz.beans;

import java.lang.reflect.Method;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.ReflectionUtils;

/**
 * @author Simon Kelly
 */
public class BeanMethodInvokerJob extends QuartzJobBean {
	
	private ApplicationContext applicationContext;
	private String beanName;
	private String methodName;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext)throws JobExecutionException {
		Object bean = applicationContext.getBean(beanName);
		Method method = ReflectionUtils.findMethod(bean.getClass(), methodName);
		if (method == null){
			throw new JobExecutionException("Method '" + methodName + "' not found for bean '" + beanName + "'");
		}
		ReflectionUtils.invokeMethod(method, bean);
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public void setMethodName(String methodName){
		this.methodName = methodName;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
