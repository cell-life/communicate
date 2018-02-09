package org.celllife.mobilisr.client.campaign;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.widget.button.Button;


public interface JustSMSCreateView extends EntityCreate<Campaign> {
	
	public Campaign getCampaign();
	
	Button getManageRecipientsButton();

	Button getCampaignListButton();

}
