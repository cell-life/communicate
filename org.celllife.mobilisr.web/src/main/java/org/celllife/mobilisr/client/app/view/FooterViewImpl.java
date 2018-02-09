package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.app.FooterView;

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class FooterViewImpl extends LayoutContainer implements FooterView{

	private Label footerLabel;
	private Anchor anchor;
	
	@Override
	public void createView(){
		
		setAutoWidth(true);
		setBorders(true);
		setStyleAttribute("text-align", "center");
		
		footerLabel = new Label(Messages.INSTANCE.footerLabel("v#"));
		footerLabel.setStyleName("footer");
		add(footerLabel);
		Html spacer = new Html("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;");
		spacer.setTagName("span");
		add(spacer);
		
		anchor = new Anchor("See What's New", "#");
		anchor.setStyleName("footer");
		add(anchor);
	}
	
	@Override
	public Label getFooterLabel() {
		return footerLabel;
	}
	
	@Override
	public Anchor getWhatsNewAnchor() {
		return anchor;
	}
	
	public Widget getViewWidget() {
		return this;
	}
}
