package org.celllife.mobilisr.client.admin;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public interface ChannelCreateView extends EntityCreate<ChannelViewModel> {

	void enableShortcode(boolean enable);

	void setChannelConfigStore(ListStore<BeanModel> store);

	ComboBox<BeanModel> getChannelHandlerCombo();

	void setChannelHandlerStore(ListStore<BeanModel> store);

	void enableConfigSelection(boolean enable);

}
