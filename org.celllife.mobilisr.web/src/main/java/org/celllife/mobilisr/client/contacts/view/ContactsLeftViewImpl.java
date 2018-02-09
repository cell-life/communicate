package org.celllife.mobilisr.client.contacts.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.contacts.ContactsLeftView;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.ui.Widget;

public class ContactsLeftViewImpl extends LayoutContainer implements ContactsLeftView{

	private Button myContactButton;
	private Button myGroupButton;
	private Button importContactButton;
	
	@Override
	public void createView(){
		myContactButton = new MyGXTButton("myContactButton", Messages.INSTANCE.contactListHeader());
		myGroupButton = new MyGXTButton("myGroupButton", Messages.INSTANCE.groupListHeader());
		importContactButton = new MyGXTButton("importContactButton", Messages.INSTANCE.importContactHeading());

		setBorders(true);
		setStyleAttribute("background-color", "white");
		
		VBoxLayoutData buttonLayout = new VBoxLayoutData(new Margins(10, 0, 10,	0));
		VBoxLayout westLayout = new VBoxLayout();
		westLayout.setPadding(new Padding(10));
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);

		setLayout(westLayout);

		add(myContactButton, buttonLayout);
		add(myGroupButton, buttonLayout);
		add(importContactButton, buttonLayout);
	
	}

	public Button getMyContactButton() {
		return myContactButton;
	}
	
	public Button getMyGroupButton() {
		return myGroupButton;
	}

	public Button getImportContactButton() {
		return importContactButton;
	}
	
	public Widget getViewWidget() {
		return this;
	}
}
