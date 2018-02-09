package org.celllife.mobilisr.client.command;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class DeleteCommand extends AbstractRestCommand {

	public DeleteCommand(String baseUrl, String relativeUrl){
		super(baseUrl, relativeUrl, null);

	}

	public DeleteCommand(String baseUrl, String relativeUrl, Object... urlParameters) {
		super(baseUrl, relativeUrl, urlParameters);

	}

	public ClientResponse executeInternal(WebResource resource) {
		ClientResponse response =  resource.accept(MediaType.TEXT_XML).delete(ClientResponse.class);
		return response;
	}

}
