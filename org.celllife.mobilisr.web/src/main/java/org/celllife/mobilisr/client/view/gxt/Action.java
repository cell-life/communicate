/**
 *
 */
package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class Action{

	String text;
	String tooltip;
	ImageResource image;
	SelectionListener<GridModelEvent> buttonSelectionListener;
	protected final String idPrefix;
	protected String idProperty = "id";

	public Action(String text, String tooltip, ImageResource image,
			String idPrefix, SelectionListener<GridModelEvent> buttonSelectionListener) {
		super();
		this.text = text;
		this.tooltip = tooltip;
		this.image = image;
		this.idPrefix = idPrefix;
		this.buttonSelectionListener = buttonSelectionListener;
	}

	public Action(String text, String tooltip, ImageResource image,
			String idPrefix) {
		super();
		this.text = text;
		this.tooltip = tooltip;
		this.image = image;
		this.idPrefix = idPrefix;
	}

	public Button getButton(){
		final Button button = new MyGXTButton();
		if (image != null) {
			AbstractImagePrototype prototype = createImage(image);
			button.setIcon(prototype);
		}
		
		if (text != null && !text.isEmpty()) {
			button.setText(text);
		}
		
		if (tooltip != null && !tooltip.isEmpty()) {
			button.setToolTip(tooltip);
		}
		return button;
	}

	protected AbstractImagePrototype createImage(ImageResource image) {
		if (image != null) {
			AbstractImagePrototype imgPrototype = AbstractImagePrototype.create(image);
			return imgPrototype;
		}
		return null;
	}

	public void setImage(ImageResource image) {
		this.image = image;
	}

	public Button render(final BeanModel model) {
		Button button = getButton();
		Object id = model.get(idProperty);
		if (id != null && idPrefix != null) {
			button.setId(idPrefix + "-" + id.toString());
		}
		button.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (buttonSelectionListener != null){
					GridModelEvent gbe = new GridModelEvent((Button) ce.getSource());
					gbe.setModel(model);
					buttonSelectionListener.componentSelected(gbe);
				}
			}
		});
		return button;
	}

	public void setIdProperty(String idProperty) {
		this.idProperty = idProperty;
	}

	public void setListener(SelectionListener<GridModelEvent> buttonSelectionListener){
		this.buttonSelectionListener = buttonSelectionListener;
	}

	public void setText(String text) {
		this.text = text;
	}
}