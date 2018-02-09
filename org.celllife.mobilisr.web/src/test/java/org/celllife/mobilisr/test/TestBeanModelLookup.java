package org.celllife.mobilisr.test;

import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;

public class TestBeanModelLookup extends BeanModelLookup {
	@Override
	public BeanModelFactory getFactory(Class<?> bean) {
		// just return a Test factory
		return new TestBeanModelFactory();
	}
}