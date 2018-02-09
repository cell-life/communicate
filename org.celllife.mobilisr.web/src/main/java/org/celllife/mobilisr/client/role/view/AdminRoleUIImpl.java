package org.celllife.mobilisr.client.role.view;

import org.celllife.mobilisr.client.role.AdminRoleCreateView;
import org.celllife.mobilisr.client.role.AdminRoleListView;
import org.celllife.mobilisr.client.role.AdminRoleUI;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

public class AdminRoleUIImpl extends LayoutContainer implements AdminRoleUI{

	private Margins m = new Margins(5);
	private RowLayout panelLayout = new RowLayout(Orientation.HORIZONTAL);
		
	private AdminRoleListView adminRoleListView;
	private AdminRoleCreateView adminRoleCreateView;
	
	@Override
	public void createView(){
		setLayout(panelLayout);
		setBorders(true);
		
		adminRoleListView = new AdminRoleListViewImpl();
		adminRoleCreateView = new AdminRoleCreateViewImpl();
		
		add(adminRoleListView.getViewWidget(), new RowData(.3, 1, m));
		add(adminRoleCreateView.getViewWidget(), new RowData(.7, 1, m));
	}

	@Override
	public AdminRoleCreateView getAdminRoleCreateView() {
		return adminRoleCreateView;
	}


	@Override
	public AdminRoleListView getAdminRoleListView() {
		return adminRoleListView;
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}
	
	@Override
	public boolean isDirty() {
		return adminRoleCreateView.isDirty();
	}
	
	@Override
	public void setDirty(boolean dirty) {
		adminRoleCreateView.setDirty(dirty);
	}
	
	@Override
	public void setDirty() {
		setDirty(true);
	}
}
