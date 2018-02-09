package org.celllife.mobilisr.client.campaign.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.campaign.CreateCampaignBasicsView;
import org.celllife.mobilisr.client.campaign.presenter.WizardPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.*;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class CreateCampaignBasicsViewImpl extends EntityCreateTemplateImpl<Campaign> implements CreateCampaignBasicsView, WizardStep {

	private static final Logger log = Logger.getLogger(CreateCampaignBasicsViewImpl.class.getName());
	
	private MyGXTTextField campNameField;
	private MyGXTTextArea campDescripField;
    private MyGXTTextField linkedCampaignId;
    private LabelField linkedCampaignLabel = new LabelField("*** LINKED CAMPAIGNS ARE FOR ADVANCED USERS ONLY ***");

	private ButtonBar buttonBar = new ButtonBar();
	private MyGXTButton btnBack;
	private MyGXTButton btnSave;
	private MyGXTButton btnCancel;
	private MyGXTButton btnNext;
	private WizardStepper parent;
	private Campaign campaign;
	
	private ComboBox<BeanModel> orgComboBox;
    private ComboBox<BeanModel> linkedCampaignComboBox;

    private CampaignServiceAsync  crudCampaignServiceAsync;
	
	public CreateCampaignBasicsViewImpl() {
	}

	public CreateCampaignBasicsViewImpl(WizardStepper parent) {
		this.parent = parent;
		createView();
	}
	
	@Override
	public void createView() {
		super.createView();
		
		orgComboBox = new MyGXTComboBox<BeanModel>("Select an Organisation",
				Organization.PROP_NAME, true);

        linkedCampaignComboBox = new MyGXTComboBox<BeanModel>("Select the campaign to link to", Campaign.PROP_NAME, true);
		campNameField = new MyGXTTextField(
				"* Campaign Name", Campaign.PROP_NAME, false, "Enter a descriptive campaign name");
		campDescripField = new MyGXTTextArea(
				"Campaign Description", Campaign.PROP_DESCRIPTION, true,
				"Please give a brief description of this campaign");

        linkedCampaignId = new MyGXTTextField("Linked Campaign Id", Campaign.PROP_LINKED_CAMPAIGN_ID, true, "");
        linkedCampaignId.setEnabled(false);
        linkedCampaignId.setAllowBlank(true);
		
		btnBack = new MyGXTButton("backButton", Messages.INSTANCE.wizardBack());
		btnSave = new MyGXTButton("saveButton", Messages.INSTANCE.save());
		btnCancel = new MyGXTButton("cancelButton", Messages.INSTANCE.cancel());
		btnNext = new MyGXTButton("nextButton", Messages.INSTANCE.wizardNext());
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(btnBack, false);
		formButtons.put(btnSave, true);
		formButtons.put(btnCancel, false);
		formButtons.put(btnNext, true);

		layoutWizardTemplate("Create New Campaign: Step 2 of 4");
		
		createContainer(formElementContainer);
		
		addAndConfigFormButtons(formButtons, false);
		createFormBinding(formPanel, true);
	}

	public void layoutWizardTemplate(String titleLabelText) {
		
		setIntStyleAttribute("margin", 0);
		setScrollMode(Scroll.AUTOY);
		setLayout(new RowLayout(Orientation.VERTICAL));
		setHeight("100%");

		titleLabel.setText(titleLabelText);
		titleLabel.setId("wizardStepTitle");
		titleLabel.getElement().setId("wizardStepTitle");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());

		formPanel.setPadding(5);
		formPanel.setBorders(true);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setLayout(new RowLayout());
		formPanel.setAutoWidth(true);
		formPanel.setAutoHeight(true);
		formPanel.setScrollMode(Scroll.AUTOY);

		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(160);
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoHeight(true);

		formCenterContainer.setAutoWidth(true);
		formCenterContainer.setHeight("60%");
		formCenterContainer.setLayout(new CenterLayout());
		formCenterContainer.add(formElementContainer);

		formPanel.add(formCenterContainer, new RowData(1, -1, new Margins(10)));

		add(titleLabel, new RowData(1, -1, new Margins(0, 10, 10, 10)));
		add(msgLabel, new RowData(1, -1, new Margins(10)));
		add(formPanel, new RowData(1, 1, new Margins(10)));
	}

	private void createContainer(LayoutContainer layoutContainer) {
		log.finer("Create basic view");
		
		orgComboBox.setName(Campaign.PROP_ORGANIZATION);
		orgComboBox.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userOrganisation());
		orgComboBox.setAllowBlank(false);
		orgComboBox.setStore(new ListStore<BeanModel>());

        linkedCampaignComboBox.setName("CampaignName");
        linkedCampaignComboBox.setFieldLabel(Messages.INSTANCE.linkedCampaignLabelName());
        linkedCampaignComboBox.setAllowBlank(true);
        linkedCampaignComboBox.setStore(new ListStore<BeanModel>());

        linkedCampaignComboBox.addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<BeanModel> beanModelSelectionChangedEvent) {

                crudCampaignServiceAsync = (CampaignServiceAsync ) parent.getItem(WizardPresenter.CAMPAIGN_SERVICE_ASYNC);
                btnSave.setEnabled(false);
                btnNext.setEnabled(false);
                crudCampaignServiceAsync.getCampaignIdForName(linkedCampaignComboBox.getRawValue(), new MobilisrAsyncCallback<Long>() {

                    @Override
                    public void onSuccess(Long result) {

                        if ((campaign.getId() != null) && (result != null) && (result == campaign.getId())) {
                            MessageBoxWithIds.alert("Linked Campaign Error", "You cannot link a campaign to itself. Please select another campaign.", null);
                            //linkedCampaignComboBox.clear();
                            btnSave.setEnabled(true);
                            btnNext.setEnabled(true);
                        }
                        else {
                            linkedCampaignId.setValue(result.toString());
                            campaign.setLinkedCampaignId(result);
                            btnSave.setEnabled(true);
                            btnNext.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        btnSave.setEnabled(true);
                        btnNext.setEnabled(true);
                        super.onFailure(error);
                    }
                });
            }
        });

		campNameField.setId("campaignName");
		campNameField.setMinLength(1);
		campNameField.setMaxLength(100);

		campDescripField.setId("campaignDescription");
		campDescripField.setMaxLength(255);
		
		FormData formData = new FormData();
		formData.setMargins(new Margins(10));
		layoutContainer.add(campNameField, formData);
		layoutContainer.add(orgComboBox, formData);
		layoutContainer.add(campDescripField, formData);
        layoutContainer.add(linkedCampaignLabel);
        layoutContainer.add(linkedCampaignComboBox, formData);
        layoutContainer.add(linkedCampaignId,formData);

		buttonBar.setAlignment(HorizontalAlignment.CENTER);

		btnSave.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});

		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		});

		btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				goNext();
			}
		});
		btnBack.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				goPrevious();
			}
		});

		buttonBar.add(btnBack);
		buttonBar.add(btnSave);
		buttonBar.add(btnCancel);
		buttonBar.add(btnNext);
		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		layoutContainer.add(buttonBar, formData);
	}

	@Override
	public void cancel() {
		parent.cancel();
	}

	@Override
	public void goNext() {
		checkCampaignName(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Void arg0) {
				parent.goNext(getFormObject());
			}
		});			
	}

	private void checkCampaignName(final AsyncCallback<Void> callback) {
		log.finer("Checking campaign name: " + campNameField.getValue());
		
		BusyIndicator.showBusyIndicator("Checking campaign name");
		crudCampaignServiceAsync.getCampaignIdForName(campNameField.getValue(),new MobilisrAsyncCallback<Long>() {
			@Override
			public void onSuccess(Long id) {
				BusyIndicator.hideBusyIndicator();
				boolean inUseByAnotherCampaign = (id != null)
						&& (!campaign.isPersisted() || !id
								.equals(campaign.getId()));
				if (inUseByAnotherCampaign){
					MessageBoxWithIds.alert("Campaign name in use", "Campaign name is in use. Please use another name", null);
					callback.onFailure(null);
				}else{
					callback.onSuccess(null);
				}
			}

			@Override
			public void onFailure(Throwable error) {
				super.onFailure(error);
				callback.onFailure(null);
			}
		});
	}

	@Override
	public void goPrevious() {
		parent.goPrevious(getFormObject());
	}

	@Override
	public void save() {
		if (isDirty()){
			checkCampaignName(new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable arg0) {
				}

				@Override
				public void onSuccess(Void arg0) {
					parent.save(false);
				}
			});	
		}
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public ViewModel<Campaign> getFormObject() {
		log.finest("Get form object");
		
		ViewModel<Campaign> viewEntityModel = super.getFormObject();
		campaign = (Campaign) viewEntityModel.getModelObject();
		viewEntityModel.setModelObject(campaign);

		return viewEntityModel;
	}

	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		log.finer("Set form object");
		@SuppressWarnings("unchecked")
		ListStore<BeanModel> orgStore = (ListStore<BeanModel>) parent.getItem(WizardPresenter.ORGANISATION_STORE);
		orgComboBox.setStore(orgStore);
		
		super.setFormObject(viewEntityModel);
		
		campaign = (Campaign)viewEntityModel.getModelObject();

		crudCampaignServiceAsync = (CampaignServiceAsync ) parent.getItem(WizardPresenter.CAMPAIGN_SERVICE_ASYNC);
		selectOrgCampaign(campaign);
		orgComboBox.setEnabled(!campaign.isPersisted());
		orgComboBox.setVisible(UserContext.hasPermission(MobilisrPermission.CAMPAIGNS_ADMIN_MANAGE));
		orgComboBox.setAllowBlank(!orgComboBox.isVisible());  // if not visible then it must allow blank

        ListStore<BeanModel> campaignStore = (ListStore<BeanModel>) parent.getItem(WizardPresenter.CAMPAIGN_STORE);
        linkedCampaignComboBox.setStore(campaignStore);

        linkedCampaignComboBox.setAllowBlank(true);
		
		btnSave.setVisible(campaign.isPersisted());
		
		updateTitle();

	}
	
	private void updateTitle() {
		
		String type = "";
		
		if (campaign.getType()==CampaignType.DAILY){
			type = "Recipient-Specific";
		}else if (campaign.getType() == CampaignType.FLEXI){
			type = "Generic";
		}else if (campaign.getType() == CampaignType.FIXED){
			type = "Fixed"; 
		}
		
		String title = ( campaign.isPersisted() ? "Editing Campaign '" + campaign.getName() +"'" : "Create New "+ type+ " Campaign" ) + ": Step 2 of 4";
		titleLabel.setText(title);
	}

	
	private void selectOrgCampaign(Campaign camp) {
		Organization org = camp.getOrganization();
		if (org != null) {
			campaign.setOrganization(org);
		}
	}

	@Override
	public void resetForm() {
		log.finer("Resetting form");
		orgComboBox.clear();
	}
}
