package org.celllife.mobilisr.client.campaign;

import java.util.List;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.MobilisrEntity;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

public interface MessageLogView extends EntityList{
	
	public static final String FILTER_DIRECTION = "filterDirection";
	public static final String SHOW_DIRECTION_FILTER = "showDirectionFilter";

	void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter);
	
	void setTitleText(String headingText);

	Button getExportButton();

	List<FilterConfig> getFilterConfigs();

	void clearFilters();

	SimpleComboBox<String> getFilterDirectionCombo();

	void setFormObject(ViewModel<? extends MobilisrEntity> vm);

}
