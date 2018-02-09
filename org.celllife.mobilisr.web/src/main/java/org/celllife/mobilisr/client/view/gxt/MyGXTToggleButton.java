package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class MyGXTToggleButton extends ToggleButton {

	List<MobilisrPermission> buttonPermissions = new ArrayList<MobilisrPermission>();

	private AbstractImagePrototype toggledImage;
	private AbstractImagePrototype unToggledImage;
	
	private String toggledTooltip;
	private String unToggledTooltip;

	private String toggledText;
	private String unToggledText;
	
	public MyGXTToggleButton() {
		addPlugin(new DisableDoubleClick());
	}

	public MyGXTToggleButton( String text ) {
		super( text );
		unToggledText = text;
		addPlugin(new DisableDoubleClick());
		initToggleListener();
	}

	public MyGXTToggleButton( String buttonId, String text ) {
		super( text );
		unToggledText = text;
		addPlugin(new DisableDoubleClick());
		setId(buttonId);
		initToggleListener();
	}
	
	public MyGXTToggleButton( String buttonId, String text, ImageResource image, IconAlign iconAlign, ButtonScale buttonScale) {
		super( text );
		unToggledText = text;
		unToggledImage = AbstractImagePrototype.create(image);
		addPlugin(new DisableDoubleClick());
		setId(buttonId);
		
		setScale(buttonScale);
		setIconAlign(iconAlign);
		setIcon(unToggledImage);
		initToggleListener();
	}
	
	private void initToggleListener() {
		addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if (isPressed()){
					if (toggledImage != null)
						setIcon(toggledImage);
					
					if (toggledText != null)
						setText(toggledText);
					
					if (toggledTooltip != null)
						MyGXTToggleButton.super.setToolTip(toggledTooltip);
				} else {
					if (unToggledImage != null)
						setIcon(unToggledImage);
					
					if (unToggledText != null)
						setText(unToggledText);
					
					if (unToggledTooltip != null)
						MyGXTToggleButton.super.setToolTip(unToggledTooltip);
				}
			}
		});
	}
	
	public void setToggledIcon(ImageResource image){
		toggledImage = AbstractImagePrototype.create(image);
	}
	
	public void setToggledTooltip(String tooltip){
		toggledTooltip = tooltip;
	}

	public void setToggledText(String toggledText){
		this.toggledText = toggledText;
	}
	
	@Override
	public void setToolTip(String text) {
		unToggledTooltip = text;
		super.setToolTip(text);
	}
	
	public List<MobilisrPermission> getButtonPermissions() {
		return buttonPermissions;
	}

	public void setButtonPermissions(List<MobilisrPermission> buttonPermissions) {
		this.buttonPermissions = buttonPermissions;
	}
	
	public void addButtonPermission(MobilisrPermission mobilisrPermission){
		this.buttonPermissions.add(mobilisrPermission);
	}
	
}
