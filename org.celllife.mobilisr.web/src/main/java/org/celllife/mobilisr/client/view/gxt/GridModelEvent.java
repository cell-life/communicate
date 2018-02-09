package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.Component;

public class GridModelEvent extends ComponentEvent {

	private BeanModel model;
	
	public GridModelEvent(Component component) {
		super(component);
	}
	public void setModel(BeanModel model) {
		this.model = model;
	}
	public BeanModel getModel() {
		return model;
	}
	
}
