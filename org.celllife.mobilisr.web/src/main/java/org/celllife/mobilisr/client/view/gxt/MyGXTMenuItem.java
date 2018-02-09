package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


public class MyGXTMenuItem extends MenuItem {

	public MyGXTMenuItem(String text, ImageResource image){
		super( text );
		if (image != null) {
			AbstractImagePrototype imagePrototype = AbstractImagePrototype.create(image);
			setIcon(imagePrototype);
		}
	}
}
