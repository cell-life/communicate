package org.celllife.mobilisr.client.view.gxt;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrClientUtil;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class MyGXTSmsBox {

	private MyGXTTextArea messageTextArea;
	private ToolBar toolBar;
	private Status charCount;
	private Status messageCount;
	private int numMesgs;
	
	public MyGXTSmsBox(){
		createView(false);
	}
	
	public MyGXTSmsBox(boolean compulsory){
		createView(compulsory);
	}
	
	private void createView(boolean compulsory) {
		String messageFieldLabel = compulsory ? Messages.INSTANCE.compulsory() : "";
		messageFieldLabel += Messages.INSTANCE.smsboxMessage();
		messageTextArea = new MyGXTTextArea(messageFieldLabel, 
				CampaignMessage.PROP_MESSAGE,
				false, Messages.INSTANCE.smsboxDefaultMessageText());
		
		charCount = new Status();
		charCount.setText(Messages.INSTANCE.smsboxCharsLeft(160));
		charCount.setBox(true);
		messageCount = new Status();
		messageCount.setText(Messages.INSTANCE.smsboxCharsLeft(0));
		messageCount.setBox(true);
		
		toolBar = new ToolBar();
		toolBar.add(charCount);
		toolBar.add(new FillToolItem());
		toolBar.add(messageCount);
		
		messageTextArea.setId("smsBoxText");
		messageTextArea.setRegex(ServiceAndUIConstants.REGEX_TEXT_MESSAGE);
		messageTextArea.getMessages().setRegexText(ServiceAndUIConstants.VALIDATION_TEXT_MESSAGE);
		messageTextArea.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				updateSmsBoxToolBar();
				messageTextArea.validate();
			}
		});
		
	}

	public void updateSmsBoxToolBar() {
		int max_length = 160;
		String value = messageTextArea.getValue();
		numMesgs = MobilisrClientUtil.calculateNumberSMSs(value);
		int length = value != null ? value.length() : 0;

		if (length > 160)
			max_length = 153;

		int charsLeft = max_length - (length % (max_length + 1));

		
		charCount.setText(Messages.INSTANCE.smsboxCharsLeft(charsLeft));
		messageCount.setText(Messages.INSTANCE.smsboxMessage(numMesgs));
	}
	
	public void resetSmsBox(){
		messageTextArea.clear();
		charCount.setText(Messages.INSTANCE.smsboxCharsLeft(160));
		messageCount.setText(Messages.INSTANCE.smsboxCharsLeft(0));
	}
	
	public String getSmsMsg(){
		return messageTextArea.getValue();
	}
	
	public void setSmsMsg(String val){
		messageTextArea.setOriginalValue(val);
		messageTextArea.reset();
		updateSmsBoxToolBar();
	}
	
	public ToolBar getToolBar(){
		return toolBar;
	}
	
	public MyGXTTextArea getMsgTxtArea(){
		return messageTextArea;
	}
	
	public int getNumMesgs() {
		return numMesgs;
	}
}
