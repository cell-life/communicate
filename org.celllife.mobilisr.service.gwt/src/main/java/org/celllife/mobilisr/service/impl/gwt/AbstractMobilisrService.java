package org.celllife.mobilisr.service.impl.gwt;

import javax.servlet.ServletException;

import net.sf.gilead.core.PersistentBeanManager;
import net.sf.gilead.gwt.PersistentRemoteService;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public abstract class AbstractMobilisrService extends PersistentRemoteService {

	private static final long serialVersionUID = -4118892727312358610L;
	
	private WebApplicationContext springContext;

	@Override
	public void init() throws ServletException {
		super.init();

		PersistentBeanManager bm = (PersistentBeanManager) getBean("persistentBeanManager");
		setBeanManager(bm);
	}

	protected ApplicationContext getApplicationContext() {
		if (springContext == null) {
			springContext = WebApplicationContextUtils
					.getWebApplicationContext(getServletContext());
		}
		return springContext;
	}

	protected Object getBean(String beanName) {
		return getApplicationContext().getBean(beanName);
	}

}
