package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class MyGXTButton extends Button {

	List<MobilisrPermission> buttonPermissions = new ArrayList<MobilisrPermission>();
	
	public MyGXTButton() {
		addPlugin(new DisableDoubleClick());
	}

	public MyGXTButton( String text ) {
		super( text );
		addPlugin(new DisableDoubleClick());
	}

	public MyGXTButton( String buttonId, String text ) {
		super( text );
		addPlugin(new DisableDoubleClick());
		setId(buttonId);
	}
	
	public MyGXTButton( String buttonId, String text, ImageResource image, IconAlign iconAlign, ButtonScale buttonScale ) {
		super( text );
		addPlugin(new DisableDoubleClick());
		setId(buttonId);
		
		setScale(buttonScale);
		setIconAlign(iconAlign);
		setIcon(image);
	}
	
	public void setIcon(ImageResource image){
		setIcon(AbstractImagePrototype.create(image));
	}
	
	public MyGXTButton( String buttonId, String text, ImageResource image, IconAlign iconAlign, ButtonScale buttonScale, List<MobilisrPermission> buttonPermissions) {
		super( text );
		setId(buttonId);
		
		setScale(buttonScale);
		setIconAlign(iconAlign);
		setIcon(image);
		this.buttonPermissions = buttonPermissions;
	}
	
	public void displayButtonBasedOnUserPermission(){
		
		for (MobilisrPermission mobilisrPermission : buttonPermissions) {
			if(UserContext.hasPermission(mobilisrPermission)){
				setVisible(true);
				break;
			}
		}
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
