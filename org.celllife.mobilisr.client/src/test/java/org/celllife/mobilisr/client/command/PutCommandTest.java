package org.celllife.mobilisr.client.command;


import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;

import org.apache.http.localserver.LocalTestServer;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.test.BaseHttpTest;
import org.celllife.mobilisr.test.MarshallerUtil;
import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

public class PutCommandTest extends BaseHttpTest {

	private UrlPostHandler handler;
	
	@Override
	protected void registerHandlers(LocalTestServer localServer) {
		handler = new UrlPostHandler("PUT", Status.OK.getStatusCode());
		localServer.register("*", handler);
	}
	
	@Test
	public void testPostCommand_basic(){
		handler.setExpectedFields("/simpleurl", "1234abcd");
		try {
			new PutCommand(getServerUrl(), "simpleurl", "1234abcd").execute(String.class);
		} catch (RestCommandException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetCommand_basic1() throws PropertyException, JAXBException{
		ContactDto dto = DtoMockFactory._().on(ContactDto.class).create();
		String expectedEntityString = MarshallerUtil.marshallToString(dto, false);
		handler.setExpectedFields("/simpleurl", expectedEntityString);
		try {
			new PutCommand(getServerUrl(), "simpleurl", dto).execute(String.class);
		} catch (RestCommandException e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
