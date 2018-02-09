package org.celllife.mobilisr.client.command;

import javax.ws.rs.core.MediaType;

import org.celllife.mobilisr.client.exception.RestCommandException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class PostCommand extends AbstractRestCommand {
	
	private final Object post;
	
	public PostCommand(String baseUrl, String relativeUrl, Object post){
		super(baseUrl, relativeUrl, null);
		this.post = post;
	}

	public PostCommand(String baseUrl, String relativeUrl, Object post, Object... urlParameters) {
		super(baseUrl, relativeUrl, urlParameters);
		this.post = post;
	}

	public ClientResponse executeInternal(WebResource resource) {
		ClientResponse response = resource.accept(MediaType.TEXT_XML).post(ClientResponse.class, post);
		return response;
	}

	@Override
	public <T> T execute(Class<T> clazz) throws RestCommandException {
		return super.execute(clazz, Status.CREATED.getStatusCode());
	}
	
	@Override
	public <T> T execute(GenericType<T> type) throws RestCommandException {
		return super.execute(type, Status.CREATED.getStatusCode());
	}
}