package org.celllife.mobilisr.service.wasp;

import java.text.MessageFormat;

import junit.framework.Assert;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.RawMessage;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegratTransformerTests{
	
	private String responseTemplate;
	private String receiptTemplate;

	@Before
	public void buildResponseTemplate(){
		StringBuilder moTemplateBuilder = new StringBuilder();
		moTemplateBuilder.append("<Message><Version Version=\"1.0\"/><Response Type=\"OnReceiveSMS\"><SystemID>Higate</SystemID>");
		moTemplateBuilder.append("<UserID>MO_USERNAME</UserID>");
		moTemplateBuilder.append("<Service>MO_SERVICE_CODE</Service><Network ID=\"{0}\" MCC=\"655\" MNC=\"001\"/>");
		moTemplateBuilder.append("<OnReceiveSMS SeqNo=\"{1}\" Sent=\"20100801120803\" FromAddr=\"{2}\" ToAddr=\"{3}\" ToTag=\"\" Value=\"0\" NetworkID=\"1\" AdultRating=\"0\">");
		moTemplateBuilder.append("<Content Type=\"TEXT\">{4}</Content></OnReceiveSMS></Response></Message>");
		responseTemplate = moTemplateBuilder.toString();
		
		StringBuilder mtRspTemplateBuilder = new StringBuilder();
		mtRspTemplateBuilder.append("<Message><Version Version=\"1.0\"/><CreditBalance Account=\"-10\" Client=\"0\"/>");
		mtRspTemplateBuilder.append("<Response Type=\"OnResult\" TOC=\"SMS\" RefNo=\"{0}\" SeqNo=\"{1}\">");
		mtRspTemplateBuilder.append("<SystemID>Higate</SystemID><UserID>cell-lifesim</UserID>");
		mtRspTemplateBuilder.append("<Service>CLFESIM</Service><NetworkID>{2}</NetworkID>");
		mtRspTemplateBuilder.append("<OnResult Flags=\"0\" Code=\"{3}\" SubCode=\"0\" Text=\"{4}\"/>");
		mtRspTemplateBuilder.append("</Response></Message>");
		receiptTemplate = mtRspTemplateBuilder.toString();
	}
	
	@Test
	public void testMo() throws ChannelProcessingException{
		
		String from = "123456789";
		String message = "message content";
		String waspMOData = buildResponse(1, "123456", from, "987654321", message);

		IntegratHttpInHandler transformer = new IntegratHttpInHandler();
		SmsMo smsmo = transformer.transformIncomingMessage(new RawMessage(waspMOData, null));
		
		Assert.assertEquals(from, smsmo.getSourceAddr());
		Assert.assertEquals(message, smsmo.getMessage());
		Assert.assertEquals("Vodacom", smsmo.getMobileNetwork());
	}
	
	@Test
	public void testMoSpecialChars() throws ChannelProcessingException{
		String from = "123456789";
		String message = "message content ~!@#$%^&*()`/?><,.\":;'}{|[]\'";
		String waspMOData = buildResponse(1, "123456", from, "987654321", message);

		IntegratHttpInHandler transformer = new IntegratHttpInHandler();
		SmsMo smsmo = transformer.transformIncomingMessage(new RawMessage(waspMOData, null));
		
		Assert.assertEquals(from, smsmo.getSourceAddr());
		Assert.assertEquals(message, smsmo.getMessage());
		Assert.assertEquals("Vodacom", smsmo.getMobileNetwork());
	}
	
	@Test
	public void testMoSpecialChars_escaped() throws ChannelProcessingException{
		String from = "123456789";
		String message = "message content &quot; &amp;";
		String waspMOData = buildResponse(1, "123456", from, "987654321", message);

		IntegratHttpInHandler transformer = new IntegratHttpInHandler();
		SmsMo smsmo = transformer.transformIncomingMessage(new RawMessage(waspMOData, null));
		
		Assert.assertEquals(from, smsmo.getSourceAddr());
		Assert.assertEquals("message content \" &", smsmo.getMessage());
		Assert.assertEquals("Vodacom", smsmo.getMobileNetwork());
	}

    @Ignore
	@Test
	public void testMoMultiLine() throws ChannelProcessingException{
		String from = "123456789";
		String message = "message content \n more contest \n\r some \t more";
		String waspMOData = buildResponse(1, "123456", from, "987654321", message);

		IntegratHttpInHandler transformer = new IntegratHttpInHandler();
		SmsMo smsmo = transformer.transformIncomingMessage(new RawMessage(waspMOData, null));
		
		Assert.assertEquals(from, smsmo.getSourceAddr());
		Assert.assertEquals(message, smsmo.getMessage());
		Assert.assertEquals("Vodacom", smsmo.getMobileNetwork());
	}

    @Test
    public void testMoHexFormat() throws ChannelProcessingException{

        String from = "27724194158";
        String packet = "<Message>\n<Version Version=\"1.0\"/>\n<Response Type=\"OnReceive SMS\">\n\t<SystemID>Higate</SystemID>\n\t<UserID>cell43</UserID>\n\t<Service>CEL43740</Service>\n\t<Network ID=\"2\" MCC=\"655\" MNC=\"010\"/>\n\t<OnReceiveSMS\n\tSeqNo=\"77721429\"\n\tSent=\"20130603071213\"\n\tFromAddr=\"27724194158\"\n\tToAddr=\"43740\"\n\tToTag=\"\"\n\tValue=\"0\"\n\tNetworkID=\"2\"\n\tAdultRating=\"0\"\n\tDataCoding=\"8\"\n\tEsmClass=\"0\">\n<Content Type=\"HEX\">004D004D0043</Content>\n</OnReceiveSMS>\n</Response>\n</Message>";
        RawMessage rm = new RawMessage(packet, null);

        IntegratHttpInHandler transformer = new IntegratHttpInHandler();
        SmsMo smsmo = transformer.transformIncomingMessage(rm);

        Assert.assertEquals(from, smsmo.getSourceAddr());
        Assert.assertEquals("MTN", smsmo.getMobileNetwork());
        Assert.assertEquals("MMC", smsmo.getMessage());
	}
	
	@Test
	public void testMtRspFromWaspParamExtract(){
		buildAndCheckMTPostWaspRsp("27785120109", "010101", "1", "1", "Queued");
		buildAndCheckMTPostWaspRsp("27785120109", "010102", "2", "2", "Submitted");
		buildAndCheckMTPostWaspRsp("27785120109", "010103", "3", "3", "Acknowledged");
		buildAndCheckMTPostWaspRsp("27785120109", "010104", "2", "4", "Recipeted");
	}
	
	@Test
	public void testPostRspFromWasp_queued() throws ChannelProcessingException{
		testPostRspFromWasp(1, IntegratHttpOutHandler.IntegratStatus.RC_QUEUED.name());
	}
	
	@Test
	public void testPostRspFromWasp_submitted() throws ChannelProcessingException{
		testPostRspFromWasp(2, IntegratHttpOutHandler.IntegratStatus.RC_SUBMITTED.name());
	}
	
	@Test
	public void testPostRspFromWasp_acknowledged() throws ChannelProcessingException{
		testPostRspFromWasp(3, IntegratHttpOutHandler.IntegratStatus.RC_ACKNOWLEDGED.name());
	}
	
	@Test
	public void testPostRspFromWasp_receipted() throws ChannelProcessingException{
		testPostRspFromWasp(4, IntegratHttpOutHandler.IntegratStatus.RC_RECEIPTED.name());
	}
	
	@Test
	public void testPostRspFromWasp_failed() throws ChannelProcessingException{
		testPostRspFromWasp(5, IntegratHttpOutHandler.IntegratStatus.RC_FAILED.name());
	}
	
	public void testPostRspFromWasp(final int statusCode, final String statusText) throws ChannelProcessingException{
		final String refNum = "27785120101";
		final String seqNum = "02424101";

		final int netowrkId = 2;
		String msgPostFromWasp =  MessageFormat.format(receiptTemplate, refNum, seqNum, netowrkId, statusCode, statusText);
		
		IntegratHttpOutHandler transformer = new IntegratHttpOutHandler();
		DeliveryReceipt receipt = transformer.transformDeliveryReceipt(new RawMessage(msgPostFromWasp, null));
		
		Assert.assertEquals(
				IntegratHttpOutHandler.IntegratStatus.valueOf(statusText).deliveryState,
				receipt.getFinalStatus());
		Assert.assertEquals(seqNum, receipt.getId());
	}

	private String buildResponse(int networkid, String seqno, String from, String to, String message){
		return MessageFormat.format(responseTemplate, networkid, seqno, from, to, message);
	}
	
	private void buildAndCheckMTPostWaspRsp(String refNum, String seqNum, String networkId, String statusCode, String statusText){
		String msgPostFromWasp =  MessageFormat.format(receiptTemplate, refNum, seqNum, networkId, statusCode, statusText);
		
		String filtNetworkID = MobilisrUtility.findValueForRegExp(msgPostFromWasp, IntegratHttpOutHandler.MT_WASPRSP_REGEXP_NETWORK);
		String filtRefNum = MobilisrUtility.findValueForRegExp(msgPostFromWasp, IntegratHttpOutHandler.MT_WASPRSP_REGEXP_REFNUM);
		String filtSeqNum = MobilisrUtility.findValueForRegExp(msgPostFromWasp, IntegratHttpOutHandler.MT_WASPRSP_REGEXP_SEQ_NUM);
		String filtStatusCode = MobilisrUtility.findValueForRegExp(msgPostFromWasp, IntegratHttpOutHandler.MT_WASPRSP_REGEXP_STATUSCODE);
		String filtWaspStatus = MobilisrUtility.findValueForRegExp(msgPostFromWasp, IntegratHttpOutHandler.MT_WASPRSP_REGEXP_TEXT);
		
		Assert.assertEquals(refNum, filtRefNum);
		Assert.assertEquals(seqNum, filtSeqNum);
		Assert.assertEquals(networkId, filtNetworkID);
		Assert.assertEquals(statusCode, filtStatusCode);
		Assert.assertEquals(statusText, filtWaspStatus);
		
	}
}


