package org.celllife.mobilisr.service.wasp;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.MessageFormat;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TelfreeHttpOutMockTests {

	public class HttpHandler implements HttpRequestHandler {
		
		private int httpOutcome = OUTCOME_SUCCESS;
		private String errorMsg;
		private String errorCode;
		private boolean isApiError;
		
		public HttpHandler() {
		}
		
		public HttpHandler(String errorMsg, String errorCode, boolean isApiError) {
			this.errorMsg = errorMsg;
			this.errorCode = errorCode;
			this.isApiError = isApiError;
			Validate.notNull(errorMsg);
			if (!isApiError)
				Validate.notNull(errorCode);
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {
			
			boolean success = errorMsg == null;
			String waspResponse;
			if (success) {
				String responseXML = getFileContents(WASP_SUCCESS_XML);
				waspResponse = MessageFormat.format(responseXML, SEQUENCE_NUMBER);
			} else if (!isApiError){
				String responseXML = getFileContents(WASP_FAIL_XML);
				waspResponse = MessageFormat.format(responseXML, errorCode, errorMsg);
			} else {
				String responseXML = getFileContents(WASP_FAIL_API_XML);
				waspResponse = MessageFormat.format(responseXML, errorMsg);
			}
			
			StringEntity entity = new StringEntity(waspResponse, "ISO-8859-1");
			entity.setContentType("text/xml");
			response.setEntity(entity);
			response.setStatusCode(OUTCOME_FAIL_HTTP == httpOutcome ? HttpStatus.SC_BAD_GATEWAY : HttpStatus.SC_OK);
		}

	}

	private static final String MSISDN = "27785120109";
	private static final String MESSAGE_CONTENT = "Hello Bulk Testing message \n\rcontent \t ~!@#$%^&*()`/?>\n<,.\":;'}{|[]\'";
	private static final String SIMULATED_ERROR_MESSAGE = "simulated error message";
	private static final String WASP_SUCCESS_XML = "org/celllife/mobilisr/service/wasp/TelfreeWaspSuccess.xml";
	private static final String WASP_FAIL_XML = "org/celllife/mobilisr/service/wasp/TelfreeWaspFail.xml";
	private static final String WASP_FAIL_API_XML = "org/celllife/mobilisr/service/wasp/TelfreeWaspFailApi.xml";
	private static final int SEQUENCE_NUMBER = 123456;
	protected static final int OUTCOME_SUCCESS = 0;
	protected static final int OUTCOME_FAIL_HTTP = 1;
	protected static final int OUTCOME_FAIL_NO_HOST = 2;
	protected static final int OUTCOME_FAIL_NULL = 3;

	private LocalTestServer localServer;

	private TelfreeHttpOutHandler telfree;

	@Before
	public void setUp() throws Exception {
		localServer = new LocalTestServer(null, null);
		localServer.start();
		
		telfree = new TelfreeHttpOutHandler();
		Pconfig config = new Pconfig();
		StringParameter param = new StringParameter(TelfreeHttpOutHandler.URL, "");
		param.setValue("http://" + getServerHttp().toHostString() + "/xml?command=send");
		config.addParameter(param);
		
		param = new StringParameter(TelfreeHttpOutHandler.USERNAME, "");
		param.setValue("test");
		config.addParameter(param);
		
		param = new StringParameter(TelfreeHttpOutHandler.PASSWORD, "");
		param.setValue("test");
		config.addParameter(param);
		telfree.configure(config);
		telfree.setRetryDelay(0);
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
	public void testTelfreeHttpOutHandler() throws Exception {
		localServer.register("/*", new HttpHandler());
		SmsMt sendMTSms = telfree.sendMTSms(new SmsMt(MSISDN, MESSAGE_CONTENT, ""));
		Assert.assertEquals(SmsStatus.WASP_SUCCESS, sendMTSms.getStatus());
		Assert.assertEquals(String.valueOf(SEQUENCE_NUMBER), sendMTSms.getMessageTrackingNumber());
		Assert.assertEquals(1, sendMTSms.getSendingAttempts().intValue());
	}
	
	@Test
	public void testTelfreeHttpOutHandler_failure() throws Exception {
		localServer.register("/*", new HttpHandler(SIMULATED_ERROR_MESSAGE, "1", false));
		telfree.setMaxRetries(5);
		SmsMt sendMTSms = telfree.sendMTSms(new SmsMt(MSISDN, MESSAGE_CONTENT, ""));
		Assert.assertEquals(SmsStatus.WASP_FAIL, sendMTSms.getStatus());
		Assert.assertEquals(SIMULATED_ERROR_MESSAGE, sendMTSms.getErrorMessage());
		Assert.assertEquals(false, sendMTSms.isInvalidNumber());
		Assert.assertEquals(5, sendMTSms.getSendingAttempts().intValue());
	}

	@Test
	public void testTelfreeHttpOutHandler_failureWithInvalidNumber() throws Exception {
		localServer.register("/*", new HttpHandler(SIMULATED_ERROR_MESSAGE, "2", false));
		SmsMt sendMTSms = telfree.sendMTSms(new SmsMt(MSISDN, MESSAGE_CONTENT, ""));
		Assert.assertEquals(SmsStatus.WASP_FAIL, sendMTSms.getStatus());
		Assert.assertEquals(SIMULATED_ERROR_MESSAGE, sendMTSms.getErrorMessage());
		Assert.assertEquals(true, sendMTSms.isInvalidNumber());
		Assert.assertEquals(1, sendMTSms.getSendingAttempts().intValue());
	}
	
	@Test
	public void testTelfreeHttpOutHandler_Apifailure() throws Exception {
		localServer.register("/*", new HttpHandler(SIMULATED_ERROR_MESSAGE, null, true));
		telfree.setMaxRetries(5);
		SmsMt sendMTSms = telfree.sendMTSms(new SmsMt(MSISDN, MESSAGE_CONTENT, ""));
		Assert.assertEquals(SmsStatus.WASP_FAIL, sendMTSms.getStatus());
		Assert.assertEquals(SIMULATED_ERROR_MESSAGE, sendMTSms.getErrorMessage());
		Assert.assertEquals(false, sendMTSms.isInvalidNumber());
		Assert.assertEquals(5, sendMTSms.getSendingAttempts().intValue());
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
