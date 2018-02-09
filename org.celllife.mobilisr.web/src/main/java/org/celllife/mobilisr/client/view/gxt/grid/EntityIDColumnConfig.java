package org.celllife.mobilisr.client.view.gxt.grid;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renders the column data in a span with an ID equal to <code>cellIdPrefix-id</code>
 * 
 * @author Simon Kelly
 */
public class EntityIDColumnConfig extends ColumnConfig {

	public EntityIDColumnConfig(String columnId, String columnName, int width, String cellIdPrefix) {
		super(columnId, columnName, width);
		configureRenderer(cellIdPrefix);
	}
	
	protected void configureRenderer(final String cellIdPrefix){
		super.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				Object object = model.get(property);
				object = (object==null?"":object);
				
				return "<span id=" + cellIdPrefix + "-" + model.get("id") + ">" + object + "</span>";
			}
		});
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setRenderer(GridCellRenderer renderer) {
		// disable setting renderer
	}
}
