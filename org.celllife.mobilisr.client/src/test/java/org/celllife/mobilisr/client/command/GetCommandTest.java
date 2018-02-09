package org.celllife.mobilisr.client.command;

import org.apache.http.localserver.LocalTestServer;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.celllife.mobilisr.test.ListRequestHandler;
import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

public class GetCommandTest extends BaseHttpTest {

	private UrlCheckingHandler handler;
	
	@Override
	protected void registerHandlers(LocalTestServer localServer) {
		handler = new UrlCheckingHandler("GET");
		localServer.register("*", handler);
	}
	
	@Test
	public void testGetCommand_basic(){
		handler.setExpectedUri("/simpleurl");
		try {
			new GetCommand(getServerUrl(), "simpleurl").execute(String.class);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetCommand_returnsErrors() {
		getLocalServer().unregister("*");
		
		ListRequestHandler handler = registerListHandler("/simpleurl", ErrorDto.class);
		handler.setReturnSize(1);
		handler.setSuccessCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
		handler.setExpectedMethod("GET");
		
		try {
			new GetCommand(getServerUrl(), "simpleurl").execute(String.class);
			Assert.fail("Expected exception");
		} catch (RestCommandException e) {
			PagedListDto<ErrorDto> errors = e.getErrors();
			Assert.assertNotNull(errors);
			Assert.assertEquals(1, errors.size());
		}
	}
}
