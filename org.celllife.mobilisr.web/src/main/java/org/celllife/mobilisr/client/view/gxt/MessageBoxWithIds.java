package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * Utility class for generating message boxes that have <code>id</code attributes set
 * for their buttons.
 *  
 * @author Simon Kelly
 */
public class MessageBoxWithIds {

	public static MessageBox info(String title, String msg, Listener<MessageBoxEvent> callback) {
		MessageBox box = MessageBox.info(title, msg, callback);
		setButtonId(box, Dialog.OK);
		return box;
	}

	public static MessageBox confirm(String title, String msg, Listener<MessageBoxEvent> callback) {
		MessageBox box = MessageBox.confirm(title, msg, callback);
		setButtonId(box, Dialog.YES);
		setButtonId(box, Dialog.NO);
		return box;
	}
	
	public static MessageBox alert(String title, String msg, Listener<MessageBoxEvent> callback) {
		MessageBox box = MessageBox.alert(title, msg, callback);
		setButtonId(box, Dialog.OK);
		return box;
	}

	private static void setButtonId(MessageBox box, String buttonId) {
		Dialog dialog = box.getDialog();
		Button button = dialog.getButtonById(buttonId);
		if (button != null){
			button.setId("msg-box-button-" + buttonId);
		}
	}

}
