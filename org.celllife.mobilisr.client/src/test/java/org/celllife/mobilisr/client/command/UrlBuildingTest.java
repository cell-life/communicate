package org.celllife.mobilisr.client.command;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.celllife.mobilisr.client.exception.RestCommandException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class UrlBuildingTest {

	private static final Logger LOG = LoggerFactory.getLogger(UrlBuildingTest.class);
	
	class TestCommand extends AbstractRestCommand {

		private URI expectedUri;
		

		public TestCommand(String baseUrl, String relativeUrl,
				Object[] urlParameters, String expectedUrl) {
			super(baseUrl, relativeUrl, urlParameters);
			try {
				this.expectedUri = new URI(expectedUrl);
			} catch (URISyntaxException e) {
				LOG.error(e.getMessage());
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
		}

		@Override
		protected ClientResponse executeInternal(WebResource resource) {
			URI actualUri = resource.getURI();
			Assert.assertEquals(expectedUri, actualUri);
			return null;

		}

	}

	@Test
	public void testPlainUrl() {
		String expectedUrl = "/plainurl";
		String relativeUrl = "plainurl";
		Object[] parameters = new Object[] {};
		testUrl(relativeUrl, parameters, expectedUrl);
	}

	@Test
	public void testStringParameter() {
		String expectedUrl = "/plainurl/someParam";
		String relativeUrl = "plainurl/{param1}";
		Object[] parameters = new Object[] { "someParam" };
		testUrl(relativeUrl, parameters, expectedUrl);
	}

	@Test
	public void testIntParameter() {
		String expectedUrl = "/plainurl/123";
		String relativeUrl = "plainurl/{param1}";
		Object[] parameters = new Object[] { 123 };
		testUrl(relativeUrl, parameters, expectedUrl);
	}

	@Test
	public void testLongParameter() {
		String expectedUrl = "/plainurl/123";
		String relativeUrl = "plainurl/{param1}";
		Object[] parameters = new Object[] { 123l };
		testUrl(relativeUrl, parameters, expectedUrl);
	}

	@Test
	public void testMultiParameter() {
		String expectedUrl = "/plainurl/123/test/456";
		String relativeUrl = "plainurl/{param1}/{param2}/{param3}";
		Object[] parameters = new Object[] { 123l, "test", 456 };
		testUrl(relativeUrl, parameters, expectedUrl);
	}

	@Test
	public void testQueryParameter() {
		String relativeUrl = "plainurl";
		HashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("p1", "456");
		map.put("p2", 123);
		String expectedUrl = "/plainurl?p1=456&p2=123";
		testUrl(relativeUrl, null, map, expectedUrl);
	}

	private void testUrl(String relativeUrl, Object[] urlParameters,
			String expectedUrl) {
		testUrl(relativeUrl, urlParameters, null, expectedUrl);
	}

	private void testUrl(String relativeUrl, Object[] urlParameters,
			Map<String, Object> queryParameters, String expectedUrl) {
		TestCommand testCommand = new TestCommand(getServerUrl(), relativeUrl,
				urlParameters, getServerUrl() + expectedUrl);
		testCommand.setQueryParameters(queryParameters);
		try {
			testCommand.execute(String.class);
		} catch (RestCommandException e) {
			// ignore
		}
	}

	protected String getServerUrl() {
		return "http://localhost";
	}

}
