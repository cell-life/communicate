package org.celllife.mobilisr.client.view.gxt.grid;

import org.celllife.mobilisr.client.view.gxt.GridModelEvent;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Renders the column data as an anchor with an ID equal to
 * <code>cellIdPrefix-id</code>
 * 
 * @author Simon Kelly
 */
public class AnchorCellRenderer implements GridCellRenderer<BeanModel> {

	private SelectionListener<GridModelEvent> listener;
	private final String cellIdPrefix;

	public AnchorCellRenderer(String cellIdPrefix) {
		this.cellIdPrefix = cellIdPrefix;
	}

	public void setSelectionListener(SelectionListener<GridModelEvent> listener) {
		this.listener = listener;
	}

	@Override
	public Object render(final BeanModel model, String property, ColumnData config,
			int rowIndex, int colIndex, ListStore<BeanModel> store,
			Grid<BeanModel> grid) {

		String anchorText = getAnchorText(model, property);

		Anchor anchor = new Anchor(anchorText, "#");
		anchor.getElement().setId(cellIdPrefix + "-" + model.get("id"));
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				GridModelEvent gbe = new GridModelEvent(null);
				gbe.setModel(model);
				if (listener != null)
					listener.componentSelected(gbe);
			}
		});
		return anchor;
	}

	protected String getAnchorText(final BeanModel model, String property) {
		Object object = model.get(property);
		object = (object == null ? "" : object);
		String anchorText = object.toString();
		return anchorText;
	}
}
