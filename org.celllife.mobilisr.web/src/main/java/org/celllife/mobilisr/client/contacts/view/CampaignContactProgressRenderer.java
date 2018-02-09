package org.celllife.mobilisr.client.contacts.view;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class CampaignContactProgressRenderer implements
		GridCellRenderer<BeanModel> {
	@Override
	public Object render(BeanModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) {
		
		CampaignContact cc = model.getBean();
		Campaign c = cc.getCampaign();
		String text = cc.getProgress() + " of " + c.getDuration();
		if ( cc.getProgress() > c.getDuration()) {
			text = "Complete";
		}

		return text;
	}
}