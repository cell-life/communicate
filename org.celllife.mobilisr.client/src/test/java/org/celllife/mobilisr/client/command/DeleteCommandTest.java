package org.celllife.mobilisr.client.command;


import org.apache.http.localserver.LocalTestServer;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.junit.Assert;
import org.junit.Test;

public class DeleteCommandTest extends BaseHttpTest {

	private UrlCheckingHandler handler;
	
	@Override
	protected void registerHandlers(LocalTestServer localServer) {
		handler = new UrlCheckingHandler("DELETE");
		localServer.register("*", handler);
	}
	
	@Test
	public void testDeleteCommand_basic(){
		handler.setExpectedUri("/simpleurl");
		try {
			new DeleteCommand(getServerUrl(), "simpleurl").execute(String.class);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
}
