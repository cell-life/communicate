package org.celllife.mobilisr.service.wasp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Service;

/**
 * @see http://sms.telfree.com/developer_resources
 */
@Service("Telfree")
public class TelfreeHttpOutHandler extends HttpTransformer implements ChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(TelfreeHttpOutHandler.class);

	static final String MT_REGEXP_SEQ_NUM = "messageId=\"(\\d+)\"";
	private static final String MT_REGEXP_ERROR_API = "error=\"(.*)\"";
	private static final String MT_REGEXP_ERROR_SEND = "errorMessage=\"(.*)\"";
	private static final String MT_REGEXP_ERROR_CODE = "errorCode=\"(\\d+)\"";
	
	private static final String MT_POST_RSP_MID = "mid";
	private static final String MT_POST_RSP_STATUS = "stat";
	private static final String MT_POST_RSP_MSISDN = "destination";
	private static final String MT_POST_RSP_ERRCODE = "err";

	public static final String URL = "url";
	public static final String USERNAME = "password";
	public static final String PASSWORD = "username";

	private static String MT_SMS_TEMPLATE;

	public enum MT_POST_STATUS {
		DELIVRD("Message has been delivered"), EXPIRED(
				"Message validity period has expired"), DELETED(
				"Message has been deleted"), UNDELIV(
				"Message could not be delivered"), ACCEPTD(
				"Message is in accepted state"), UNKNOWN(
				"Message is in invalid state"), REJECTD(
				"Message has been rejected");

		public String label;

		private MT_POST_STATUS(String label) {
			this.label = label;
		}

		public static String getLabel(String name) {
			try {
				return valueOf(name).label;
			} catch (Exception e) {
				return null;
			}
		}
	}

	private HttpClient httpClient;

	private Pconfig config;

	private int retryDelay = 2000;

	private int maxRetries = 10;

	/**
	 * inputChannel and outputChannel must match existing channels in
	 * outgoingMessageContext.xml 
	 */
	@ServiceActivator(inputChannel = "out-telfreeHttp", outputChannel = "individualMessageResponse")
	public SmsMt sendMTSms(final SmsMt smsMt) throws ChannelProcessingException {
		String msg = null;
		try {
			msg = URLEncoder.encode(smsMt.getMessage(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new ChannelProcessingException(e);
		}
		final String getUrl = MessageFormat.format(TelfreeHttpOutHandler.MT_SMS_TEMPLATE,
				new Object[] { smsMt.getMsisdn(), msg });

		final boolean trace = log.isTraceEnabled();
		
		if (trace){
			log.trace("Sending message to '{}' with URL: '{}'", smsMt.getMsisdn(), getUrl);
		}

		RetryCommand command = new RetryCommand() {
			@Override
			protected boolean execute(int attempt) throws Exception {
				smsMt.setSendingAttempts(attempt);

				if (trace){
					log.trace("Sending attempt {} for message to '{}'", attempt, smsMt.getMsisdn());
				}
				HttpContext localContext = new BasicHttpContext();
				String responseString = sendToTelfree(localContext, getUrl);
				HttpResponse response = (HttpResponse) localContext
						.getAttribute(ExecutionContext.HTTP_RESPONSE);
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (trace){
					log.trace("Sending attempt {} for message to '{}': status code {}", new Object[]{attempt, smsMt.getMsisdn(), statusCode});
				}
				
				if (statusCode == HttpStatus.SC_OK && responseString != null
						&& !responseString.isEmpty()) {
					List<String> seq_num = MobilisrUtility.findValuesForRegExp(
							responseString, TelfreeHttpOutHandler.MT_REGEXP_SEQ_NUM);
					if (!seq_num.isEmpty()) {
						String sequence = StringUtils.join(seq_num, ',');
						smsMt.setMessageTrackingNumber(sequence);
						smsMt.setErrorMessage(null);
						
						if (trace){
							log.trace("Sending attempt {} for message to '{}': sequence number: '{}'", new Object[]{attempt, smsMt.getMsisdn(), sequence});
						}

						return true;
					} else {
						if (responseString.contains("<api")) {
							String message = MobilisrUtility
									.findValueForRegExp(
											responseString,
											TelfreeHttpOutHandler.MT_REGEXP_ERROR_API);
							smsMt.setErrorMessage(message);
						} else {
							String message = MobilisrUtility
									.findValueForRegExp(
											responseString,
											TelfreeHttpOutHandler.MT_REGEXP_ERROR_SEND);
							smsMt.setErrorMessage(message);

							String code = MobilisrUtility.findValueForRegExp(
									responseString,
									TelfreeHttpOutHandler.MT_REGEXP_ERROR_CODE);
							if (code != null && code.equals("2")) {
								smsMt.setInvalidNumber(true);
								return true;
							}
						}
						
						if (trace){
							log.trace("Sending attempt {} for message to '{}': failed with message: '{}'", new Object[]{attempt, smsMt.getMsisdn(), smsMt.getErrorMessage()});
						}
					}
				} else {
					String error = response.getStatusLine().toString();
					smsMt.setErrorMessage("HTTP Error: "
							+ error);
					
					if (trace){
						log.trace("Sending attempt {} for message to '{}': failed with HTTP error: '{}'", new Object[]{attempt, smsMt.getMsisdn(), error});
					}

				}

				return false;
			}
			
			@Override
			protected void retriesExceeded() {
				if (smsMt.getErrorMessage() == null || smsMt.getErrorMessage().isEmpty()){
					smsMt.setErrorMessage("Maximum number of retries exceeded");
				}
				log.info("Post to Telfree failed [errMsg={}]",
						smsMt.getErrorMessage());
			}

			@Override
			protected boolean handleError(Exception e, int attempt) {
				String message = e.getMessage();
				if (message == null || message.isEmpty()){
					message = e.toString();
				}
				smsMt.setErrorMessage("IO Exception: " + message);
				log.info("Post to Telfree failed [errMsg={}] [attempts={}]",
						smsMt.getErrorMessage(), attempt);
				return true;
			}

			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				smsMt.setErrorMessage("Interrupted Exception: "
						+ e.getMessage());
				log.info("Post to Telfree failed [errMsg={}] [attempts={}]",
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

	protected String sendToTelfree(HttpContext localContext, String url)
			throws IOException {
		HttpGet request = new HttpGet(url);

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

		if (httpClient == null){
			httpClient = HttpClientFactory.getClient();
		}
		
		String response = httpClient.execute(request, handler, localContext);
		return response;
	}
	
	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "delivery-telfree", outputChannel = "deliveryChannel")
	public DeliveryReceipt transformDeliveryReceipt(RawMessage rawMessage)
			throws ChannelProcessingException {
		Map<String, String[]> map = rawMessage.getParameterMap();

		String seqNum = getParameter(map, MT_POST_RSP_MID);
		String msisdn = getParameter(map, MT_POST_RSP_MSISDN);
		String waspPostStatus = getParameter(map, MT_POST_RSP_STATUS);
		String statusCode = getParameter(map, MT_POST_RSP_ERRCODE);
		
		DeliveryReceiptState status = DeliveryReceiptState.getByName(waspPostStatus);
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum, new Date(), status, statusCode);
		receipt.setSourceAddr(msisdn);
		
		return receipt;
	}

	@Override
	public void configure(Pconfig config) {
		String mt_username = getStringParameter(config, USERNAME);
		String mt_pwd = getStringParameter(config, PASSWORD);
		String mt_url_post = getStringParameter(config, URL);
		
		Validate.noNullElements(new Object[] { mt_username, mt_pwd, mt_url_post}, 
				"Channel config has null values.");

		StringBuilder mtTemplateBuilder = new StringBuilder();
		mtTemplateBuilder.append(mt_url_post);
		mtTemplateBuilder.append("&username=");
		mtTemplateBuilder.append(mt_username);
		mtTemplateBuilder.append("&password=");
		mtTemplateBuilder.append(mt_pwd);
		mtTemplateBuilder.append("&destination={0}");
		mtTemplateBuilder.append("&message={1}");
		MT_SMS_TEMPLATE = mtTemplateBuilder.toString();
	}

	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.OUT.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		if (config == null) {
			config = new Pconfig(null, "Telfree HTTP");
			config.addParameter(new StringParameter(URL,"URL:"));
			config.addParameter(new StringParameter(USERNAME,"Username:"));
			config.addParameter(new StringParameter(PASSWORD,"Password:"));
			config.setResource("out-telfreeHttp");
		}
		return config;
	}
	
	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
}