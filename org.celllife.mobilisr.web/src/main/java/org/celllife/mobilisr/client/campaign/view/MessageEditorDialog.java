package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.view.gxt.FormDialog;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTSmsBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTTimeField;
import org.celllife.mobilisr.domain.CampaignMessage;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class MessageEditorDialog extends FormDialog {

	private MyGXTSmsBox smsbox;
	private MyGXTNumberField dayToSend;
	private MyGXTTimeField timeToSend;
	private String oldMsg;
	
	public MessageEditorDialog() {
		buildDialog();
	}
	
	@Override
	protected void createFormContents(FormPanel formPanel) {
		setHideOnSubmit(true);
		setHeading("Edit Message");
		
		dayToSend = new MyGXTNumberField("Day to Send",
				CampaignMessage.PROP_MSG_DAY, false, "", 0);
		timeToSend = new MyGXTTimeField("Time to Send ",
				CampaignMessage.PROP_MSG_TIME, 30, false, true, false);
		
		smsbox = new MyGXTSmsBox();
		smsbox.getMsgTxtArea().setName(CampaignMessage.PROP_MESSAGE);

		dayToSend.setId("dayToSend");
		dayToSend.setPropertyEditorType(Integer.class);
		dayToSend.disable();

		timeToSend.setId("timeToSend");
		timeToSend.setForceSelection(true);
		timeToSend.setTriggerAction(TriggerAction.ALL);
		timeToSend.disable();
		
		formPanel.add(smsbox.getMsgTxtArea(), new FormData("95%") );
		smsbox.getMsgTxtArea().setHeight(180);
		formPanel.add(new AdapterField(smsbox.getToolBar() ), new FormData("95%") );
		formPanel.add(dayToSend);
		formPanel.add(timeToSend);
		
		addListeners();
	}
	
	@Override
	protected void createFormBinding() {
		super.createFormBinding();
		
		// Update the message toolbar once binding has taken place - this makes the dialog
		//  show the correct character count when first displayed.
		getFormBindings().addListener(Events.Bind, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				smsbox.updateSmsBoxToolBar();
			}
		});
	}

	private void addListeners() {
		getCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				BeanModel model = (BeanModel) getFormBindings().getModel();
				model.set(CampaignMessage.PROP_MESSAGE, oldMsg);
			}
		});

		dayToSend.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				dayToSend.validate();
			}
		});
	}
	
	

	@Override
	public void bind(BeanModel beanModel){
		super.bind(beanModel);
		oldMsg = beanModel.get(CampaignMessage.PROP_MESSAGE);
	}

	public void setDayTimeEnabled(boolean b) {
		dayToSend.setEnabled(b);
		timeToSend.setEnabled(b);
	}

}
