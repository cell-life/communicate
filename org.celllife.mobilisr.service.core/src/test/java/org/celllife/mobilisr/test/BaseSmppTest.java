package org.celllife.mobilisr.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.service.wasp.BaseMessageReceiverListener;
import org.celllife.mobilisr.service.wasp.SMPPServerSimulator;
import org.celllife.mobilisr.service.wasp.SmppGateway;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class BaseSmppTest {

	public final class MessageReceiver extends BaseMessageReceiverListener {
		
		private AtomicInteger count = new AtomicInteger();
		private List<String> deliveredMessage = new ArrayList<String>();
		private List<SmsMo> receivedMessages = new ArrayList<SmsMo>();
		
		public AtomicInteger getCount() {
			return count;
		}

		@Override
		protected void incomingMessageReceived(SmsMo smsMo) {
			receivedMessages.add(smsMo);
			count.incrementAndGet();
		}

		@Override
		protected void deliveryReceived(DeliveryReceipt deliveryReceipt) {
			String id = deliveryReceipt.getId();
			deliveredMessage.add(id);
			count.incrementAndGet();
		}

		public boolean containsMessage(String messageId) {
			return deliveredMessage.contains(messageId);
		}
		
		public List<SmsMo> getReceivedMessages() {
			return receivedMessages;
		}
	}

	protected static SMPPServerSimulator smppServerSimulator;

	@BeforeClass
	public static void setupClass(){
		smppServerSimulator = new SMPPServerSimulator(8065, true);
		smppServerSimulator.run();
	}
	
	@AfterClass
	public static void tearDownClass(){
		smppServerSimulator.shutdown();
	}

	private SmppGateway gateway;
	private MessageReceiver receiver;
	
	@Before
	public void setup() throws IOException{
		receiver = new MessageReceiver();
		gateway = new SmppGateway("localhost", 8065, new BindParameter(BindType.BIND_TRX,
				"", "", "communicate",
				TypeOfNumber.UNKNOWN,
				NumberingPlanIndicator.UNKNOWN, null), receiver);
	}
	
	public SmppGateway getGateway() {
		return gateway;
	}
	
	public MessageReceiver getReceiver() {
		return receiver;
	}
	
	public void sendMessage(String message, String sourceAddress, String desinationAddress){
		smppServerSimulator.sendMessage(message, sourceAddress, desinationAddress);
	}
}
