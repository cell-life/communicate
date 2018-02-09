package org.celllife.mobilisr.client.app.view;

import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.app.HeaderView;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.MobilisrPermission;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.ui.Widget;

public class HeaderViewImpl extends LayoutContainer implements HeaderView{
	
	private VerticalPanel verticalMsgPanel;
	
	private Button programsButton;
	private Button contactsButton;
	private MyGXTButton adminButton;
	private Button profileButton;
	private Button logoutButton;
	
	private Label welcomeLabel;
	private Label balanceLabel;
	private Label statusLabel;
	
	@Override
	public void createView(){
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setBorders(true);
		setHeight("10%");
		
		programsButton = new MyGXTButton("programsButton", "Home" ,Resources.INSTANCE.home() , IconAlign.LEFT , ButtonScale.LARGE);
		contactsButton = new MyGXTButton("contactsButton", "Contacts", Resources.INSTANCE.addressbook() , IconAlign.LEFT , ButtonScale.LARGE);
		adminButton = new MyGXTButton("adminButton", "Admin", Resources.INSTANCE.admin() , IconAlign.LEFT , ButtonScale.LARGE);
		profileButton = new MyGXTButton("profileButton" , "My Profile" );
		logoutButton = new MyGXTButton("logoutButton", "Logout");
		
		welcomeLabel = new Label();
		balanceLabel = new Label();
		statusLabel = new Label();
		
		adminButton.addButtonPermission(MobilisrPermission.VIEW_ADMIN_CONSOLE);
		adminButton.setVisible(false);
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(5);
		horizontalPanel.setHeight("100%");
		horizontalPanel.setHorizontalAlign(HorizontalAlignment.LEFT);
		horizontalPanel.setIntStyleAttribute("padding", 5);
		
		horizontalPanel.add(programsButton);
		horizontalPanel.add(contactsButton);
		horizontalPanel.add(adminButton);
		
		verticalMsgPanel = new VerticalPanel();
		verticalMsgPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
		verticalMsgPanel.setVerticalAlign(VerticalAlignment.MIDDLE);
		verticalMsgPanel.setSpacing(5);
		welcomeLabel.setStyleAttribute("font-size","13pt");
		
		statusLabel.setStyleAttribute("font-weight","bold");
		statusLabel.setStyleAttribute("text-align","center");
		
		welcomeLabel.setId("welcomeLabel");
		balanceLabel.setId("balanceLabel");
		statusLabel.setId("statusLabel");
		
		verticalMsgPanel.add(welcomeLabel);
		verticalMsgPanel.add(balanceLabel);
		verticalMsgPanel.add(statusLabel);
		
		VBoxLayout layout = new VBoxLayout();
		layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		layout.setPadding(new Padding(5));
		LayoutContainer layoutContainer = new LayoutContainer(layout);
		layoutContainer.add(profileButton, new VBoxLayoutData(new Margins(5,0,5,0)));
		layoutContainer.add(logoutButton, new VBoxLayoutData(new Margins(0)));
		
		add(horizontalPanel,new RowData( .4, -1));
		add(verticalMsgPanel, new RowData( .54, -1));
		// must specify height when using VBoxLayout
		add(layoutContainer, new RowData( .06, 60));		
		
		layout();
	}

	public MyGXTButton getAdminButton() {
		return adminButton;
	}

	public Button getContactsButton() {
		return contactsButton;
	}

	public Button getProgramsButton() {
		return programsButton;
	}

	public Button getLogoutButton() {
		return logoutButton;
	}

	public Button getProfileButton() {
		return profileButton;
	}

	public Label getWelcomeLabel() {
		return welcomeLabel;
	}
	
	public Widget getViewWidget() {
		return this;
	}

	public Widget getMyWidget() {
		return this;
	}

	@Override
	public Label getBalanceLabel() {
		return balanceLabel;
	}
	
	@Override
	public Label getStatusLabel() {
		return statusLabel;
	}
	
	@Override
	public Button getProgramButton(){
		return programsButton;
	}

	/* (non-Javadoc)
	 * @see org.celllife.mobilisr.client.app.HeaderView#adjustHeight()
	 */
	@Override
	public void adjustHeight() {
		if ( statusLabel.getText().isEmpty() )
			setHeight("10%");
		else
			setHeight(verticalMsgPanel.getHeight());
	}
	
}
