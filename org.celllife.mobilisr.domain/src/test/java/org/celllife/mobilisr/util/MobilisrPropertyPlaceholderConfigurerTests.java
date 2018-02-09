package org.celllife.mobilisr.util;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class MobilisrPropertyPlaceholderConfigurerTests {

	private static final String PREFIX = "prefix";

	@Test
	public void testConvertProperties(){
		Properties props = new Properties();
		props.put("noprefix", "noprefixvalue");
		props.put(PREFIX + "." + PREFIX, "multiprefixvalue");
		props.put(PREFIX + ".prop", "singleprefixvalue");
		MobilisrPropertyPlaceholderConfigurer conf = new MobilisrPropertyPlaceholderConfigurer();
		conf.setPrefix(PREFIX);
		conf.convertProperties(props);
		
		Assert.assertEquals(props.get("noprefix"), "noprefixvalue");
		Assert.assertEquals(props.get(PREFIX), "multiprefixvalue");
		Assert.assertEquals(props.get("prop"), "singleprefixvalue");
	}
	
	@Test
	public void testConvertPropertiesNullPrefix(){
		Properties props = new Properties();
		props.put("noprefix", "noprefixvalue");
		props.put(PREFIX + "." + PREFIX, "multiprefixvalue");
		props.put(PREFIX + ".prop", "singleprefixvalue");
		MobilisrPropertyPlaceholderConfigurer conf = new MobilisrPropertyPlaceholderConfigurer();
		conf.convertProperties(props);
		
		Assert.assertEquals(props.get("noprefix"), "noprefixvalue");
		Assert.assertEquals(props.get(PREFIX + "." + PREFIX), "multiprefixvalue");
		Assert.assertEquals(props.get(PREFIX + ".prop"), "singleprefixvalue");
	}
	
	@Test
	public void testConvertPropertiesPrefixWithoutDot(){
		Properties props = new Properties();
		props.put("noprefix", "noprefixvalue");
		props.put(PREFIX + PREFIX, "multiprefixvalue");
		props.put(PREFIX + "prop", "singleprefixvalue");
		MobilisrPropertyPlaceholderConfigurer conf = new MobilisrPropertyPlaceholderConfigurer();
		conf.setPrefix(PREFIX);
		conf.convertProperties(props);
		
		Assert.assertEquals(props.get("noprefix"), "noprefixvalue");
		Assert.assertEquals(props.get(PREFIX), "multiprefixvalue");
		Assert.assertEquals(props.get("prop"), "singleprefixvalue");
	}
}

