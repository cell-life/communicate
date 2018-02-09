package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public class MyGXTComboBox<D extends ModelData> extends ComboBox<D> {
	
	public MyGXTComboBox(String emptyText, String displayField, boolean paged) {
		setForceSelection(true);
		setEmptyText(emptyText);
		setDisplayField(displayField);
		setMinChars(2);
		if (paged){
			setPageSize(10);
			setMinListWidth(200);
		}
	}

}
