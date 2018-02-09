package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.ui.Image;

/**
 * Utility Class to show a busy indicator
 * when the system is processing a user generated event.
 */
public class BusyIndicator {
	private static Window popup;

    private BusyIndicator() { }
    
    /**
     * Show the indicator in the center of the screen. 
     */
    public static void showBusyIndicator() {
		showBusyIndicator(null);
	}
    
    /**
     * Show the indicator in the center of the screen with a message 
     */
    public static void showBusyIndicator(String message) {
        Window win = getBusyIndicator();
    	
    	win.show();
    	if (message != null && !message.isEmpty()) {
    		win.setHeading(message);
    		win.setWidth(message.length()*8);
    	} else {
    		win.setHeading("Loading...");
    		win.setWidth(65);
    	}
    	win.setHeight(65);
        win.center();
    }
    
    /**
     * Hides the busy indicator
     */
    public static void hideBusyIndicator() {
        if (popup != null) {
            popup.hide();
        }
    }

    /**
     * Gets a reference to the popup,
     * and creates it if it is not already initialised
     * @return ProgressIndicator
     */
    private static Window getBusyIndicator() {
      if (popup == null) {
          popup = new Window();
          popup.setFrame(true);
          popup.setBodyBorder(false);
          popup.setDraggable(false);
          popup.setClosable(false);
          popup.setResizable(false);
          popup.setModal(true);
          popup.setBodyStyle("backgroundColor: #CED9E7");
          
          VBoxLayout layout = new VBoxLayout();  
          layout.setPadding(new Padding(5));  
          layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER); 
          
          popup.setLayout(layout);
          Image img = new Image("gxt/images/default/shared/large-loading.gif");
          popup.add(img, new VBoxLayoutData(new Margins(0)));
      }
      
      return popup;
    }
}
