package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.app.DirtyView;

public interface SettingsView extends DirtyView {

	SettingsCreateViewImpl getCreateView();

	SettingsListViewImpl getListView();

}
