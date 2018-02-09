package org.celllife.mobilisr.service.wasp;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extjs.gxt.ui.client.util.Format;

public class IntegratSendCommand {
	
	private static final Logger log = LoggerFactory.getLogger(IntegratSendCommand.class);
	private static final String MT_REGEXP_SEQ_NUM = "<field name=\"seq_no\" value=\"(\\d+)\"/>";
//    private static final String MT_REGEXP_STATUS_CODE = "status_code=\"(\\d+)\"";
    private static final String MT_REGEXP_ERROR_SEND = "<field name=\"reason\" value=\"(.*)\"/>";
//    private static final String MT_REGEXP_STATUS_VALUE = "<field name=\"status\" value=\"(\\d+)\"/>";
	
	private String postUrl;
	private String smsTemplate;
	
	private int maxRetries = 10;
	private int retryDelay = 2000;

	public IntegratSendCommand(String postUrl, String smsTemplate) {
		this.postUrl = postUrl;
		this.smsTemplate = smsTemplate;
	}

	public SmsMt send(final SmsMt smsMt, final HttpClient client) {
		final String smsMsg = Format.substitute(smsTemplate,
				new Object[] { smsMt.getMsisdn(), smsMt.getMessage() });

		RetryCommand command = new RetryCommand() {
			@Override
			protected boolean execute(int attempt) throws Exception {
				smsMt.setSendingAttempts(attempt);
				
				HttpContext localContext = new BasicHttpContext();
				String responseString = postToIntegrat(localContext, smsMsg, client);

				HttpResponse response = (HttpResponse) localContext.getAttribute(ExecutionContext.HTTP_RESPONSE);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK && responseString != null
							&& !responseString.isEmpty()) {
					String seq_num = MobilisrUtility.findValueForRegExp(responseString, MT_REGEXP_SEQ_NUM);
					if (seq_num != null) {
						smsMt.setMessageTrackingNumber(seq_num);
						smsMt.setErrorMessage(null);
						return true;
					} else {
						String errorMessage = MobilisrUtility
								.findValueForRegExp(responseString,
										MT_REGEXP_ERROR_SEND);
						smsMt.setErrorMessage(errorMessage);
						
						/*String statusValue = MobilisrUtility
						.findValueForRegExp(responseString,
								MT_REGEXP_STATUS_VALUE);*/
						
					}
				} else {
					smsMt.setErrorMessage("HTTP Error: "
							+ response.getStatusLine().toString());
				}
				return false;
			}
			
			@Override
			protected void retriesExceeded() {
				if (smsMt.getErrorMessage() == null || smsMt.getErrorMessage().isEmpty()){
					smsMt.setErrorMessage("Maximum number of retries exceeded");
				}
				log.info("Post to Integrat failed [errMsg={}]",
						smsMt.getErrorMessage());
			}

			@Override
			protected boolean handleError(Exception e, int attempt) {
				smsMt.setErrorMessage("Exception: " + e.getClass().getName() + " : " + e.getMessage());
				log.info("Post to Integrat failed [errMsg={}] [attempts={}]",
						smsMt.getErrorMessage(), attempt);
				return true;
			}

			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				smsMt.setErrorMessage("Interrupted Exception: "
						+ e.getMessage());
				log.info("Post to Integrat failed [errMsg={}] [attempts={}]",
						smsMt.getErrorMessage(), attempt);
				return true;
			}
		};

		command.setMaxRetries(maxRetries);
		command.setRetryDelay(retryDelay);
		command.run();

		SmsStatus status = (smsMt.getErrorMessage() == null ? SmsStatus.WASP_SUCCESS
				: SmsStatus.WASP_FAIL);
		smsMt.setStatus(status);
		
		return smsMt;
	}

	private String postToIntegrat(HttpContext localContext, String message, HttpClient httpClient)
			throws IOException {

		HttpPost post = new HttpPost(postUrl);
		StringEntity entity = new StringEntity(message, "ISO-8859-1");
		entity.setContentType("text/xml");
		post.setEntity(entity);

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					return null;
				}
			}
		};

		String response = httpClient.execute(post, handler, localContext);
		return response;
	}
	
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

}
