package org.celllife.mobilisr.client.command;

import junit.framework.Assert;

import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.celllife.mobilisr.test.BasicAuthHandler;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

public class AuthenticationTest extends BaseHttpTest{
	
	@Test
	public void testAuthenticationFail(){
		getLocalServer().register("/plainurl", new BasicAuthHandler());
		GetCommand command = new GetCommand(getServerUrl(), "plainurl", new Object[]{});
		try {
			command.execute(String.class);
			Assert.fail("Expected RestCommandException");
		} catch (RestCommandException e) {
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), e.getStatusCode());
		}
	}
	
	@Test
	public void testAuthenticationSuccess(){
		BasicAuthHandler handler = new BasicAuthHandler();
		handler.setAcceptedAuthToken(USERNAME + ":" + PASSWORD);
		getLocalServer().register("/plainurl", handler);
		GetCommand command = new GetCommand(getServerUrl(), "plainurl", new Object[]{});
		command.setAuthenticator(new BasicAuthenticator(USERNAME, PASSWORD));
		try {
			command.execute(String.class);
			Assert.assertTrue(true);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}

}
