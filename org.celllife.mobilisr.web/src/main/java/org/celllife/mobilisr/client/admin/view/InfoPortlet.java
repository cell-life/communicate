package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

public class InfoPortlet implements MobilisrPortlet {

	private Label unsentMail;
	private Label lostMessages;
	private final AdminServiceAsync adminService;
	private MyGXTButton sendMailNow;

	public InfoPortlet(AdminServiceAsync adminService) {
		this.adminService = adminService;
	}

	@Override
	public Portlet getPortlet() {
		Portlet portlet = new Portlet();
		portlet.setHeading("Mobilisr Info");
		portlet.setLayout(new FillLayout());
		portlet.setPinned(false);
		portlet.setSize(250, 300);

		portlet.getHeader().addTool(
				new ToolButton("x-tool-refresh",
						new SelectionListener<IconButtonEvent>() {
							@Override
							public void componentSelected(IconButtonEvent ce) {
								refreshData();
							}
						}));

		portlet.add(getInfo());
		refreshData();
		return portlet;
	}

	private Widget getInfo() {
		LayoutContainer lc = new LayoutContainer();
		lc.setLayout(new RowLayout(Orientation.VERTICAL));
		lc.setBorders(true);

		LayoutContainer c1 = new LayoutContainer();
		c1.setLayout(new HBoxLayout());

		Margins m = new Margins(5);
		c1.add(new Label("Mail queue size:"), new HBoxLayoutData(m));
		unsentMail = new Label("0000");
		c1.add(unsentMail, new HBoxLayoutData(m));
		sendMailNow = new MyGXTButton("sendMailNow","",Resources.INSTANCE.mailSend(),
				IconAlign.LEFT, ButtonScale.SMALL);
		sendMailNow.setToolTip("Send mail now");
		sendMailNow.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						adminService.sendMailNow(new MobilisrAsyncCallback<Void>() {
							@Override
							public void onSuccess(Void arg0) {
								MessageBoxWithIds.info("Mail job started",
										"Mail job started",null);
							}
						});
					}
				});
		c1.add(sendMailNow, new HBoxLayoutData(m));

		lc.add(c1, new RowData(1, -1));

		// Lost messages count
		LayoutContainer c2 = new LayoutContainer();
		c2.setLayout(new HBoxLayout());
		c2.add(new Label("Lost messages:"), new HBoxLayoutData(m));
		lostMessages = new Label("0000");
		c2.add(lostMessages, new HBoxLayoutData(m));
		lc.add(c2, new RowData(1, -1));

		return lc;
	}

	protected void refreshData() {
		adminService.getMailQueueCount(new MobilisrAsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer count) {
				unsentMail.setText(count.toString());
				sendMailNow.setEnabled(count > 0);
			}
		});

		adminService.getLostMessagesCount(new MobilisrAsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer count) {
				lostMessages.setText(count.toString());
			}
		});
	}
}
