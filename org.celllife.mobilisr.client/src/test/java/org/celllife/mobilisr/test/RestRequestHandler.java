package org.celllife.mobilisr.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequestHandler implements HttpRequestHandler {

	private static final Logger log = LoggerFactory
			.getLogger(RestRequestHandler.class);

	private final Class<? extends MobilisrDto> returnClass;

	private final String expectedUrl;

	private int status = HttpStatus.SC_OK;

	private String httpMethod = "GET";

	private Map<String,String> headers = new HashMap<String, String>();

	public RestRequestHandler(String expectedUrl, Class<? extends MobilisrDto> returnClass) {
		this.expectedUrl = expectedUrl;
		this.returnClass = returnClass;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		String uri = request.getRequestLine().getUri();
		if (!uri.equals(expectedUrl)){
			log.error("Unexpected url: {}", uri);
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		
		String method = request.getRequestLine().getMethod();
		if (httpMethod != null && !method.equals(httpMethod)){
			log.error("Unexpected method: {}", httpMethod);
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		
		try {
			if (returnClass != null){
				MobilisrDto returnObject = getReturnObject();
				
				String string = MarshallerUtil.marshallToString(returnObject);
				
				log.trace("Rest handler response: \n{}", string);
				StringEntity entity = new StringEntity(string, HTTP.UTF_8);
				entity.setContentType("text/xml");
				response.setEntity(entity);
			}			
			response.setStatusCode(status);
			for (Entry<String, String> header : headers.entrySet()) {
				response.addHeader(header.getKey(), header.getValue());
			}
		} catch (Exception e) {
			log.error("Error handling request", e);
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
	}

	protected MobilisrDto getReturnObject() {
		MobilisrDto returnObject = DtoMockFactory._().on(returnClass).create();
		return returnObject;
	}
	
	public Class<? extends MobilisrDto> getReturnClass() {
		return returnClass;
	}

	public void setSuccessCode(int status) {
		this.status = status;
	}
	
	public void setExpectedMethod(String httpMethod){
		this.httpMethod = httpMethod;
	}

	public void addReturnHeader(String name, String value) {
		headers.put(name, value);
	}
}
