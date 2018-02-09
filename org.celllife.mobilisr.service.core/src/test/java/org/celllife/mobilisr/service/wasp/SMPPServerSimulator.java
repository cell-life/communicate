/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.celllife.mobilisr.service.wasp;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.ServerResponseDeliveryAdapter;
import org.jsmpp.session.Session;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author uudashr
 *
 */
public class SMPPServerSimulator extends ServerResponseDeliveryAdapter implements Runnable, ServerMessageReceiverListener {
    private static final Integer DEFAULT_PORT = 8065;
    private static final Logger logger = LoggerFactory.getLogger(SMPPServerSimulator.class);
    private ExecutorService execService;
    private ExecutorService execServiceDelReciept;
    private final MessageIDGenerator messageIDGenerator = new RandomMessageIDGenerator();
    private final AtomicInteger requestCounter = new AtomicInteger();
    private final AtomicInteger responseCounter = new AtomicInteger();
    private final AtomicInteger totalResponseCounter = new AtomicInteger();
    private final AtomicInteger totalRequestCounter = new AtomicInteger();
    private final AtomicInteger sendCounter = new AtomicInteger();
    private final AtomicInteger errorCounter = new AtomicInteger();
    private final AtomicInteger totalSentCounter = new AtomicInteger();
    private int port;
	private boolean exit = true;
	public int responseDelay = 0;
	private int deliveryDelay = 0;
	protected SMPPServerSession serverSession;
	private final boolean withTrafficWatcher;
	private Thread listenerThread;
    
    public SMPPServerSimulator(int port, boolean withTrafficWatcher) {
        this.port = port;
		this.withTrafficWatcher = withTrafficWatcher;
    }
    
    public int getTotalRequestCount() {
		return totalRequestCounter.get();
	}
    
    public int getTotalResponseCount() {
    	return totalResponseCounter.get();
    }
    
    public void run() {
        exit = false;
        execService = Executors.newFixedThreadPool(5);
        execServiceDelReciept = Executors.newFixedThreadPool(100);
        
    	listenerThread = new Thread(new Runnable(){
			@Override
			public void run() {
				SMPPServerSessionListener sessionListener = null;
				try {
					sessionListener = new SMPPServerSessionListener(port);
					sessionListener.setTimeout(1000);
		            logger.info("Listening on port {}", port);
		            while (!exit) {
		                serverSession = sessionListener.accept();
		                logger.info("Accepting connection for session {}", serverSession.getSessionId());
		                serverSession.setMessageReceiverListener(SMPPServerSimulator.this);
		                serverSession.setResponseDeliveryListener(SMPPServerSimulator.this);
		                execService.execute(new WaitBindTask(serverSession));
		            }
				} catch (SocketTimeoutException e){
					logger.debug("Accept timed out");
				} catch (Exception e) {
		            logger.error("Error occured", e);
		        } finally {
		        	if (sessionListener != null)
						try {
							sessionListener.close();
						} catch (IOException e) {}
		        }
			}
    	});
    	
    	listenerThread.start();
    	
    	if (withTrafficWatcher)
    		new TrafficWatcherThread().start();
    }
    
    public void sendMessage(String message,String sourceAddress, String desinationAddress){
		execServiceDelReciept.execute(new DeliverMessageTask(serverSession,
				message, sourceAddress, desinationAddress));
    }
    
    public void shutdown(){
    	logger.info("Shutdown initiated");
    	this.exit = true;
    	if (serverSession != null)
    		serverSession.close();
    	execService.shutdownNow();
    	execServiceDelReciept.shutdownNow();
    	listenerThread.interrupt();
    }
    
    public QuerySmResult onAcceptQuerySm(QuerySm querySm,
            SMPPServerSession source) throws ProcessRequestException {
        logger.info("Accepting query sm, but not implemented");
        return null;
    }
    
