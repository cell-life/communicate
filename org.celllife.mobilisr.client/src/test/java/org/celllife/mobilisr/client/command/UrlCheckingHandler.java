package org.celllife.mobilisr.client.command;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Assert;

class UrlCheckingHandler implements HttpRequestHandler {

	private String expectedUri;
	private final String method;
	
	public UrlCheckingHandler(String method){
		this.method = method;
	}

	public UrlCheckingHandler(String method, String expectedUri) {
		this.method = method;
		this.expectedUri = expectedUri;
	}

	public void setExpectedUri(String expectedUri) {
		this.expectedUri = expectedUri;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {

		String method = request.getRequestLine().getMethod();
		String uri = request.getRequestLine().getUri();
		Assert.assertEquals(this.method, method);
		Assert.assertEquals(expectedUri, uri);

		response.setStatusCode(HttpStatus.SC_OK);
	}

}