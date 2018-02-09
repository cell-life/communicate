package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.contacts.ContactsView;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.ui.Widget;

public class ContactsViewImpl extends LayoutContainer implements
		ContactsView {

	@Override
	public void createView(){

		// setHeaderVisible(false);
		setAutoWidth(true);
		// setBorders(true);
		setHeight("460");
		setLayout(new BorderLayout());

		ContentPanel leftPanel = new ContentPanel();
		leftPanel.setHeaderVisible(false);
		leftPanel.setBorders(true);
		// leftPanel.setFrame(true);

		VBoxLayout westLayout = new VBoxLayout();
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		leftPanel.setLayout(westLayout);

		ContentPanel rightPanel = new ContentPanel();
		// rightPanel.setFrame(true);
		rightPanel.setBorders(true);
		rightPanel.setHeaderVisible(false);
		rightPanel.setLayout(new FillLayout());

		Margins m = new Margins(0, 5, 0, 0);
		BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 130);
		west.setMargins(m);
		west.setSplit(false);
		add(leftPanel, west);

		Margins mc = new Margins(0, 0, 0, 5);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
		center.setMargins(mc);
		add(rightPanel, center);
	}

	public Widget getViewWidget() {
		return this;
	}
}
