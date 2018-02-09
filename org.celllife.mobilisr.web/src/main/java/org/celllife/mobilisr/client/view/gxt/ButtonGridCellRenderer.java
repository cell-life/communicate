package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class ButtonGridCellRenderer implements GridCellRenderer<BeanModel> {
	
	private List<Action> buttons = new ArrayList<Action>();

	public ButtonGridCellRenderer(){
	}
	
	public void addAction(Action action){
		buttons.add(action);
	}

	@Override
	public Object render(final BeanModel model, String property, ColumnData config, int rowIndex,
			int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.LEFT);
		buttonBar.setSpacing(10);
		for (Action bw : buttons) {
			Component action = bw.render(model);
			buttonBar.add(action);
		}
		return buttonBar;
		
	}

}