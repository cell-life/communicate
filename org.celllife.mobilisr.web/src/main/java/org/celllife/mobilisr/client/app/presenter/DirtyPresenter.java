package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.app.DirtyView;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.history.NavigationConfirmationInterface;
import com.mvp4g.client.history.NavigationEventCommand;

@Presenter(view=Class.class)
public class DirtyPresenter<V extends DirtyView,E extends EventBus> extends MobilisrBasePresenter<V,E> implements NavigationConfirmationInterface {
	
	@Override
	public void confirm(final NavigationEventCommand event) {
		if (getView().isDirty()){
			MessageBoxWithIds.confirm(Messages.INSTANCE.dialogUnsavedChangesTitle(), 
					Messages.INSTANCE.dialogUnsavedChangesMessage(), new Listener<MessageBoxEvent>() {
				@Override
				public void handleEvent(MessageBoxEvent be) {
					if (be.getButtonClicked().getItemId()
							.equals(Dialog.YES)) {
						event.fireEvent();
					}
				}
			});
		} else {
			event.fireEvent();
		}
	}
}
