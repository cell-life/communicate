package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.BasicView;

import com.extjs.gxt.ui.client.widget.button.Button;

public interface ContactsLeftView extends BasicView {

	public Button getMyContactButton();
	
	public Button getMyGroupButton();
	
	public Button getImportContactButton();
}
