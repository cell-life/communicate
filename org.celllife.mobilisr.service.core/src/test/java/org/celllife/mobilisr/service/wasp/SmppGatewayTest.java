package org.celllife.mobilisr.service.wasp;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.test.BaseSmppTest;
import org.celllife.mobilisr.test.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("This test was randomly failing")
public class SmppGatewayTest extends BaseSmppTest {

	@Test(timeout=5000)
	public void testSmppGateway() throws ChannelProcessingException {
		
		String messageId = getGateway().submitShortMessage(
				MockUtils.createMsisdn(0), "test message".getBytes(),
				"80256478", "");
		Assert.assertNotNull(messageId);
		
		while (getReceiver().getCount().get() < 1){
			try {
				Thread.sleep(500);
				System.out.print(".");
			} catch (InterruptedException e) {
			}
		}
		
		Assert.assertTrue(getReceiver().containsMessage(messageId));
	}
	
	@Test(timeout=5000)
	public void testSmppGateway_mulitpleMessages() throws ChannelProcessingException{
		List<String> messageIds = new ArrayList<String>();
		int count = 100;
		for (int i = 0;i < count;i++){
			String messageId = getGateway().submitShortMessage(
					MockUtils.createMsisdn(0), ("test message" + i).getBytes(),
					"80256478", "");
			Assert.assertNotNull(messageId);
			messageIds.add(messageId);
		}
		
		while (getReceiver().getCount().get() < count){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		
		for (String id : messageIds) {
			Assert.assertTrue("Receiver is missing id " + id, getReceiver().containsMessage(id));
		}
	}
	
	@Test(timeout=5000)
	public void testSmppGateway_longMessage() throws ChannelProcessingException{
		String message = TestUtils.getLoremIpsum(500);
		String messageId = getGateway().submitShortMessage(
				MockUtils.createMsisdn(0), message.getBytes(),
				"80256478", "");
		Assert.assertNotNull(messageId);
		String[] ids = messageId.split(",");
		Assert.assertEquals(4, ids.length);
		
		while (getReceiver().getCount().get() < 4){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		
		for (String id : ids) {
			Assert.assertTrue("Receiver is missing id " + id,getReceiver().containsMessage(id));
		}
	}
	
	@Test(timeout=5000)
	public void testSmppGateway_receiveMessage() throws ChannelProcessingException{
		String message = TestUtils.getLoremIpsum(100);
		String msisdn = MockUtils.createMsisdn(0);
		String desinationAddress = "123456";
		sendMessage(message, msisdn, desinationAddress);

		List<SmsMo> receivedMessages = getReceiver().getReceivedMessages();
		
		while (getReceiver().getCount().get() < 1){
			try {
				Thread.sleep(500);
				System.out.print(".");
			} catch (InterruptedException e) {
			}
		}
		
		Assert.assertEquals(1, receivedMessages.size());
		SmsMo smsMo = receivedMessages.get(0);
		Assert.assertEquals(msisdn, smsMo.getSourceAddr());
		Assert.assertEquals(desinationAddress, smsMo.getDestAddr());
		Assert.assertEquals(message, smsMo.getMessage());
	}
}
