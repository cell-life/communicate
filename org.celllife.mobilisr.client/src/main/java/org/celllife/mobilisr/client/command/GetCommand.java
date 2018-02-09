package org.celllife.mobilisr.client.command;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GetCommand extends AbstractRestCommand {
	
	public GetCommand(String baseUrl, String relativeUrl){
		super(baseUrl, relativeUrl, null);
	}

	public GetCommand(String baseUrl, String relativeUrl, Object... urlParameters) {
		super(baseUrl, relativeUrl, urlParameters);
	}

	public ClientResponse executeInternal(WebResource resource) {
		ClientResponse response = resource.accept(MediaType.TEXT_XML).get(ClientResponse.class);
		return response;
	}

}