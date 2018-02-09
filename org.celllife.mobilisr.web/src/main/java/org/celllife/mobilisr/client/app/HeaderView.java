package org.celllife.mobilisr.client.app;

import org.celllife.mobilisr.client.view.gxt.MyGXTButton;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface HeaderView extends BasicView{

	public Button getProgramsButton();

	public Button getContactsButton();

	public MyGXTButton getAdminButton();

	//public Button getHelpButton();	
	
	public Button getProfileButton();

	public Button getLogoutButton();
	
	public Label getWelcomeLabel();

	public Label getBalanceLabel();

	Button getProgramButton();

	public Label getStatusLabel();

	/**
	 * Workaround for statusLabel sometimes hidden on smaller screens.
	 * TODO: this can probably be removed once the header layout is changed.
	 */
	public void adjustHeight();
}
