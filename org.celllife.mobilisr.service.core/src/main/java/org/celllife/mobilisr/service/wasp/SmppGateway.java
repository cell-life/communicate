package org.celllife.mobilisr.service.wasp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.util.LogUtil;
import org.jsmpp.bean.*;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This are implementation of {@link Gateway}. This gateway will reconnect for a
 * specified interval if the session are closed.
 * 
 * @author uudashr
 * 
 */
public class SmppGateway implements Gateway {
    private static final int ENQUIRE_LINK_TIMER = 50000;
	private static final Logger logger = LoggerFactory.getLogger(SmppGateway.class);
	private static final int SHORT_MESSAGE_MAX = 160;
	private static final int MULTI_MESSAGE_MAX = 146;
    private SMPPSession session = null;
    private String remoteIpAddress;
    private int remotePort;
    private BindParameter bindParam;
	private MessageReceiverListener receiverListener;
    private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
    private AtomicInteger idgen = new AtomicInteger();
	private boolean isClosed = false;
	private long reconnectInterval = 50L;

    /**
     * Construct auto reconnect gateway with specified ip address, port and SMPP
     * Bind parameters.
     * 
     * @param remoteIpAddress is the SMSC IP address.
     * @param remotePort is the SMSC port.
     * @param bindParam is the SMPP Bind parameters.
     * @throws IOException
     */
    public SmppGateway(String remoteIpAddress, int remotePort,
            BindParameter bindParam, 
            MessageReceiverListener receiverListener) {
        this.remoteIpAddress = remoteIpAddress;
        this.remotePort = remotePort;
        this.bindParam = bindParam;
		this.receiverListener = receiverListener;
        try {
			session = newSession();
		} catch (Exception e) {
			logger.error(LogUtil.getMarker_notifyAdmin(),
					"Unable to connect to SMPP server: [remoteHost={}] [remotePort={}]",
					new Object[]{remoteIpAddress, remotePort});
			reconnectAfter(10000);
		}
    }
    
	public String submitShortMessage(String destinationAddr,
			byte[] shortMessage, String sourceAddress, String serviceType)
			throws ChannelProcessingException {
        
		if (shortMessage.length > SHORT_MESSAGE_MAX){
			final int totalSegments = (int) Math.ceil(new Double(shortMessage.length)
					/ MULTI_MESSAGE_MAX);
			
			OptionalParameter sarMsgRefNum = OptionalParameters
					.newSarMsgRefNum((short) idgen.incrementAndGet());
			OptionalParameter sarTotalSegments = OptionalParameters
					.newSarTotalSegments(totalSegments);

			String[] ids = new String[totalSegments];
			for (int i = 0; i < totalSegments; i++) {
				final int seqNum = i + 1;
				
				int from = i * MULTI_MESSAGE_MAX;
				int to = (i + 1) * MULTI_MESSAGE_MAX;
				to = to > shortMessage.length ? shortMessage.length : to;
				byte[] messagePart = Arrays.copyOfRange(shortMessage, from, to);
				OptionalParameter sarSegmentSeqnum = OptionalParameters
						.newSarSegmentSeqnum(seqNum);
				
				String messageId = submitMessage(destinationAddr, messagePart,
						sourceAddress, serviceType, sarMsgRefNum, sarTotalSegments,
						sarSegmentSeqnum);
				ids[i] = messageId.toUpperCase();
			}
			
			return StringUtils.join(ids, ",");
		}else {
			String messageId = submitMessage(destinationAddr, shortMessage, sourceAddress, serviceType);
			return messageId.toUpperCase();
		}
    }

	private String submitMessage(String destinationAddr, byte[] shortMessage,
			String sourceAddress, String serviceType,
			OptionalParameter... optionalParams)
			throws ChannelProcessingException {
		try {
			String messageId = getSession().submitShortMessage(serviceType,
					TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN,
					sourceAddress, TypeOfNumber.UNKNOWN,
					NumberingPlanIndicator.UNKNOWN, destinationAddr, new ESMClass(),
					(byte) 0, (byte) 1, timeFormatter.format(new Date()), null,
					new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), (byte) 0,
                    new DataCoding1111(0), (byte) 0,
							shortMessage, optionalParams);
			return messageId;
		} catch (Exception e) {
			throw new ChannelProcessingException(e);
		}
	}

    /**
     * Create new {@link SMPPSession} complete with the
     * {@link SessionStateListenerImpl}.
     * 
     * @return the {@link SMPPSession}.
     * @throws IOException if the creation of new session failed.
     */
    private SMPPSession newSession() throws IOException {
        SMPPSession tmpSession = new SMPPSession();
        tmpSession.connectAndBind(remoteIpAddress, remotePort, bindParam, 5000);
        tmpSession.addSessionStateListener(new SessionStateListenerImpl());
        tmpSession.setEnquireLinkTimer(ENQUIRE_LINK_TIMER);
        if (receiverListener != null){
        	tmpSession.setMessageReceiverListener(receiverListener);
        }
        return tmpSession;
    }

    /**
     * Get the session. If the session still null or not in bound state, then IO
     * exception will be thrown.
     * 
     * @return the valid session.
     * @throws IOException if there is no valid session or session creation is
     *         invalid.
     */
    private SMPPSession getSession() throws IOException {
        if (session == null) {
            logger.info("SMPP gateway initiate session for the first time to " + remoteIpAddress + ":" + remotePort);
            session = newSession();
        } else if (!session.getSessionState().isBound()) {
            session = newSession();
        }
        return session;
    }
    
    private class SessionStateListenerImpl implements SessionStateListener {
        public void onStateChange(SessionState newState, SessionState oldState,
                Object source) {
			logger.info("SMPP session state changed from {} to {}", oldState,
					newState);
			 if (newState.equals(SessionState.CLOSED) && !isClosed) {
	                logger.info("SMPP session closed");
	                reconnectAfter(reconnectInterval);
	         }
        }
    }
    
    /**
     * Reconnect session after specified interval.
     * 
     * @param timeInMillis is the interval.
     */
    private void reconnectAfter(final long timeInMillis) {
        new Thread() {
            @Override
            public void run() {
                logger.info("SMPP gateway schedule reconnect after " + timeInMillis + " millis");
                try {
                    Thread.sleep(timeInMillis);
                } catch (InterruptedException e) {
                }

                int attempt = 0;
                while (session == null || session.getSessionState().equals(SessionState.CLOSED)) {
                	if (isClosed){
                		break;
                	}
                    try {
                        logger.info("SMPP gateway reconnecting attempt #" + (++attempt) + "...");
                        session = newSession();
                        logger.info("SMPP session with id " + session.getSessionId() + " established.");
                    } catch (IOException e) {
                        if (attempt < 12) { // Don't send an email and log an error just yet.
                            logger.warn("SMPP gateway failed opening connection and bind to " + remoteIpAddress + ":" + remotePort);
                        } else { // after 12 attempts we can send an email and log an error
                            logger.error(LogUtil.getMarker_notifyAdmin(), "SMPP gateway failed opening connection and bind to " + remoteIpAddress + ":" + remotePort, e);
                        }
                        try {
                            Thread.sleep(reconnectInterval);
                        } catch (InterruptedException ee) {
                            logger.warn("Thread Sleep Interrupted", e);
                        }
                    }
                }
            }
        }.start();
    }

    @Override
	public void shutdown() {
    	isClosed = true;
    	if (session != null) {
    		session.unbindAndClose();
    	}
	}
    
    public boolean isClosed() {
		return isClosed;
	}
}
