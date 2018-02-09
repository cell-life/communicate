package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.resources.client.ImageResource;

public class ToggleMenuActionItem extends MenuActionItem {

	String altText;
	ImageResource altImage;
	private String toggleProperty;

	public ToggleMenuActionItem(String text, ImageResource image, String toggleProperty) {
		super(text, image);
		this.toggleProperty = toggleProperty;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public void setAltImage(ImageResource altImage) {
		this.altImage = altImage;
	}
	
	@Override
	public MenuItem getMenuItem(BeanModel model) {
		Boolean useAlternate = model.get(toggleProperty);
		
		if (useAlternate){
			return new MyGXTMenuItem(altText, altImage);
		}
		
		return super.getMenuItem(model);
	}
}
