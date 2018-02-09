package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class ContactNameColumnRenderer implements GridCellRenderer<BeanModel> {

	@Override
	public Object render(BeanModel model, String property,
			com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex,
			int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
		Contact contact = getContact(model);
		String firstName = contact.getFirstName();
		String lastName = contact.getLastName();
		String displayName = (firstName != null) ? firstName : "";
		if ((firstName != null) && (lastName != null)) {
			displayName += " ";
		}
		if (lastName != null) {
			displayName += lastName;
		}
		return displayName;
	}

	/**
	 * @param model
	 * @return
	 */
	protected Contact getContact(BeanModel model) {
		Contact contact = model.getBean();
		return contact;
	}
}