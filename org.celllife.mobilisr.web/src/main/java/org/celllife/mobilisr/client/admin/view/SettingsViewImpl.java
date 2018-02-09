package org.celllife.mobilisr.client.admin.view;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

public final class SettingsViewImpl extends LayoutContainer implements SettingsView{

	private SettingsListViewImpl listView;
	private SettingsCreateViewImpl createView;

    @Override
    public void createView() {

    	setLayout(new RowLayout(Orientation.HORIZONTAL));
		setBorders(true);
		
		listView = new SettingsListViewImpl();
		listView.createView();
		createView = new SettingsCreateViewImpl();
		createView.createView();

		Margins m = new Margins(5);
		add(listView.getViewWidget(), new RowData(.5, 1, m));
		add(createView.getViewWidget(), new RowData(.5, 1, m));
    }

	@Override
	public Widget getViewWidget() {
		return this;
	}
	
	@Override
	public SettingsListViewImpl getListView() {
		return listView;
	}
	
	@Override
	public SettingsCreateViewImpl getCreateView() {
		return createView;
	}
	
	@Override
	public boolean isDirty() {
		return createView.isDirty();
	}
	
	@Override
	public void setDirty(boolean dirty) {
		createView.setDirty(dirty);
	}
	
	@Override
	public void setDirty() {
		setDirty(true);
	}
}