    public MessageId onAcceptSubmitSm(SubmitSm submitSm,
            SMPPServerSession source) throws ProcessRequestException {
        MessageId messageId = messageIDGenerator.newMessageId();
       
        try {
			Thread.sleep(responseDelay );
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
        logger.debug("Receiving submit_sm {}, and return message id {}", new String(submitSm.getShortMessage()), messageId.getValue());
        if (SMSCDeliveryReceipt.SUCCESS.containedIn(submitSm.getRegisteredDelivery()) || SMSCDeliveryReceipt.SUCCESS_FAILURE.containedIn(submitSm.getRegisteredDelivery())) {
            execServiceDelReciept.execute(new DeliveryReceiptTask(source, submitSm, messageId));
        }
        
        requestCounter.incrementAndGet();
        return messageId;
    }
    
    public void onSubmitSmRespSent(MessageId messageId,
            SMPPServerSession source) {
        logger.debug("submit_sm_resp with message_id {} has been sent", messageId);
    }
    
    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti,
            SMPPServerSession source) throws ProcessRequestException {
        return null;
    }
    
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
            throws ProcessRequestException {
        return null;
    }
    
    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession source)
            throws ProcessRequestException {
    }
    
    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession source)
            throws ProcessRequestException {
    }
    
    private class WaitBindTask implements Runnable {
        private final SMPPServerSession serverSession;
        
        public WaitBindTask(SMPPServerSession serverSession) {
            this.serverSession = serverSession;
        }

        public void run() {
            try {
                BindRequest bindRequest = serverSession.waitForBind(1000);
                logger.info("Accepting bind for session {}", serverSession.getSessionId());
                try {
                    bindRequest.accept("sys");
                } catch (PDUStringException e) {
                    logger.error("Invalid system id", e);
                    bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
                }
            
            } catch (IllegalStateException e) {
                logger.error("System error", e);
            } catch (TimeoutException e) {
                logger.warn("Wait for bind has reach timeout", e);
            } catch (IOException e) {
                logger.error("Failed accepting bind request for session {}", serverSession.getSessionId());
            }
        }
    }
    
    private class DeliveryReceiptTask implements Runnable {
        private final SMPPServerSession session;
        private final SubmitSm submitSm;
        private MessageId messageId;
        public DeliveryReceiptTask(SMPPServerSession session,
                SubmitSm submitSm, MessageId messageId) {
            this.session = session;
            this.submitSm = submitSm;
            this.messageId = messageId;
        }

        public void run() {
            try {
                Thread.sleep(deliveryDelay);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            String stringValue = messageId.getValue();
            try {
                DeliveryReceipt delRec = new DeliveryReceipt(stringValue, 1, 1, new Date(), new Date(), DeliveryReceiptState.DELIVRD,  null, new String(submitSm.getShortMessage()));
                session.deliverShortMessage(
                        "mc", 
                        TypeOfNumber.valueOf(submitSm.getDestAddrTon()), 
                        NumberingPlanIndicator.valueOf(submitSm.getDestAddrNpi()), 
                        submitSm.getDestAddress(), 
                        TypeOfNumber.valueOf(submitSm.getSourceAddrTon()), 
                        NumberingPlanIndicator.valueOf(submitSm.getSourceAddrNpi()), 
                        submitSm.getSourceAddr(), 
                        new ESMClass(MessageMode.DEFAULT, MessageType.SMSC_DEL_RECEIPT, GSMSpecificFeature.DEFAULT), 
                        (byte)0, 
                        (byte)0, 
                        new RegisteredDelivery(0), 
                        new DataCoding1111(0),
                        delRec.toString().getBytes());
                responseCounter.incrementAndGet();
                logger.debug("Sending delivery reciept for message id " + messageId + ":" + stringValue);
            } catch (Exception e) {
                logger.error("Failed sending delivery_receipt for message id " + messageId + ":" + stringValue, e);
            }
        }
    }
    
    private class DeliverMessageTask implements Runnable {
        private final SMPPServerSession session;
        private String message;
		private String destinationAddress;
		private String sourceAddress;

		public DeliverMessageTask(SMPPServerSession session, String message,
				String sourceAddress, String desinationAddress) {
            this.session = session;
			this.message = message;
			this.sourceAddress = sourceAddress;
			destinationAddress = desinationAddress;
        }

        public void run() {
            try {
                Thread.sleep(deliveryDelay);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            try {
                session.deliverShortMessage(
                        "mc", 
                        TypeOfNumber.UNKNOWN, 
                        NumberingPlanIndicator.UNKNOWN, 
                        sourceAddress, 
                        TypeOfNumber.UNKNOWN, 
                        NumberingPlanIndicator.UNKNOWN, 
                        destinationAddress, 
                        new ESMClass(MessageMode.DEFAULT, MessageType.DEFAULT, GSMSpecificFeature.DEFAULT), 
                        (byte)0, 
                        (byte)0, 
                        new RegisteredDelivery(0),
                        new DataCoding1111(0),
                        message.getBytes());
                sendCounter.incrementAndGet();
                logger.debug("Sending message from " + sourceAddress + " to " + destinationAddress);
            } catch (Exception e) {
            	errorCounter.incrementAndGet();
            	logger.error("Failed sending message from " + sourceAddress + " to " + destinationAddress,e);
            }
        }
    }
    
    private class TrafficWatcherThread extends Thread {
        @Override
        public void run() {
            logger.info("Starting traffic watcher...");
            while (!exit) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                int requestPerSecond = requestCounter.getAndSet(0);
                int responsePerSecond = responseCounter.getAndSet(0);
                int sentPerSecond = sendCounter.getAndSet(0);
                totalRequestCounter.addAndGet(requestPerSecond);
                totalResponseCounter.addAndGet(responsePerSecond);
                totalSentCounter.addAndGet(sentPerSecond);
                logger.info("Traffic per second : requests {}, responses {}, sent {}",
                		new Object[]{requestPerSecond, responsePerSecond, sentPerSecond});
            }
        }
    }
    
    
    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(System.getProperty("jsmpp.simulator.port", DEFAULT_PORT.toString()));
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
        }
        SMPPServerSimulator smppServerSim = new SMPPServerSimulator(port, true);
        smppServerSim.run();
    }

	public void setResponseDelay(int responseDelay) {
		this.responseDelay = responseDelay;
	}

	public void setDeliveryReceiptDelay(int deliveryDelay) {
		this.deliveryDelay = deliveryDelay;
	}
	
	public int getTotalSentCount() {
		return totalSentCounter.get();
	}
	
	public int getErrorCount(){
		return errorCounter.get();
	}
	
	public boolean isShutdown() {
		return exit;
	}
	
	public void resetStats() {
		errorCounter.set(0);
		
		totalSentCounter.set(0);
		sendCounter.set(0);
		
		responseCounter.set(0);
		totalResponseCounter.set(0);
		
		requestCounter.set(0);
		totalRequestCounter.set(0);
	}
}
