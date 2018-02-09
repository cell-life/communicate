package org.celllife.mobilisr.client.campaign.view;

import com.extjs.gxt.ui.client.Style.*;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.campaign.ManageRecipientsView;
import org.celllife.mobilisr.client.contacts.view.CampaignContactProgressRenderer;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;

import java.util.ArrayList;
import java.util.List;

/**
 * In this context, "Recipient" means a CampaignContact.
 *
 */
public class ManageRecipientsViewImpl extends ContentPanel implements ManageRecipientsView {

	private Label titleLabel;
	private GenericContactsGrid availContactsGridPanel;
	private GenericContactsGrid selectedRecipientsGridPanel;
	private MyGXTButton bulkAddButton;
	private MyGXTButton bulkRemoveButton;
	private MyGXTButton doneButton;
	private Campaign campaign;
	private Action addContactAction;
	private Action removeRecipientAction;
	private ContentPanel columnContainer;
	private MyGXTButton addNewContactButton;
	private boolean isDirty;
	private AnchorCellRenderer msisdnAnchor;

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void createView() {
		RowLayout rowLayout = new RowLayout(Orientation.VERTICAL);
		setIntStyleAttribute("margin", 10);
		setLayout(rowLayout);
		setFrame(true);
		setHeaderVisible(false);
		setScrollMode(Scroll.AUTOY);

		titleLabel = new Label();
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());
		titleLabel.setId("titleLabel");
		
		//Layout required for title, since margins not honoured on GXT Labels.
		LayoutContainer titleLayout = new LayoutContainer(new RowLayout());
		titleLayout.add(titleLabel);
		add(titleLayout, new RowData(1, -1, new Margins(5)) );

		bulkAddButton = new MyGXTButton("bulkAddButton",
				Messages.INSTANCE.manageRecipientsBulkAdd(), Resources.INSTANCE.listAdd(),
				IconAlign.LEFT, ButtonScale.SMALL);

