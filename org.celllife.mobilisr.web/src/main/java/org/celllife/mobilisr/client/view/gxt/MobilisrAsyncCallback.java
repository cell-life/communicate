package org.celllife.mobilisr.client.view.gxt;

import org.celllife.mobilisr.client.app.presenter.StartEventHandler;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

public abstract class MobilisrAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable error) {
		BusyIndicator.hideBusyIndicator();
		if (error instanceof MobilisrException 
				|| error instanceof MobilisrRuntimeException){
			handleExpectedException(error);
		} else if (error instanceof InvocationException && error.getMessage().contains("j_spring_security_check")){
			StartEventHandler.removeWindowCloseHandler();
			Window.Location.replace(GWT.getModuleBaseURL()+"j_spring_security_logout");
		} else {
			showErrorMessage("Error communicating with the server. \n" + error.getClass().getName() + " - " + error.getMessage() +
				" \nIf this problem persists please contact support@cell-life.org");
		}
	}

	/**
	 * Method should be overridden where necessary
	 * 
	 * @param error
	 */
	protected void handleExpectedException(Throwable error) {
		showErrorMessage(error.getMessage());
	}

	protected void showErrorMessage(String message) {
		MessageBox box = new MessageBox();
		box.setTitle("Mobilisr Error");
		box.setMessage(message);
		box.setButtons(MessageBox.OK);
		box.setIcon(MessageBox.ERROR);
		box.show();
	}

}
