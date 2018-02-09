package org.celllife.mobilisr.client.command;

import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public abstract class AbstractRestCommand {

	private Authenticator authenticator;

	private final String relativeUrl;
	private String baseUrl;
	private final Object[] parameters;
	private Map<String, Object> queryParameters;

	private static final Logger log = LoggerFactory
			.getLogger(AbstractRestCommand.class);

	private ClientResponse response;

	public AbstractRestCommand(String baseUrl, String relativeUrl,
			Object[] urlParameters) {
		this.baseUrl = baseUrl;
		this.relativeUrl = relativeUrl;
		this.parameters = urlParameters;
	}

	public void setQueryParameters(Map<String, Object> parameters) {
		queryParameters = parameters;
	}

	public void addQueryParameter(String name, Object value) {
		if (queryParameters == null) {
			queryParameters = new LinkedHashMap<String, Object>();
		}
		queryParameters.put(name, value);
	}

	public <T> T execute(Class<T> clazz) throws RestCommandException {
		return execute(clazz, ClientResponse.Status.OK.getStatusCode());
	}
	
	public <T> T execute(GenericType<T> type) throws RestCommandException {
		return execute(type, ClientResponse.Status.OK.getStatusCode());
	}
	
	public <T> T execute(GenericType<T> type, int statusCode) throws RestCommandException{
		try {
			execute(statusCode);
			Type genericType = type.getType();
			if (genericType.equals(Void.class)){
				return null;
			}
			return response.getEntity(type);
		} catch (ClientHandlerException e) {
			throw new RestCommandException("Error processing response", e);
		} catch (UniformInterfaceException e) {
			return null;
		}
	}

	public <T> T execute(Class<T> clazz, int statusCode)
			throws RestCommandException {
		execute(statusCode);
		if (clazz.equals(Void.class)){
			return null;
		}
		return response.getEntity(clazz);
	}
	
	private ClientResponse execute(int statusCode) throws RestCommandException{
		setAuthentication();
		WebResource resource = buildResrouce();
		response = executeInternal(resource);
		clearAuthentaciton();
		if (response == null) {
			log.debug("Command response is null");
			RestCommandException exception = new RestCommandException("Null response from server");
			exception.setRequestUrl(resource.getURI().toString());
			throw exception;
		} else if (response.getStatus() != statusCode){
			log.debug("Command response statuscode does not match expected status code. [expected={}] [actual={}]", 
					statusCode, response.getStatus());
			RestCommandException exception = new RestCommandException(response.getStatus(), resource.getURI().toString());
			
			boolean hasEntity = response.hasEntity();
			if (hasEntity){
				processErrorEntity(response, exception);
			}
			throw exception;
		}
		return response;
	}

	private void processErrorEntity(ClientResponse response, RestCommandException exception) {
		MediaType type = response.getType();
		log.debug("Response type [{}]", type);
		if (type.isCompatible(MediaType.TEXT_XML_TYPE)){
			try {
				log.debug("Checking for returned error list");
				@SuppressWarnings("unchecked")
				PagedListDto<ErrorDto> errors = response.getEntity(PagedListDto.class);
				if (errors != null && !errors.isEmpty() && 
						errors.getElements().get(0).getClass().isAssignableFrom(ErrorDto.class)){
					log.debug("Error list returned [size={}]", errors.size());
					exception.setErrors(errors);
				}
			} catch (Exception ignore) { 
				/*ignore exception*/
				log.info("Unable to get errors from response. [message={}]", ignore.getMessage());
			}
		} else if (type.isCompatible(MediaType.TEXT_HTML_TYPE)
				|| type.isCompatible(MediaType.TEXT_PLAIN_TYPE)){
			try {
				log.debug("Checking for error response from server");
				String entity = response.getEntity(String.class);
				log.error("Error response from server [{}]", entity);
			} catch (Exception ignore) { 
				/*ignore exception*/
				log.info("Unable to get errors from response. [message={}]", ignore.getMessage());
			}
		}
	}

	protected URI buildUri() {
		UriBuilder builder = UriBuilder.fromPath(baseUrl).path(relativeUrl);
		if (queryParameters != null) {
			for (Entry<String, Object> entry : queryParameters.entrySet()) {
				builder.queryParam(entry.getKey(), entry.getValue());
			}
		}
		return builder.build(parameters);
	}

	protected WebResource buildResrouce() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		URI uri = buildUri();
		log.debug("Building webresource with [uri={}]", uri);
		WebResource service = client.resource(uri);
		return service;
	}

	private void setAuthentication() {
		if (authenticator != null) {
			log.debug("Setting authentication");
			Authenticator.setDefault(authenticator);
		} else {
			log.debug("No authentication");
		}

	}
	
	public String getHeader(String header){
		return response.getHeaders().getFirst(header);
	}

	private void clearAuthentaciton() {
		log.debug("Removing authentication (if set)");
		Authenticator.setDefault(null);
	}

	protected abstract ClientResponse executeInternal(WebResource resource);

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}
}
