package org.celllife.mobilisr.test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.apache.http.HttpHost;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.localserver.RequestBasicAuth;
import org.apache.http.localserver.ResponseBasicUnauthorized;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.client.impl.MobilisrClientImpl;
import org.junit.After;
import org.junit.Before;

import com.sun.jersey.api.client.ClientResponse.Status;

public abstract class BaseHttpTest {

	protected static final String USERNAME = "resttest";
	protected static final String PASSWORD = "resttest";
	private LocalTestServer localServer;
	protected MobilisrClientImpl client;

	public BaseHttpTest() {
		super();
	}

	@Before
	public void setup() throws Exception {
		BasicHttpProcessor httpproc = new BasicHttpProcessor();
		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());
		httpproc.addInterceptor(new RequestBasicAuth());
		httpproc.addInterceptor(new ResponseBasicUnauthorized());
		
		ValidatorFactoryImpl vfactory = new ValidatorFactoryImpl();
		vfactory.setCountryRules(Arrays.asList(new MsisdnRule("SA", "27", "^27[1-9][0-9]{8}$")));
	
		localServer = new LocalTestServer(httpproc, null);
		localServer.start();
		client = new MobilisrClientImpl(getServerUrl(), USERNAME, PASSWORD, vfactory);
		registerHandlers(localServer);
	}

	/**
	 * Override this method to add handlers to the localServer before each test
	 * @param localServer
	 */
	protected void registerHandlers(LocalTestServer localServer2) {
		// do nothing
	}

	protected String getServerUrl() {
		return "http://"+ getServerHost().toHostString();
	}
	
	public LocalTestServer getLocalServer() {
		return localServer;
	}

	@After
	public void tearDown() throws Exception {
		if (localServer != null) {
			localServer.stop();
		}
	}

	/**
	 * Obtains the address of the local test server.
	 * 
	 * @return the test server host, with a scheme name of "http"
	 */
	protected HttpHost getServerHost() {
		InetSocketAddress address = (InetSocketAddress) localServer
				.getServiceAddress();
		return new HttpHost(address.getHostName(), address.getPort(), "http");
	}
	
	protected RestRequestHandler registerHandler(String url, Class<? extends MobilisrDto> dto) {
		return registerHandler("GET",url, dto);
	}
	
	protected RestRequestHandler registerHandler(String method, String url, Class<? extends MobilisrDto> dto) {
		return registerHandler(Status.OK.getStatusCode(), method, url, dto);
	}
	
	protected RestRequestHandler registerHandler(int returnStatus, String method, String url, Class<? extends MobilisrDto> dto) {
		RestRequestHandler handler = new RestRequestHandler(url, dto);
		handler.setExpectedMethod(method);
		handler.setSuccessCode(returnStatus);
		getLocalServer().register("*", handler);
		return handler;
	}
	
	protected ListRequestHandler registerListHandler(String url, Class<? extends MobilisrDto> dto) {
		ListRequestHandler handler = new ListRequestHandler(url, dto);
		getLocalServer().register("*", handler);
		return handler;
	}

}