package org.celllife.mobilisr.client.app;

import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;

public interface EntityCreate<T> extends DirtyView {

	MyGXTButton getFormSubmitButton();
	
	MyGXTButton getFormCancelButton();
	
	void setErrorMessage(String errorMsg);
	
	ViewModel<T> getFormObject();

	void setFormObject(ViewModel<T> viewModel);

}
