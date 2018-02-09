package org.celllife.mobilisr.test;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;

public class TestBeanModelFactory extends BeanModelFactory {
	// Have to create a subclass to have access to the constructor
	public class TestBeanModel extends BeanModel {
		private static final long serialVersionUID = -1471516237439502658L;
	}

	@Override
	protected BeanModel newInstance() {
		// Just create a simple instance of BeanModel
		// No need for specific Bean Model since they
		// won't be used by GXT widgets
		return new TestBeanModel();
	}
}