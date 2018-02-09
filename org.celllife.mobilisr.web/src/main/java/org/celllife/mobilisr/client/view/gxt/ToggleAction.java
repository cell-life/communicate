package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.resources.client.ImageResource;

/**
 * A ToggleAction is an Action whose text, tooltip, image
 * and idPrefix can be changed to an alternate version,
 * depending on the state of a Boolean bean property.
 *
 * To use, construct a ToggleAction object, then use setters
 * to set the alternate text, tooltip, image, idPrefix.
 */
public class ToggleAction extends Action {

	String altText = null;
	String altTooltip;
	ImageResource altImage;
	String altIdPrefix;
	private String toggleProperty;

	public ToggleAction(String text, String tooltip, ImageResource image, String idPrefix,
			String toggleProperty) {
		super(text, tooltip, image, idPrefix);
		this.toggleProperty = toggleProperty;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public void setAltTooltip(String altTooltip) {
		this.altTooltip = altTooltip;
	}

	public void setAltImage(ImageResource altImage) {
		this.altImage = altImage;
	}

	public void setAltIdPrefix(String altIdPrefix) {
		this.altIdPrefix = altIdPrefix;
	}

	@Override
	public Button render(final BeanModel model) {
		Button button = super.render(model);

		Boolean useAlternate = model.get(toggleProperty);
		button.setText(useAlternate ? altText : text);
		button.setIcon(createImage(useAlternate ? altImage : image));
		button.setToolTip(useAlternate ? altTooltip : tooltip);
		Object id = model.get(idProperty);
		if (id != null && idPrefix != null && altIdPrefix != null) {
			button.setId((useAlternate ? altIdPrefix : idPrefix) + "-" + id.toString());
		}

		return button;
	}

}
