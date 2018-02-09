package org.celllife.mobilisr.service.wasp;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.http.client.HttpClient;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Service;

@Service("Integrat")
public class IntegratHttpOutHandler extends BaseChannelHandler implements ChannelHandler {
	
	public enum IntegratStatus {
		
		RC_QUEUED(DeliveryReceiptState.ACCEPTD), 
		RC_SUBMITTED(DeliveryReceiptState.ACCEPTD),
		RC_ACKNOWLEDGED(DeliveryReceiptState.ACCEPTD), 
		RC_RECEIPTED(DeliveryReceiptState.DELIVRD), 
		RC_EXPIRED(DeliveryReceiptState.EXPIRED), 
		RC_FAILED(DeliveryReceiptState.UNDELIV),
		RC_AUTH_DENIED(DeliveryReceiptState.REJECTD),
		RC_PENDING(DeliveryReceiptState.UNKNOWN),
		RC_CANCELLED(DeliveryReceiptState.DELETED);
		
		public DeliveryReceiptState deliveryState;
		
		private IntegratStatus(DeliveryReceiptState state) {
			deliveryState = state;
		}
		
		public static DeliveryReceiptState convertStatus(String waspPostStatus) {
			try {
				IntegratStatus integratStatus = IntegratStatus.valueOf(waspPostStatus);
				return integratStatus.deliveryState;
			} catch (Exception e) {
				return DeliveryReceiptState.UNKNOWN;
			}
		}
	}

	public static final String MT_WASPRSP_REGEXP_SEQ_NUM = "SeqNo=\"(\\d+)\"";
	public static final String MT_WASPRSP_REGEXP_STATUSCODE = "Code=\"(\\d+)\"";
	public static final String MT_WASPRSP_REGEXP_TEXT = "Text=\"(.*)\"";
	public static final String MT_WASPRSP_REGEXP_REFNUM = "RefNo=\"(\\d+)\"";
	public static final String MT_WASPRSP_REGEXP_NETWORK = "<NetworkID>(.*)</NetworkID>";
	
	public static final String URL = "url";
	public static final String USERNAME = "password";
	public static final String PASSWORD = "username";
	public static final String SERVICE_CODE = "serviceCode";
	public static final String TAG = "tag";
	
	private String mt_sms_template;	
	private String mt_url_post;
	
	private HttpClient httpClient;
	private Pconfig config;
	
	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@ServiceActivator(inputChannel = "out-integratHttp", outputChannel = "individualMessageResponse")
	public SmsMt sendMTSms(final SmsMt smsMt) {
		if (httpClient == null){
			httpClient = HttpClientFactory.getClient();
		}
		IntegratSendCommand command = new IntegratSendCommand(mt_url_post, mt_sms_template);
		return command.send(smsMt, httpClient);
	}
	
	/**
	 * inputChannel and outputChannel must match existing channels in
	 * incomingMessageContext.xml 
	 */
	@Transformer(inputChannel = "delivery-integrat", outputChannel = "deliveryChannel")
	public DeliveryReceipt transformDeliveryReceipt(RawMessage rawMessage)
			throws ChannelProcessingException {
		String rspFromWASP = rawMessage.getBody();

		String seqNum = MobilisrUtility.findValueForRegExp(rspFromWASP, MT_WASPRSP_REGEXP_SEQ_NUM);
		String msisdn = MobilisrUtility.findValueForRegExp(rspFromWASP, MT_WASPRSP_REGEXP_REFNUM);
		String waspPostStatus = MobilisrUtility.findValueForRegExp(rspFromWASP, MT_WASPRSP_REGEXP_TEXT);
		String statusCode = MobilisrUtility.findValueForRegExp(rspFromWASP, MT_WASPRSP_REGEXP_STATUSCODE);
		/*String networkID = MobilisrUtility.findValueForRegExp(rspFromWASP, MT_WASPRSP_REGEXP_NETWORK);
		int networkIDVal = Integer.parseInt(networkID);
		int statusCodeVal = Integer.parseInt(statusCode);
		
		SmsStatus smsStatus = (statusCodeVal <= MT_POST_STATUS.RC_RECEIPTED.ordinal() ? SmsStatus.TX_SUCCESS : SmsStatus.TX_FAIL);
		String mobileNetwork = MobilisrUtility.obtainMobileNetwork(networkIDVal);*/
		
		DeliveryReceiptState status = IntegratStatus.convertStatus(waspPostStatus);
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum, new Date(), status, statusCode);
		receipt.setSourceAddr(msisdn);
		
		// TODO: send success response to Integrat
		
		return receipt;
	}

	@Override
	public void configure(Pconfig config) {
		String mt_username = getStringParameter(config, USERNAME);
		String mt_pwd = getStringParameter(config, PASSWORD);
		mt_url_post = getStringParameter(config, URL);
		String mt_service_code = getStringParameter(config, SERVICE_CODE);
		String mt_tag = getStringParameter(config, TAG);
		
		Validate.noNullElements(new Object[] { mt_username, mt_pwd,
				mt_url_post, mt_service_code, mt_tag }, "Channel config has null values.");
		
		mt_sms_template = buildTemplate(mt_username, mt_pwd, mt_service_code, mt_tag);
	}

	/**
	 * @param mt_username
	 * @param mt_pwd
	 * @param mt_service_code
	 * @param mt_tag
	 * @return 
	 */
	String buildTemplate(String mt_username, String mt_pwd,
			String mt_service_code, String mt_tag) {
		StringBuilder mtTemplateBuilder = new StringBuilder();
		mtTemplateBuilder.append("<Message><Request Type=\"SendSMS\" RefNo=\"{0}\">");
		mtTemplateBuilder.append("<UserID Orientation=\"TR\">" + mt_username + "</UserID>");
		mtTemplateBuilder.append("<Password>" + mt_pwd + "</Password>");
		mtTemplateBuilder.append("<SendSMS ToAddr=\"{0}\" Validity=\"0\" Flags=\"0\">");
		mtTemplateBuilder.append("<Ticket Service=\"" + mt_service_code + "\" ChargeAddr=\"{0}\" Value=\"0\"/>");
		mtTemplateBuilder.append("<Reply Flags=\"0\" Tag=\"" + mt_tag + "\" Display=\"" + mt_service_code + "\"/>");
		mtTemplateBuilder.append("<Content Type=\"TEXT\">{1}</Content>");
		mtTemplateBuilder.append("</SendSMS></Request></Message>");
		return mtTemplateBuilder.toString();
	}
	
	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.OUT.equals(type);
	}
	
	@Override
	public Pconfig getConfigDescriptor() {
		if (config == null) {
			config = new Pconfig(null, "Integrat HTTP");
			config.addParameter(new StringParameter(URL,"URL:"));
			config.addParameter(new StringParameter(USERNAME,"Username:"));
			config.addParameter(new StringParameter(PASSWORD,"Password:"));
			config.addParameter(new StringParameter(SERVICE_CODE,"Service code:"));
			config.addParameter(new StringParameter(TAG,"Tag:"));
			
			/*
			 * resource name must match channel in outgoingMessageContext.xml
			 */
			config.setResource("out-integratHttp");
		}
		return config;
	}
}
