package org.celllife.mobilisr.service.wasp;

import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.junit.Assert;
import org.junit.Test;

public class TelfreeTests {
	
	@Test
	public void testSeqNumRegex(){
		String expected= "1306744539517207110113852";
		String body = "<messages><message messageId=\""+expected+"\"/></messages>";
		String value = MobilisrUtility.findValueForRegExp(body,TelfreeHttpOutHandler.MT_REGEXP_SEQ_NUM);
		Assert.assertEquals(expected, value);
	}

}
