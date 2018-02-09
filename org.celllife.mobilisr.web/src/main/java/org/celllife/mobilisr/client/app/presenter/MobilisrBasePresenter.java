package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.app.BasicView;
import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.model.ViewModel;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.LazyPresenter;

@Presenter(view=Class.class)
public class MobilisrBasePresenter<V extends BasicView, E extends EventBus> extends LazyPresenter<V, E> implements PresenterStateAware {
	
	private boolean isAdminView = false;
	public static final String ADMIN_VIEW = "isViewAdmin";
	
	/**
	 * Determines if the current view is an admin view or now
	 * @param viewEntityModel
	 * @return
	 */
	public boolean isAdminView(ViewModel<?> viewModel) {
		isAdminView = false;
		if (viewModel != null) {
			if (viewModel.isPropertyTrue(ADMIN_VIEW)) {
				isAdminView = true;
			}
		}
		return isAdminView;
	}
	
	/**
	 * Returns the current status of isAdminView
	 * @return
	 */
	public boolean isAdminView() {
		return isAdminView;
	}

	@Override
	public BasicView getPresenterView() {
		return (BasicView) getView();
	}
}
