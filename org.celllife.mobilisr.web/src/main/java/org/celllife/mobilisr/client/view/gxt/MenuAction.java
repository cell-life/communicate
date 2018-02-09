package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.ButtonArrowAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.resources.client.ImageResource;

public class MenuAction extends Action {

	private List<MenuActionItem> items;

	public MenuAction(String text, String tooltip, ImageResource image,
			String idPrefix) {
		super(text, tooltip, image, idPrefix);
		items = new ArrayList<MenuActionItem>();
	}

	@Override
	public Button render(final BeanModel model) {
		Button button = super.render(model);
		Menu localMenu = new Menu();
		
		for (final MenuActionItem item : items) {
			MenuItem localMenuItem = item.getMenuItem(model);
			if (localMenuItem == null)
				continue;
			
			localMenuItem.addListener(Events.Select, new Listener<BaseEvent>() {
				@Override
				public void handleEvent(BaseEvent be) {
					GridModelEvent gbe = new GridModelEvent((Menu) be.getSource());
					gbe.setModel(model);
					item.fireEvent(Events.Select, gbe);
				}
			});
			localMenu.add(localMenuItem);
		}
		
		button.setMenu(localMenu);
		button.setArrowAlign(ButtonArrowAlign.RIGHT);
		
		
		return button;
	}

	public void addMenuItem(MenuActionItem item) {
		items.add(item);
	}
}
