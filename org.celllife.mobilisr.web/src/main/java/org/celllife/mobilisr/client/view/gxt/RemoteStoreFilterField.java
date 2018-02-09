package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public abstract class RemoteStoreFilterField<M extends ModelData> extends
		StoreFilterField<M> {
	public static final String PARM_FIELDS = "fields";
	public static final String PARM_QUERY = "query";

	/* disable the locally default behavior */

	public RemoteStoreFilterField() {
		super();
		setValidationDelay(1000);
	}

	@Override
	public void bind(Store<M> store) {
		/* do nothing here - to avoid calling locally the grid filter */
	}

	@Override
	protected boolean doSelect(Store<M> store, M parent, M record,
			String property, String filter) {
		/* do nothing here - to avoid calling locally the grid filter */
		return true;
	}

	@Override
	protected void onTriggerClick(ComponentEvent ce) {
		setValue(null);
		handleCancelFilter();
	}

	@Override
	protected void onFilter() {
		focus();

		String filter = getRawValue();
		if (filter != null && !filter.isEmpty()) {
			handleOnFilter(filter);
		}
	}

	/** need to perform cancellation of the remote filter */
	protected abstract void handleCancelFilter();

	/** need to perform the remote filtering */
	protected abstract void handleOnFilter(String filter);
}
