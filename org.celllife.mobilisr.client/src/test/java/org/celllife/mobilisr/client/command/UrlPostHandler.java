package org.celllife.mobilisr.client.command;

import java.io.IOException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

class UrlPostHandler implements HttpRequestHandler {

	private String expectedUri;
	private Object expectedEntityString;
	private final int returnStatus;
	private final String expectedMethod;
	
	public UrlPostHandler(String method, int returnStatus){
		this.expectedMethod = method;
		this.returnStatus = returnStatus;
	}

	public void setExpectedFields(String expectedUri, String expectedEntityString) {
		this.expectedUri = expectedUri;
		this.expectedEntityString = expectedEntityString;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		
		String method = request.getRequestLine().getMethod();
		String uri = request.getRequestLine().getUri();
		Assert.assertEquals(expectedMethod, method);
		Assert.assertEquals(expectedUri, uri);

		if (request instanceof HttpEntityEnclosingRequest){
			HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
			String entity = EntityUtils.toString(entityRequest.getEntity());
			Assert.assertEquals(expectedEntityString, entity);
		} else {
			Assert.fail("No entity in post");
		}
		
		response.setStatusCode(returnStatus);
		response.setHeader("Location", uri + "/4");
	}

}