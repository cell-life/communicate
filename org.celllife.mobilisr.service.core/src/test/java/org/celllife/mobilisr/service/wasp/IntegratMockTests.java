package org.celllife.mobilisr.service.wasp;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.MessageFormat;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class IntegratMockTests {

	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY = 10;
	private static final String MSISDN = "27785120109";
	private static final String MESSAGE_CONTENT = "Hello Bulk Testing message \n\rcontent \t ~!@#$%^&*()`/?>\n<,.\":;'}{|[]\'";
	private static final String SIMULATED_ERROR_MESSAGE = "simulated error message";
	private static final String WASP_SUCCESS_XML = "org/celllife/mobilisr/service/wasp/IntegratWaspSuccess.xml";
	private static final String WASP_FAIL_XML = "org/celllife/mobilisr/service/wasp/IntegratWaspFail.xml";
	protected static final int OUTCOME_SUCCESS = 0;
	protected static final int OUTCOME_FAIL_HTTP = 1;
	protected static final int OUTCOME_FAIL_NO_HOST = 2;
	protected static final int OUTCOME_FAIL_NULL = 3;

	private LocalTestServer localServer;

	@Mock
	HttpRequestHandler handler;
	private IntegratSendCommand integrat;

	@Before
	public void setUp() throws Exception {
		localServer = new LocalTestServer(null, null);
		localServer.register("/*", handler);
		localServer.start();
		
		String template = new IntegratHttpOutHandler().buildTemplate("test", "test", "servicecode","tag");
		integrat = new IntegratSendCommand("http://" + getServerHttp().toHostString(), template);
		integrat.setMaxRetries(MAX_RETRIES);
		integrat.setRetryDelay(RETRY_DELAY);
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
	protected HttpHost getServerHttp() {
		InetSocketAddress address = (InetSocketAddress) localServer
				.getServiceAddress();
		return new HttpHost(address.getHostName(), address.getPort(), "http");
	}
	
	@Test
	@Ignore("Getting redirect to OpenDNS search page")
	public void testIntegratSmsMTRunnable_unknownHost() throws Exception {
		integrat.setPostUrl("http://nonexistent/url");
		executeIntegratSmsMTRunnable(MAX_RETRIES+1, null, OUTCOME_FAIL_NO_HOST);
	}
	
	@Test
	public void testIntegratSmsMTRunnable_nullHost() throws Exception {
		integrat.setPostUrl(null);
		executeIntegratSmsMTRunnable(MAX_RETRIES, null, OUTCOME_FAIL_NULL);
	}

	@Test
	public void testIntegratSmsMTRunnable_success() throws Exception {
		testIntegratSmsMTRunnable(0, 1, OUTCOME_SUCCESS);
	}
	
	@Test
	public void testIntegratSmsMTRunnable_retriesOverMaxFailHttp() throws Exception {
		testIntegratSmsMTRunnable(4, MAX_RETRIES,  OUTCOME_FAIL_HTTP);
	}
	
	@Test
	public void testIntegratSmsMTRunnable_retries() throws Exception {
		testIntegratSmsMTRunnable(2, 3, OUTCOME_SUCCESS);
	}
	
	@Test
	public void testIntegratSmsMTRunnable_retriesOverMax() throws Exception {
		testIntegratSmsMTRunnable(4, MAX_RETRIES, OUTCOME_FAIL_HTTP);
	}
	
	public void testIntegratSmsMTRunnable(final int triesBeforeSuccess, final int expectedTries, final int outcome) throws Exception {
		final Integer sequenceNumber = Double.valueOf(Math.random() * Integer.MAX_VALUE).intValue();
		
		doAnswer(new Answer<Void>() {
			int tries = 1;

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				verifyRequest(invocation);
				sendResponse(invocation, tries > triesBeforeSuccess, outcome);
				tries++;
				return null;
			}

			private void verifyRequest(InvocationOnMock invocation) throws Exception {
				Object[] args = invocation.getArguments();
				HttpRequest request = (HttpRequest) args[0];
				Assert.assertTrue(request instanceof HttpEntityEnclosingRequest);
				HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
				HttpEntity entity = entityRequest.getEntity();
				String requestString = EntityUtils.toString(entity);
				Assert.assertTrue(requestString.contains("ToAddr=\""+MSISDN+"\""));
				Assert.assertTrue(requestString.contains("<Content Type=\"TEXT\">"+MESSAGE_CONTENT+"</Content>"));				
			}
			
			private void sendResponse(InvocationOnMock invocation, boolean successMessage, int outcome)
					throws UnsupportedEncodingException {
				Object[] args = invocation.getArguments();
				HttpResponse response = (HttpResponse) args[1];
				
				String responseXML = getFileContents(successMessage ? WASP_SUCCESS_XML : WASP_FAIL_XML);
				String waspResponse;
				if (successMessage) {
					waspResponse = MessageFormat.format(responseXML, sequenceNumber);
				} else {
					waspResponse = MessageFormat.format(responseXML, SIMULATED_ERROR_MESSAGE);
				}
				
				StringEntity entity = new StringEntity(waspResponse, "ISO-8859-1");
				entity.setContentType("text/xml");
				response.setEntity(entity);
				response.setStatusCode(OUTCOME_FAIL_HTTP == outcome ? HttpStatus.SC_BAD_GATEWAY : HttpStatus.SC_OK);
			}

		}).when(handler).handle(any(HttpRequest.class), any(HttpResponse.class), any(HttpContext.class));
		
		executeIntegratSmsMTRunnable(expectedTries, sequenceNumber, outcome);
	}
	
	public void executeIntegratSmsMTRunnable(final Integer desiredAttempts, final Integer expectedSequenceNumber, final int outcome) {
		final SmsMt smsMT = new SmsMt(MSISDN, MESSAGE_CONTENT, "smsorigin");
		DefaultHttpClient client = new DefaultHttpClient();
		SmsMt smsMTFromWasp = integrat.send(smsMT, client);
		
		Assert.assertEquals(desiredAttempts, smsMTFromWasp.getSendingAttempts());
		Assert.assertEquals(smsMT, smsMTFromWasp);
		String errorMessage = smsMTFromWasp.getErrorMessage();
		
		if (OUTCOME_SUCCESS == outcome){
			Assert.assertNull(errorMessage);
		} else if (OUTCOME_FAIL_HTTP == outcome) {
			Assert.assertTrue(errorMessage.contains(String.valueOf(HttpStatus.SC_BAD_GATEWAY)));
		} else if (OUTCOME_FAIL_NO_HOST == outcome){
			Assert.assertTrue(errorMessage.contains("UnknownHostException"));
		} else if (OUTCOME_FAIL_NULL == outcome){
			Assert.assertTrue(errorMessage.contains("NullPointerException"));
		}
		
		if (OUTCOME_SUCCESS == outcome && expectedSequenceNumber != null){
			Assert.assertEquals(expectedSequenceNumber.toString(), smsMT.getMessageTrackingNumber());
		}
	}
	
	private String getFileContents(String filename) {
		String fileContents = null;
		try {
			URL resource = this.getClass().getClassLoader().getResource(filename);
			fileContents = FileUtils.readFileToString(new File(resource.toURI()));
		} catch (Exception e) {
			Assert.fail("Failed to read file contents: " + filename);
		}
		return fileContents;
	}
}
