package org.celllife.mobilisr.client.campaign;

import org.celllife.mobilisr.client.app.DirtyView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.widget.button.Button;

public interface ManageRecipientsView extends DirtyView {

	void setFormObject(ViewModel<Campaign> viewEntityModel);

	Campaign getCampaign();

	void buildWidgets(MyGXTPaginatedGridSearch<Contact> gridSearchAvailable,
			MyGXTPaginatedGridSearch<CampaignContact> gridSearchSelected);

	Button getBulkAddButton();

	Button getBulkRemoveButton();

	Button getDoneButton();

	Action getAddContactAction();

	Action getRemoveRecipientAction();

	MyGXTButton getAddNewContactButton();

	AnchorCellRenderer getMsisdnAnchor();

	void enableBulkRemove(boolean enable);

}
