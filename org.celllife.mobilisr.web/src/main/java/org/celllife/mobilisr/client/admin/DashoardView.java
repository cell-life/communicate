package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.admin.view.MobilisrPortlet;
import org.celllife.mobilisr.client.app.BasicView;

public interface DashoardView extends BasicView {

	void addPortlet(MobilisrPortlet portlet, int column);


}
