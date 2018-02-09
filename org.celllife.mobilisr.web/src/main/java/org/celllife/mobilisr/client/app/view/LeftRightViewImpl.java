package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.app.LeftRightView;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.Widget;

public class LeftRightViewImpl extends LayoutContainer implements LeftRightView {

	private LayoutContainer rightLayoutContainer;

	@Override
	public void createView(){
		setAutoWidth(true);
		setLayout(new BorderLayout());
		setStyleAttribute("background-color", "white");
		
		rightLayoutContainer = new LayoutContainer();
		rightLayoutContainer.setBorders(true);
		rightLayoutContainer.setLayout(new FillLayout());
		rightLayoutContainer.setStyleAttribute("background-color", "white");
		rightLayoutContainer.setStyleName("container");
		
	}
	
	public void setLeftLayoutContainer(Widget widget){
		removeAll();
		widget.addStyleName("container");
		
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 130);
		westData.setMargins(new Margins(0, 5 , 0 , 0));
		westData.setSplit(false);
		add(widget, westData);
		layout();
	}
	
	public void setRightLayoutContainer(Widget widget){
		rightLayoutContainer.removeAll();
		rightLayoutContainer.add(widget);
		
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));
		add(rightLayoutContainer , centerData);
		layout();
	}
	
	public Widget getViewWidget() {
		return this;
	}
}
