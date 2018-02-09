package org.celllife.mobilisr.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class BasicAuthHandler implements HttpRequestHandler {
	
	private HttpRequestHandler wrapped;
	private String token;

	public BasicAuthHandler() {
	}
	
	public BasicAuthHandler(HttpRequestHandler wrapped) {
		this.wrapped = wrapped;
	}
	
	public void setAcceptedAuthToken(String token){
		this.token = token;
	}

	public void handle(final HttpRequest request,
			final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {
		String creds = (String) context.getAttribute("creds");
		if (creds == null || !creds.equals(token)) {
			response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
		} else if (wrapped != null){
			wrapped.handle(request, response, context);
		} else {
			response.setStatusCode(HttpStatus.SC_OK);
		}
	}

}