package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.campaign.view.CampaignStatusRenderer;
import org.celllife.mobilisr.client.contacts.presenter.ContactCampaignStatusPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ContactCampaignStatusViewImpl  extends EntityListTemplateImpl implements ContactCampaignStatusView {

	private Contact contact;
	private Label nameLabel;
	private Label surnameLabel;
	private Label msisdnLabel;
	
	@Override
	public void createView() {
		nameLabel = new Label();
		surnameLabel = new Label();
		msisdnLabel = new Label();
		
		layoutListTemplate("Campaigns for contact", null, true);
	}
	
	@Override
	protected void addMessageLabel() {
		add(nameLabel, new RowData(1, -1, new Margins(20, 10, 0, 20)));
		add(surnameLabel, new RowData(1, -1, new Margins(20, 10, 0, 20)));
		add(msisdnLabel, new RowData(1, -1, new Margins(20, 10, 0, 20)));
				
		super.addMessageLabel();
	}
	
	private void setContactDetails(Contact contact){
		nameLabel.setText("Name: " + ((contact.getFirstName()==null)? "" : contact.getFirstName()));
		surnameLabel.setText("Surname: " + ((contact.getLastName()==null)? "" : contact.getLastName()));
		msisdnLabel.setText("Mobile Number: " + ((contact.getMsisdn()==null)? "" : contact.getMsisdn()));
	}

	@Override
	public ColumnConfig buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter,final ContactCampaignStatusPresenter presenter) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig campName = new ColumnConfig(CampaignContact.PROP_MSISDN, "Campaign Name", 70);
		campName.setRenderer(new GridCellRenderer<BeanModel>(){

			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {

				CampaignContact cc = model.getBean();
				Campaign c = cc.getCampaign();
				return c.getName();
			}});
		configs.add(campName);
		ColumnConfig campStatus = new ColumnConfig(Campaign.PROP_STATUS, "Status", 30);
		campStatus.setRenderer(new CampaignStatusRenderer(){
			@Override
			protected Campaign getCampaign(BeanModel model) {
				CampaignContact cc = model.getBean();
				return cc.getCampaign();
			}
		});
		configs.add(campStatus);
		
		ColumnConfig progress = new ColumnConfig(CampaignContact.PROP_PROGRESS, "Progress", 30);
		progress.setRenderer(new CampaignContactProgressRenderer());
		configs.add(progress);
		
		
		ColumnConfig date = new ColumnConfig(CampaignContact.PROP_JOINING_DATE, "Date Started", 50);
		date.setRenderer(new GridCellRenderer<BeanModel>(){

			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {

				CampaignContact cc = model.getBean();
				
				DateTimeFormat dtf = DateTimeFormat.getFormat(" yyyy-MM-dd ");
				Date joiningDate = cc.getJoiningDate();
				
				return (joiningDate==null)? "Unknown" : dtf.format(joiningDate);
			}});
		configs.add(date);
		
		renderEntityListGrid(store, filter, configs, "", "Search for Campaign");
		
		return new ColumnConfig();
	}

	@Override
	public Button getNewEntityButton() {
		return null;
	}
	
	@Override
	public void setFormObject(ViewModel<Contact> viewEntityModel) {
		this.contact = viewEntityModel.getModelObject();
		setContactDetails(contact);
	}
}
