package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.app.RegionTemplateView;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

/**
 * Class that contains the basic layout structure for the application and provides
 * ability to add different widgets to the layout.
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
public class RegionTemplateViewImpl extends Composite implements RegionTemplateView{

	private Viewport viewPort = new Viewport();
	Margins m = new Margins(5);
			
	public RegionTemplateViewImpl(){
	
		viewPort.setLayout(new RowLayout(Orientation.VERTICAL));
		viewPort.setHeight(Window.getClientHeight());
		viewPort.setWidth(Window.getClientWidth());
		
		initComponent(viewPort);
	}	
	
	//This is the xyz functionality related content
	public void setContentWidget(Widget widget) {
		
		if( viewPort.getItemCount() > 2 ){
			Component c1 = viewPort.getItem(1);
			Component c2 = viewPort.getItem(2);
			c1.removeFromParent();
			c2.removeFromParent();
			viewPort.add(widget, new RowData(1, 1, m));
			viewPort.add(c2, new RowData(1, -1, m));	
			viewPort.layout();
		}else{
			viewPort.add(widget, new RowData(1, 1, m));
		}
	}

	//This is the footer widget by default contianing footer label 
	public void setFooterWidget(Widget widget) {
		widget.setStyleName("container");
		viewPort.add(widget, new RowData(1, -1, m));		
	}

	//This is the top widget by default containing button bar, welcome msg and logout button
	public void setHeaderWidget(Widget widget) {
		widget.setStyleName("container");
		viewPort.add(widget, new RowData(1, -1, m));		
	}

}
