package org.celllife.mobilisr.service.wasp;

import java.util.HashMap;
import java.util.Map;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.junit.Assert;
import org.junit.Test;

public class PanceaMobileHttpInHandlerTest {

	@Test
	public void testPanceaMobileShortCodeHappyDays() throws Exception {
		PanceaMobileHttpInHandler aat = new PanceaMobileHttpInHandler();
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put("to", new String[] {"30612"});
		parameterMap.put("message", new String[] {"Please call blah blah blah. Some advertising"});
		parameterMap.put("from", new String[] {"2776819807"});
		parameterMap.put("code", new String[] {"12345"});
		RawMessage rawMessage = new RawMessage("", parameterMap);
		SmsMo smsMO = aat.transformIncomingMessage(rawMessage);
		Assert.assertEquals("12345", smsMO.getReference());
		Assert.assertEquals("Please call blah blah blah. Some advertising", smsMO.getMessage());
		Assert.assertEquals("30612", smsMO.getDestAddr());
		Assert.assertEquals("2776819807", smsMO.getSourceAddr());
		Assert.assertEquals("Unknown", smsMO.getMobileNetwork());
	}

	@Test
	public void testPanceaMobileMsisdnPlus27() throws Exception {
		PanceaMobileHttpInHandler aat = new PanceaMobileHttpInHandler();
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put("to", new String[] {"30612"});
		parameterMap.put("message", new String[] {"Please call blah blah blah. Some advertising"});
		parameterMap.put("from", new String[] {"+2776819807"});
		parameterMap.put("code", new String[] {"12345"});
		RawMessage rawMessage = new RawMessage("", parameterMap);
		SmsMo smsMO = aat.transformIncomingMessage(rawMessage);
		Assert.assertEquals("12345", smsMO.getReference());
		Assert.assertEquals("Please call blah blah blah. Some advertising", smsMO.getMessage());
		Assert.assertEquals("30612", smsMO.getDestAddr());
		Assert.assertEquals("2776819807", smsMO.getSourceAddr());
		Assert.assertEquals("Unknown", smsMO.getMobileNetwork());
	}
}
