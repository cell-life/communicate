package org.celllife.mobilisr.service.wasp;

import java.util.HashMap;
import java.util.Map;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.junit.Assert;
import org.junit.Test;

public class AATHttpInHandlerTest {

	@Test
	public void testAATShortCodeHappyDays() throws Exception {
		AATHttpInHandler aat = new AATHttpInHandler();
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put("prem", new String[] {"30612"});
		parameterMap.put("mesg", new String[] {"Please call blah blah blah. Some advertising"});
		parameterMap.put("num", new String[] {"2776819807"});
		parameterMap.put("tonum", new String[] {"2776819808"});
		parameterMap.put("id", new String[] {"12345"});
		RawMessage rawMessage = new RawMessage("", parameterMap);
		SmsMo smsMO = aat.transformIncomingMessage(rawMessage);
		Assert.assertEquals("12345", smsMO.getReference());
		Assert.assertEquals("Please call blah blah blah. Some advertising", smsMO.getMessage());
		Assert.assertEquals("30612", smsMO.getDestAddr());
		Assert.assertEquals("2776819807", smsMO.getSourceAddr());
		Assert.assertEquals("Unknown", smsMO.getMobileNetwork());
	}
}
