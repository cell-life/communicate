package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.admin.DashoardView;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.google.gwt.user.client.ui.Widget;

public class DashoardViewImpl extends LayoutContainer implements DashoardView {

	private Portal portal;

	@Override
	public void createView() {
		portal = new Portal(3);
		portal.setColumnWidth(0, 0.33);
		portal.setColumnWidth(1, 0.33);
		portal.setColumnWidth(2, 0.33);
		
		add(portal);	
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void addPortlet(MobilisrPortlet portlet, int column) {
		portal.add(portlet.getPortlet(), column);
		
	}
}