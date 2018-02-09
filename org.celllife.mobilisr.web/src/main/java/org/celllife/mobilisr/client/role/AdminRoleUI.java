package org.celllife.mobilisr.client.role;

import org.celllife.mobilisr.client.app.DirtyView;

public interface AdminRoleUI extends DirtyView {

	AdminRoleCreateView getAdminRoleCreateView();
	
	AdminRoleListView getAdminRoleListView();

}
