package org.celllife.mobilisr.service.filter;

import junit.framework.Assert;

import org.celllife.mobilisr.service.exception.TriggerException;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.StringParameter;
import org.junit.Test;

public class KeywordFilterTests {

	@Test
	public void testMatches() throws TriggerException {
		Filter kw = getFilter("test");
		boolean matches = kw.matches(" test this is a test");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatchesBlankMsg() throws TriggerException {
		Filter kw = getFilter("test");
		boolean matches = kw.matches("");
		Assert.assertFalse(matches);
	}
	
	@Test(expected=TriggerException.class)
	public void testMatchesBlankKw() throws TriggerException {
		Filter kw = getFilter("");
		boolean matches = kw.matches("test test");
		Assert.assertFalse(matches);
	}
	
	@Test
	public void testMatchesMultiKw() throws TriggerException {
		Filter kw = getFilter(" test test");
		boolean matches = kw.matches("test test 123");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatchesCase() throws TriggerException {
		Filter kw = getFilter("TEsT ");
		boolean matches = kw.matches("test test 123");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatchesWholeWord() throws TriggerException {
		Filter kw = getFilter("test ");
		boolean matches = kw.matches("testtest 123");
		Assert.assertFalse(matches);
	}
	
	@Test
	public void testMatchesWholeMessage() throws TriggerException {
		Filter kw = getFilter("test");
		boolean matches = kw.matches("test");
		Assert.assertTrue(matches);
	}

	private Filter getFilter(String keyword) {
		Filter kw = new KeywordFilter();
		StringParameter param = new StringParameter(KeywordFilter.KEYWORD,"");
		param.setValue(keyword);
		kw.init(YamlUtils.dumpParameterList(param));
		return kw;
	}
	
	@Test
	public void testMatches_multiple() throws TriggerException {
		Filter kw = getFilter("test,monkey,baboon,hello");
		boolean matches = kw.matches("test this is");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatches_multipleWithSpace() throws TriggerException {
		Filter kw = getFilter("test,monkey,baboon,hello");
		boolean matches = kw.matches(" test this is ");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatches_multipleNotFirst() throws TriggerException {
		Filter kw = getFilter("horse,monkey,baboon,hello");
		boolean matches = kw.matches("baboon this is ");
		Assert.assertTrue(matches);
	}
	
	@Test
	public void testMatches_multipleFalse() throws TriggerException {
		Filter kw = getFilter("horse,monkey,baboon,hello");
		boolean matches = kw.matches("babon this is ");
		Assert.assertFalse(matches);
	}
	
	@Test
	public void testMatches_quotes() throws TriggerException {
		Filter kw = getFilter("TEST,TSET,\"TEST\"");
		boolean matches = kw.matches("\"TEST\" 123");
		Assert.assertTrue(matches);
	}
}
