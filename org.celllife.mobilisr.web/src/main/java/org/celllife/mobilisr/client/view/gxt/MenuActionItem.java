package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseObservable;
import com.extjs.gxt.ui.client.event.Observable;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.resources.client.ImageResource;

public class MenuActionItem extends BaseObservable implements Observable{

	private String text;

	private ImageResource image;

	public MenuActionItem(String text, ImageResource image) {
		this.text = text;
		this.image = image;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setImage(ImageResource image) {
		this.image = image;
	}

	public MenuItem getMenuItem(BeanModel model) {
		return new MyGXTMenuItem(text, image);
	}
}
