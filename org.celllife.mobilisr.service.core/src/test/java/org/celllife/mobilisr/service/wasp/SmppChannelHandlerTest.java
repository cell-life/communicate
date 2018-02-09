package org.celllife.mobilisr.service.wasp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.RandomStringUtils;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SmppChannelHandlerTest {

	private final class GatewayImplementation implements Gateway {
		private long delay;
		private boolean simulateError;

		@Override
		public String submitShortMessage(String destinationAddr,
				byte[] shortMessage, String sourceAddress, String serviceType)
				throws ChannelProcessingException {

			if (delay > 0) {
				try {Thread.sleep(delay);} catch (InterruptedException e1) {}
			}
			
			if (simulateError){
				throw new ChannelProcessingException("simulated error");
			}
			
			return RandomStringUtils.random(10);
		}

		@Override
		public void shutdown() {
		}

		@Override
		public boolean isClosed() {
			return false;
		}

		public void setDelay(long delay) {
			this.delay = delay;
		}
		
		public void simulateError(boolean simulateError){
			this.simulateError = simulateError;
		}
	}

	private SmppChannelHandler handler;
	private GatewayImplementation gateway;
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	private int maxUnacked;

	@Before
	public void setup() {
		handler = new SmppChannelHandler("test", "test") {
		};
		gateway = new GatewayImplementation();
		gateway.setDelay(100);
		handler.setGateway(gateway);
		maxUnacked = 5;
		handler.setMaxUnacked(maxUnacked);
	}

    @Ignore //TODO: I need to find a better way to do this.
	@Test(timeout=10000)
	public void testChannelHandler() throws ChannelProcessingException {
		runTest(50,0);
	}

    @Ignore //TODO: I need to find a better way to do this.
	@Test(timeout=10000)
	public void testChannelHandler_withErrors() throws ChannelProcessingException {
		gateway.simulateError(true);
		runTest(50,50);
	}
	
	public void runTest(int sendNum, int expectedErrors) throws ChannelProcessingException {
		final AtomicInteger sent = new AtomicInteger();
		final AtomicInteger errors = new AtomicInteger();
		for (int i = 0; i < sendNum; i++){
			executor.execute(new Runnable(){
				@Override
				public void run() {
					try {
						SmsMt mt = handler.sendMTSms(new SmsMt("1234", "message",  "createdFor"));
						sent.incrementAndGet();
						if (mt.getStatus().isFailure()){
							errors.incrementAndGet();
						}
					} catch (ChannelProcessingException e) {
						e.printStackTrace();
					}
				}
			});
		}	
		
		int max = 0;
		do {
			int unacked = handler.getUnacked();
			if (unacked > max){
				max = unacked;
			}
			try {Thread.sleep(10);} catch (InterruptedException e1) {}
		} while (sent.get() < sendNum);
		
		Assert.assertTrue("Expected maxUnacked <= " + maxUnacked + " but was " + max, max <= maxUnacked);
		Assert.assertEquals(expectedErrors,errors.get());
	}

}