		bulkRemoveButton = new MyGXTButton("bulkRemoveButton",
				Messages.INSTANCE.manageRecipientsBulkRemove(), Resources.INSTANCE.listRemove(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		addNewContactButton = new MyGXTButton("addContactButton",
				Messages.INSTANCE.contactAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		createActions();
		
		// Create columnContainer.
		ColumnLayout layout = new ColumnLayout();
		layout.setAdjustForScroll(true);
		columnContainer = new ContentPanel(layout);
		columnContainer.setBorders(false);
		columnContainer.setFrame(false);
		columnContainer.setHeaderVisible(false);
		add(columnContainer, new RowData(1, -1) );
		
		// Create and add button bar.
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setSpacing(10);
		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		doneButton = new MyGXTButton("doneButton", Messages.INSTANCE.done());
		doneButton.setSize(100, -1);
		buttonBar.add(doneButton);
		add(buttonBar, new RowData(1, -1, new Margins(5)));
	}
	
	private void createActions(){
		msisdnAnchor = new AnchorCellRenderer("recipient") {
			protected String getAnchorText(BeanModel model, String property) {
				String anchorText = super.getAnchorText(model, property);
				Boolean invalid = model.get(CampaignContact.PROP_INVALID);
				if (invalid != null && invalid) {
					anchorText += " (Invalid)";
				}
				return anchorText;
			};
		};
		 
		 addContactAction = new Action("", "Add contact to campaign",
					Resources.INSTANCE.add(), "add_contact");
		 
		 removeRecipientAction = new Action("", "Remove recipient from campaign",
					Resources.INSTANCE.delete(), "remove_recipient");
	}
	
	@Override
	public void buildWidgets(MyGXTPaginatedGridSearch<Contact> gridSearchAvailable,
			MyGXTPaginatedGridSearch<CampaignContact> gridSearchSelected) {

		// Build Contact grids.
		buildAvailableContactsGrid(gridSearchAvailable);
		buildSelectedRecipientsGrid(gridSearchSelected); 
	}

	private void buildAvailableContactsGrid(MyGXTPaginatedGridSearch<Contact> gridSearchAvailable) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig addConfig = new ColumnConfig("id", "Add", 50);
		addConfig.setFixed(true);
		ButtonGridCellRenderer addRenderer = new ButtonGridCellRenderer();
		addRenderer.addAction(addContactAction);
		addConfig.setRenderer(addRenderer);
		configs.add(addConfig);

		ColumnConfig nameConfig = new EntityIDColumnConfig(Contact.PROP_MSISDN, "Contact #", 80, "contact");
		nameConfig.setFixed(true);
		configs.add(nameConfig);

		ColumnConfig firstNameConfig = new ColumnConfig(Contact.PROP_FIRST_NAME, "First Name", 120);
		configs.add(firstNameConfig);

		ColumnConfig lastNameConfig = new ColumnConfig(Contact.PROP_LAST_NAME, "Last Name", 120);
		configs.add(lastNameConfig);
		
		RemoteStoreFilterField<BeanModel> filter = gridSearchAvailable.getFilter();
		filter.setId("searchAvailableContacts");
		
		availContactsGridPanel = new GenericContactsGrid(configs, gridSearchAvailable);
		availContactsGridPanel.setStyleAttribute("margin-right", "10px");
		availContactsGridPanel.setHeading(Messages.INSTANCE.manageRecipientsAvailable());
		availContactsGridPanel.getTopToolBar().add(new FillToolItem());
		availContactsGridPanel.getTopToolBar().add(addNewContactButton);
		availContactsGridPanel.getTopToolBar().add(bulkAddButton);
	}

	private void buildSelectedRecipientsGrid(MyGXTPaginatedGridSearch<CampaignContact> gridSearchSelected) {		

		List<ColumnConfig> configs = getRecipientGridColumns();
		
		RemoteStoreFilterField<BeanModel> filter = gridSearchSelected.getFilter();
		filter.setId("searchSelectedContacts");
		
		selectedRecipientsGridPanel = new GenericContactsGrid(configs, gridSearchSelected);
		selectedRecipientsGridPanel.setStyleAttribute("margin-left", "10px");
		selectedRecipientsGridPanel.setHeading(Messages.INSTANCE.manageRecipientsSelected());
		selectedRecipientsGridPanel.getTopToolBar().add(new FillToolItem());
		selectedRecipientsGridPanel.getTopToolBar().add(bulkRemoveButton);
		
	}

	/**
	 * @param configs
	 * @return 
	 */
	private List<ColumnConfig> getRecipientGridColumns() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig removeConfig = new ColumnConfig("id", "Remove", 52);
		removeConfig.setResizable(disabled);
		removeConfig.setFixed(true);
		ButtonGridCellRenderer removeRenderer = new ButtonGridCellRenderer();
		removeRenderer.addAction(removeRecipientAction);
		removeConfig.setRenderer(removeRenderer);
		configs.add(removeConfig);

		ColumnConfig msisdn = new ColumnConfig(CampaignContact.PROP_MSISDN, "Contact #", 80);
		msisdn.setFixed(true);
		msisdn.setRenderer(msisdnAnchor);
		configs.add(msisdn);

        ColumnConfig firstNameConfig = new ColumnConfig(Contact.PROP_FIRST_NAME, "First Name", 120);
        msisdn.setRenderer(msisdnAnchor);
        configs.add(firstNameConfig);

        ColumnConfig lastNameConfig = new ColumnConfig(Contact.PROP_LAST_NAME, "Last Name", 120);
        msisdn.setRenderer(msisdnAnchor);
        configs.add(lastNameConfig);
		
		if (campaign != null && !CampaignType.FIXED.equals(campaign.getType())) {
			ColumnConfig progressConfig = new ColumnConfig(CampaignContact.PROP_PROGRESS, "Progress", 60 );
			progressConfig.setFixed(true);
			progressConfig.setRenderer(new CampaignContactProgressRenderer());
			configs.add(progressConfig);
		}
		
		if (campaign != null && CampaignType.DAILY.equals(campaign.getType())){
			int timesPerDay = campaign.getTimesPerDay();
			final DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm");
			
			for(int i = 0; i < timesPerDay ; i++){
				final int index = i+1;
				final ColumnConfig timeConfig = new ColumnConfig( String.valueOf(index),
						"Message Time (" + index + " )", 55 );
				timeConfig.setRenderer(new GridCellRenderer<BeanModel>() {

					@Override
					public Object render(BeanModel model, String property, 
							com.extjs.gxt.ui.client.widget.grid.ColumnData config,
							final int rowIndex, final int colIndex,
							ListStore<BeanModel> store, final Grid<BeanModel> grid) {
						final CampaignContact campaignContact = model.getBean();
						List<ContactMsgTime> contactMsgTimes = campaignContact.getContactMsgTimes();
						if (contactMsgTimes != null && contactMsgTimes.size() < index) // Safety
							return null;
						final ContactMsgTime presentTime = contactMsgTimes.get(index-1);
						return fmt.format(presentTime.getMsgTime());
					}
				});
				configs.add(timeConfig);
			}
		}
		
		return configs;
	}

	@Override
	public Button getBulkAddButton() {
		return bulkAddButton;
	}

	@Override
	public Button getBulkRemoveButton() {
		return bulkRemoveButton;
	}

	@Override
	public Button getDoneButton() {
		return doneButton;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		campaign = viewEntityModel.getModelObject();
		titleLabel.setText("Campaign: " + campaign.getName());
		
		selectedRecipientsGridPanel.reconfigureGrid(getRecipientGridColumns());
		
		double availableWidth = 0.5;
		double selectedWidth = 0.5;
		if (CampaignType.FLEXI.equals(campaign.getType())){
			availableWidth -= 0.1;
			selectedWidth += 0.1;
		} else if (CampaignType.DAILY.equals(campaign.getType())){
			availableWidth -= 0.2;
			selectedWidth += 0.2;
		}
		columnContainer.removeAll();
		// Add contact grids to columnContainer, with spacing
		columnContainer.add(availContactsGridPanel, new ColumnData(availableWidth));
		columnContainer.add(selectedRecipientsGridPanel, new ColumnData(selectedWidth));
		
		setDirty(false);
	}

	@Override
	public Campaign getCampaign() {
		return campaign;
	}

	@Override
	public Action getAddContactAction() {
		return addContactAction;
	}

	@Override
	public Action getRemoveRecipientAction() {
		return removeRecipientAction;
	}
	
	@Override
	public AnchorCellRenderer getMsisdnAnchor() {
		return msisdnAnchor;
	}
	
	@Override
	public MyGXTButton getAddNewContactButton() {
		return addNewContactButton;
	}
	
	@Override
	public void enableBulkRemove(boolean enable) {
		bulkRemoveButton.setEnabled(enable);
	}
	
	@Override
	public boolean isDirty() {
		return isDirty;
	}
	
	@Override
	public void setDirty() {
		isDirty = true;
	}
	
	@Override
	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}
}
