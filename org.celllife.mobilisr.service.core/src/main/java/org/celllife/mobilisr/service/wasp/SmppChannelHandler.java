package org.celllife.mobilisr.service.wasp;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.MessageReceiverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SmppChannelHandler extends BaseChannelHandler{
	
	private static final Logger log = LoggerFactory.getLogger(SmppChannelHandler.class);

	private static final int retryAttempts = 3;

	public static final String SERVICE_TYPE = "service_type";
    public static final String ADDRESS_RANGE = "address_range";
	public static final String SOURCE_ADDRESS = "source_address";
	public static final String SYSTEM_TYPE = "system_type";
	public static final String PASSWORD = "password";
	public static final String USERNAME = "username";
	public static final String PORT = "port";
	public static final String HOST = "host";
	
	private Pconfig config;
	private final String handlerName;
	private final String handlerResource;

	private String user;
	private String password;
	private String host;
	private Integer port;
	private String serviceType;
	private String sourceAddress;
    private String addressRange;
	private String systemType;
	
	@Autowired
	private MessageReceiverListener receiverListener;

	private Gateway gateway;
	
	private AtomicInteger unacked = new AtomicInteger();

	private int maxUnacked = 30;

	/**
	 * @param handlerName
	 * @param handlerResource must match channel in outgoingChannelContext.xml
	 */
	public SmppChannelHandler(String handlerName, String handlerResource) {
		this.handlerName = handlerName;
		this.handlerResource = handlerResource;
	}

	public SmsMt sendMTSms(final SmsMt smsMt) throws ChannelProcessingException {

		if (gateway == null || gateway.isClosed()) {
			throw new ChannelProcessingException("SMPP gateway not started for " + handlerName);
		}

		RetryCommand command = new RetryCommand() {
			
			@Override
			protected boolean execute(int attempt) throws Exception {
				
				while (unacked.get() >= maxUnacked){
					try { Thread.sleep(10); } catch (InterruptedException e1) {};
				}
				
				smsMt.setSendingAttempts(attempt);

				unacked.incrementAndGet();
				String messageId = gateway.submitShortMessage(
						smsMt.getMsisdn(), smsMt.getMessage().getBytes(),
						sourceAddress, serviceType);
				unacked.decrementAndGet();
				
				smsMt.setMessageTrackingNumber(messageId == null ? null
						: messageId.toUpperCase());
				smsMt.setStatus(SmsStatus.WASP_SUCCESS);
				smsMt.setErrorMessage(null);
				return true;
			}

			@Override
			protected void retriesExceeded() {
				if (smsMt.getErrorMessage() == null
						|| smsMt.getErrorMessage().isEmpty()) {
					smsMt.setErrorMessage("Maximum number of retries exceeded");
				}
				log.info("Post to {} failed [errMsg={}]",
						handlerName,smsMt.getErrorMessage());
			}

			@Override
			protected boolean handleError(Exception e, int attempt) {
				unacked.decrementAndGet();
				Throwable cause = e.getCause();
				if (cause != null && cause instanceof NegativeResponseException){
					int status = ((NegativeResponseException)cause).getCommandStatus();
					SMPPErrorCodes code = SMPPErrorCodes.getFromCode(status);
					smsMt.setErrorMessage(code.getDescription());
					
					if (code == SMPPErrorCodes.ESME_RINVDSTADR){
						smsMt.setInvalidNumber(true);
						return false;
					}
				} else {
					if (cause != null){
						smsMt.setErrorMessage(cause.getClass().getSimpleName()
								+ ": " + e.getMessage());
					} else {
						smsMt.setErrorMessage(e.getClass().getSimpleName()
								+ ": " + e.getMessage());
					}
				}
				
				log.info("Post to {} failed [errMsg={}] [attempts={}]",
						new Object[]{handlerName, smsMt.getErrorMessage(), attempt});
				
				return true;
			}

			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				unacked.decrementAndGet();
				smsMt.setErrorMessage("Interrupted Exception: "
						+ e.getMessage());
				log.info("Post to {} failed [errMsg={}] [attempts={}]",
						new Object[]{handlerName, smsMt.getErrorMessage(), attempt});
				return true;
			}
		};

		command.setMaxRetries(retryAttempts);
		command.run();

		SmsStatus status = (smsMt.getErrorMessage() == null ? SmsStatus.WASP_SUCCESS
				: SmsStatus.WASP_FAIL);
		smsMt.setStatus(status);

		return smsMt;
	}

	@Override
	public void configure(Pconfig config) {
		user = getStringParameter(config, USERNAME);
		password = getStringParameter(config, PASSWORD);
		host = getStringParameter(config, HOST);
		port = getIntegerParameter(config, PORT);
		systemType = getStringParameter(config, SYSTEM_TYPE);
		serviceType = getStringParameter(config, SERVICE_TYPE);
		sourceAddress = getStringParameter(config, SOURCE_ADDRESS);
        addressRange = getStringParameter(config, ADDRESS_RANGE);
		
		Validate.noNullElements(new Object[] { user, password, host, port,
				systemType, serviceType }, "Channel config has null values.");
	}

	@Override
	public boolean supportsChannelType(ChannelType type) {
		return ChannelType.IN.equals(type) || ChannelType.OUT.equals(type);
	}

	@Override
	public void start() {
		log.info("Starting {} server", handlerName);
		gateway = new SmppGateway(host, port, new BindParameter(
				BindType.BIND_TRX, user, password, systemType,
				TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, addressRange),
				receiverListener);
	}

	@Override
	public void stop() {
		if (gateway != null) {
			gateway.shutdown();
			gateway = null;
		}
	}

	@Override
	public Pconfig getConfigDescriptor() {
		if (config == null) {
			config = new Pconfig(null, handlerName);
			config.addParameter(new StringParameter(HOST,"Host:"));
			config.addParameter(new IntegerParameter(PORT,"Port:"));
			config.addParameter(new StringParameter(USERNAME,"Username:"));
			config.addParameter(new StringParameter(PASSWORD,"Password:"));
			StringParameter source = new StringParameter(SOURCE_ADDRESS,"Source Address:");
			source.setTooltip("The address to send the message from");
			config.addParameter(source);
			config.addParameter(new StringParameter(SYSTEM_TYPE,"System Type:"));
			StringParameter type = new StringParameter(SERVICE_TYPE,"Service Type:");
			type.setRegex("^.{0,5}$");
			config.addParameter(type);

			/*
			 * resource name must match channel in outgoingMessageContext.xml
			 */
			config.setResource(handlerResource);
		}
		
		return config;
	}
	
	public void setMaxUnacked(int maxUnacked){
		this.maxUnacked = maxUnacked;
	}
	
	void setGateway(Gateway gateway){
		this.gateway = gateway;
	}
	
	int getUnacked() {
		return unacked.get();
	}
}
