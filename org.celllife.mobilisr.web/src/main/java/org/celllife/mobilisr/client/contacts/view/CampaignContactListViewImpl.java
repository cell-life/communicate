package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.contacts.CampaignContactListView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;

public class CampaignContactListViewImpl extends EntityListTemplateImpl 
										implements CampaignContactListView {
	
	private MyGXTButton exportBtn;
	
	@Override
	public void createView(){
		exportBtn = new MyGXTButton("csvExportContacts", "Export Recipients",
				Resources.INSTANCE.csv(), IconAlign.LEFT, ButtonScale.SMALL);
		layoutListTemplate(Messages.INSTANCE.campaignRecipients(), null, true);
	}

	
	@Override
	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter ) {
		
		List<ColumnConfig> configs = getColumnConfigs(null);

		renderEntityListGrid(store, filter, configs, null, "Search for Contact #");
		configureExportButton();
	}


	/**
	 * @return
	 */
	private List<ColumnConfig> getColumnConfigs(Campaign campaign) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		configs.add( new ColumnConfig( CampaignContact.PROP_CONTACT + "." 
				+ Contact.PROP_FIRST_NAME, Messages.INSTANCE.contactFirstName(), 50 ));
		configs.add( new ColumnConfig( CampaignContact.PROP_CONTACT + "." 
				+ Contact.PROP_LAST_NAME, Messages.INSTANCE.contactLastName(), 50 ));
		configs.add( new ColumnConfig( CampaignContact.PROP_MSISDN, 
				Messages.INSTANCE.contactMobileNumber(), 50));

		if ( (campaign != null) && !(CampaignType.FIXED.equals(campaign.getType()) ) ) {
			ColumnConfig startDate = new ColumnConfig( CampaignContact.PROP_JOINING_DATE,
					Messages.INSTANCE.startDate(), 30);
			startDate.setDateTimeFormat(DateTimeFormat.getFormat("dd-MM-yyyy"));
			configs.add(startDate);
			
			ColumnConfig endDate = new ColumnConfig( CampaignContact.PROP_END_DATE,
					Messages.INSTANCE.endDate(), 30);
			endDate.setDateTimeFormat(DateTimeFormat.getFormat("dd-MM-yyyy"));
			configs.add(endDate);
		}

		ColumnConfig progress = new ColumnConfig(CampaignContact.PROP_PROGRESS, 
				Messages.INSTANCE.campaignProgress(), 40);
		progress.setRenderer(new CampaignContactProgressRenderer());
		configs.add(progress);

		if ( (campaign != null) && (CampaignType.DAILY.equals(campaign.getType()) ) ) {
			configs.addAll(createMsgTimeColumns(campaign));
		}
		return configs;
	}

 
	/**
	 * Helper method - creates message time columns.
	 * Should only be called for recipient-specific campaigns.
	 * @param campaign 
	 * @return
	 */
	private List<ColumnConfig> createMsgTimeColumns(Campaign campaign) {
		int timesPerDay = campaign.getTimesPerDay();
		List<ColumnConfig> timeConfigs = new ArrayList<ColumnConfig>();
		for(int i = 0; i < timesPerDay; i++) {
			final int iIndex = i;
			final ColumnConfig timeConfig = new ColumnConfig( String.valueOf(i+1),
					"Message Time (" + (i+1) + " )", 45 );
			
			timeConfig.setRenderer(new GridCellRenderer<BeanModel>() {
				
				@Override
				public Object render(BeanModel model, String property,
								ColumnData config, final int rowIndex, final int colIndex,
								ListStore<BeanModel> store, final Grid<BeanModel> grid) {
					final CampaignContact campaignContact = model.getBean();
 
					List<ContactMsgTime> contactMsgTimes = campaignContact.getContactMsgTimes();
					if(iIndex < contactMsgTimes.size()) {
						DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm");
						final ContactMsgTime presentTime = contactMsgTimes.get(iIndex);
						
						return new MyGXTLabelField(fmt.format(presentTime.getMsgTime()));
					}
					else
						return null;
				}
			});
			timeConfigs.add(timeConfig);
		}
		return timeConfigs;
	}
	
	private void configureExportButton() {
		getTopToolBar().add(new FillToolItem());
		getTopToolBar().add(exportBtn);
	}

	
	@Override
	// Deliberately left blank. Required by interface.
	public Button getNewEntityButton() {
		return null;
	}

	@Override
	public Button getExportButton() {
		return exportBtn;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> vem){
		Campaign campaign = (Campaign) vem.getModelObject();
		if (campaign != null){
			setTitleLabel(campaign.getName() + ": View Recipients");
		}
		
		reconfigureGrid(getColumnConfigs(campaign));
	}
}
