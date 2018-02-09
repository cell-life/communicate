package org.celllife.mobilisr.client.model;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class ViewEntityModelTests {
	
	@Test
	public void testPutAndGetProperty(){
		Date value = new Date();
		ViewModel<Object> model = new ViewModel<Object>().putProperty("test", value);
		Object property = model.getProperty("test");
		Assert.assertEquals(value, property);
	}
	
	@Test
	public void testRemoveProperty(){
		Date value = new Date();
		ViewModel<Object> model = new ViewModel<Object>().putProperty("test", value);
		Object property = model.getProperty("test");
		Assert.assertEquals(value, property);
		model.removeProperty("test");
		property = model.getProperty("test");
		Assert.assertNull(property);
	}
	
	@Test
	public void testPutNullProperty(){
		ViewModel<Object> model = new ViewModel<Object>().putProperty("test", null);
		Object property = model.getProperty("test");
		Assert.assertNull(property);
	}
	
	@Test
	public void testPutNullName(){
		ViewModel<Object> model = new ViewModel<Object>().putProperty(null, "test");
		Assert.assertTrue(model.properties.isEmpty());
	}
	
	@Test
	public void testPutEmptyName(){
		ViewModel<Object> model = new ViewModel<Object>().putProperty("", "test");
		Assert.assertTrue(model.properties.isEmpty());
	}
	
	@Test
	public void testGetNullName(){
		Object prop = new ViewModel<Object>().getProperty(null);
		Assert.assertNull(prop);
	}
	
	@Test
	public void testGetEmptyName(){
		Object prop = new ViewModel<Object>().getProperty("");
		Assert.assertNull(prop);
	}
	
	@Test
	public void testSetAndGetViewMessage(){
		String message = "some message";
		ViewModel<Object> model = new ViewModel<Object>().setViewMessage(message);
		Assert.assertEquals(message, model.getViewMessage());
	}
	
	@Test
	public void testClearViewMessage(){
		String message = "some message";
		ViewModel<Object> model = new ViewModel<Object>().setViewMessage(message);
		Assert.assertEquals(message, model.getViewMessage());
		model.clearViewMessage();
		Assert.assertNull(model.getViewMessage());
	}

}
