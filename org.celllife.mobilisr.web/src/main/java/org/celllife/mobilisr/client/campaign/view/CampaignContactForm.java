package org.celllife.mobilisr.client.campaign.view;

import java.util.List;

import org.celllife.mobilisr.client.validator.BeforeTimeFieldValidator;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTTimeField;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.ContactMsgTime;

import com.extjs.gxt.ui.client.binding.TimeFieldBinding;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;

/**
 * This class encapsulates the form for customising the message times for a campaign contact.
 * 
 * @author Simon Kelly
 *
 */
public class CampaignContactForm {
	
	private MyGXTTimeField[] timeFields;
	private TimeFieldBinding[] bindings;

	public CampaignContactForm(LayoutContainer panel, CampaignContact contact) {
		List<ContactMsgTime> messageTimes = contact.getContactMsgTimes();
		
		timeFields = new MyGXTTimeField[messageTimes.size()];
		bindings = new TimeFieldBinding[messageTimes.size()];
		
		for (int i = 0; i < messageTimes.size(); i++) {
			timeFields[i] = new MyGXTTimeField("Time for message " + (i + 1)
					+ " ?", String.valueOf(i), 30, false, true, true);
			timeFields[i].setLabelSeparator("");
			timeFields[i].setId("time"+i);
			timeFields[i].setForceSelection(true);
			timeFields[i].setTriggerAction(TriggerAction.ALL);
			timeFields[i].addListener(Events.Select,new SelectionListener<ComponentEvent>() {
				@Override
				public void componentSelected(ComponentEvent be) {
					for (int j = 0; j < timeFields.length; j++) {
						timeFields[j].validate();
					}
				}
			});
			panel.add(timeFields[i]);
			
			if (i > 0) {
				MyGXTTimeField previousField = timeFields[i - 1];
				timeFields[i].setValidator(new BeforeTimeFieldValidator(
						previousField));
			}
			
			bindings[i] = new TimeFieldBinding(timeFields[i], ContactMsgTime.PROP_MSG_TIME);
			bindings[i].setUpdateOriginalValue(true);
			ContactMsgTime msgTime = messageTimes.get(i);
			bindings[i].bind(ModelUtil.convertEntityToBeanModel(msgTime));
		}
	}
}
