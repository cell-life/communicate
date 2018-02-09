package org.celllife.mobilisr.client.app.presenter;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class StartEventHandler extends BaseEventHandler<MobilisrEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	private static HandlerRegistration windowClosingHandler;

	@Override
	public void bind() {
		adminService.getNumberInfoList(new MobilisrAsyncCallback<List<NumberInfo>>() {
					@Override
					public void onSuccess(List<NumberInfo> result) {
						if (result == null || result.isEmpty()){
							MessageBoxWithIds.alert("Warning: number validation not configured", 
									"There are no number validators configured. Please" +
									" add some to the database and refresh the browser window.", null);
						}
						
						ArrayList<MsisdnRule> list = new ArrayList<MsisdnRule>(result.size());
						for (NumberInfo numberInfo : result) {
							list.add(numberInfo.getMsisdnRule());
						}
						ValidatorFactory.setNumberInfoList(list);
					}
				});
	}

	public void onStart() {
		addWindowCloseHandler();
	}

	public void onShowWhatsNew() {
		com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
		w.setHeading("What's new");
		w.setModal(true);
		w.setSize(850, 500);
		w.setMaximizable(true);
		w.setToolTip("What's new in this version");
		w.setUrl("whats-new.html");
		w.show();
	}

	public static void addWindowCloseHandler() {
		if (windowClosingHandler == null){
			windowClosingHandler = Window.addWindowClosingHandler(new Window.ClosingHandler() {
				public void onWindowClosing(Window.ClosingEvent event) {
					event.setMessage("Communicate");
				}
			});
		}
	}
	
	public static void removeWindowCloseHandler(){
		if (windowClosingHandler != null) {
			windowClosingHandler.removeHandler();
		}
	}
}
