package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.app.DirtyView;
import org.celllife.mobilisr.client.campaign.WizardView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;

public class WizardViewImpl extends EntityCreateTemplateImpl<Campaign> implements WizardView, WizardStepper {

	private static final Logger log = Logger.getLogger(WizardViewImpl.class.getName());
	
	private MyGXTButton btnImportCampaign = new MyGXTButton("Import Campaign");
	private BorderLayout borderLayout = new BorderLayout();
	private BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 100);
	private BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
	private List<MyGXTButton> lstButtons = new ArrayList<MyGXTButton>();
	private List<WizardStep> lstWizardSteps = new ArrayList<WizardStep>(); 
	private Map<String, Object> mapWarehouse = new HashMap<String, Object>();
	private int stepSelected = 0;
	private ListStore<BeanModel> organisationStore;
    private ListStore<BeanModel> campaignStore;
	
	private LayoutContainer buttonContainer = new LayoutContainer();
	private VBoxLayout westLayout = new VBoxLayout();
	private VBoxLayoutData buttonLayout = new VBoxLayoutData(new Margins(10, 0, 10,	0));
	
	private Margins marginsWest = new Margins(0, 0 , 0 , 0);
	private Margins marginsCenter = new Margins(0, 0, 0, 5);
	private boolean editingNotCreating = false;
	//private Campaign campaign;

	@Override
	public void createView() {	
		super.createView();
		log.finer("Create view");

		layoutCreateTemplate("Campaign Wizard", true);
		
		btnImportCampaign.disable();

		generateRecipientSpecificWizardSteps(true);
		
		for (int i = 0; i < lstWizardSteps.size(); i++){
			log.finer("Create step button: " + i+1);
			
			MyGXTButton btnStep = new MyGXTButton("wizardStep-"+(i+1),"Step " + (i+1));
			btnStep.setData("wizardStep", i+1);
			if(i != 0){
				btnStep.disable();
			}
			
			btnStep.addSelectionListener(new SelectionListener<ButtonEvent>() {
				
							@Override
							public void componentSelected(ButtonEvent ce) {
								ViewModel<Campaign> vem = getStep(stepSelected).getFormObject();
								Integer step = ce.getButton().getData("wizardStep");
								stepSelected = step;
								displayWizardWidget(stepSelected-1, vem);
							}
						});
			
			lstButtons.add(btnStep);
		}
		
		createLeftButtonView();
		
		westData.setMargins(marginsWest);
		westData.setSplit(false);
		centerData.setMargins(marginsCenter);
	}

	private void generateRecipientSpecificWizardSteps(boolean createNotEdit) {
		log.finer("Create recipient specific steps. isCreate=" + createNotEdit);
		if (createNotEdit){
			lstWizardSteps.clear();
			lstWizardSteps.add(new CreateCampaignTypesViewImpl(this));
			lstWizardSteps.add(new CreateCampaignBasicsViewImpl(this));
			lstWizardSteps.add(new RecipientSpecific3ViewImpl(this));
			lstWizardSteps.add(new RecipientSpecific4ViewImpl(this) );
			
		}else{
			if (lstWizardSteps.get(2) instanceof Generic3ViewImpl){
				lstWizardSteps.remove(2);
				lstWizardSteps.add(2,new RecipientSpecific3ViewImpl(this));
				
			}
		}
	}
	
	private void generateGenericWizardSteps(boolean createNotEdit) {
		log.finer("Create generic steps. isCreate=" + createNotEdit);
		if (createNotEdit){
			lstWizardSteps.clear();
			lstWizardSteps.add(new CreateCampaignTypesViewImpl(this));
			lstWizardSteps.add(new CreateCampaignBasicsViewImpl(this));
			lstWizardSteps.add(new Generic3ViewImpl(this));
			lstWizardSteps.add(new RecipientSpecific4ViewImpl(this) );
		}
		else{ 	
			if (lstWizardSteps.get(2) instanceof RecipientSpecific3ViewImpl){
				lstWizardSteps.remove(2);
				lstWizardSteps.add(2,new Generic3ViewImpl(this));
			}
		}
	}
	
	private void createLeftButtonView(){
		log.finer("Create left button view");
		
		buttonContainer.setBorders(true);
		buttonContainer.setStyleName("container");
		buttonContainer.setStyleAttribute("background-color", "white");
		
		westLayout.setPadding(new Padding(5));
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		buttonContainer.setLayout(westLayout);
		
		buttonContainer.add(btnImportCampaign, buttonLayout);
		for (MyGXTButton button : lstButtons) {
			buttonContainer.add(button, buttonLayout);
		}		
	}
	
	public void layoutCreateTemplate(String titleLabelText, boolean isAutoBind) {
		log.finer("Create layout");
		
		setLayout(new RowLayout(Orientation.VERTICAL));
		setAutoWidth(true);
		setIntStyleAttribute("margin", 5);
		setStyleAttribute("background-color", "white");
		
		titleLabel.setText(titleLabelText);
		titleLabel.setId("titleLabelText");
		titleLabel.getElement().setId("titleLabelText");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());
				
		formPanel.setPadding(0);
		formPanel.setBorders(false);
		formPanel.setBodyBorder(true);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setLayout(new RowLayout());
		formPanel.setWidth("100%");
		formPanel.setScrollMode(Scroll.AUTOY);
		formPanel.setHeaderVisible(false);
		formPanel.setStyleName("container");
		formPanel.setFrame(false);
		
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoWidth(true);
		formElementContainer.setHeight("100%");
		formElementContainer.setIntStyleAttribute("margin", 0);
		formPanel.add(formElementContainer, new RowData(1, 1, new Margins(5)));

		createFormBinding(formPanel, isAutoBind);
		
		LayoutContainer formLayoutContainer = new LayoutContainer();
		formLayoutContainer.setLayout(borderLayout);
		formLayoutContainer.setIntStyleAttribute("margin", 0);
		formLayoutContainer.setScrollMode(Scroll.AUTOY);
		formLayoutContainer.setStyleAttribute("background-color", "white");
		formLayoutContainer.setAutoWidth(true);
		
		formLayoutContainer.add(buttonContainer, westData);
		formLayoutContainer.add(formPanel, centerData);
		
		add(titleLabel, new RowData(1,-1, new Margins(10)));
		add(formLayoutContainer, new RowData(1,1,new Margins(10)));
	}
	
	private void displayWizard(int stepNumber){
		log.finer("Display wizard: " + stepNumber);
		
		formElementContainer.removeAll();
		formElementContainer.add(lstWizardSteps.get(stepNumber).getViewWidget());
		formElementContainer.layout();
		layout();
	}
	
	@Override
	public void setFormObject(ViewModel<Campaign> vem){
		log.finer("Set form object");
		
		setEditingMode(vem.isModeUpdate());
		
		resetForms();
		stepSelected = 0;
		displayWizardWidget(stepSelected, vem);
	}
	
	public void setEditingMode(boolean editingNotCreating) {
		log.finer("Set editing mode: isEdit=" + editingNotCreating);
		
		this.editingNotCreating  = editingNotCreating;
		if (editingNotCreating){
			for (MyGXTButton button : lstButtons) {
				button.enable();
			}
		}
	}

	@Override
	public void goNext(ViewModel<Campaign> vem){
		displayWizardWidget(stepSelected+1, vem);
	}
	
	@Override
	public void goPrevious(ViewModel<Campaign> vem){
		displayWizardWidget(stepSelected-1, vem );
	}
	
	public void save(final boolean goNext){
		log.finer("Save. Go to next: " + goNext);
		ViewModel<Campaign> vem =  getStep(stepSelected).getFormObject();
		
		MobilisrEntityEvent event = new MobilisrEntityEvent(MobilisrEvents.SAVE, vem.getModelObject(), goNext);
		fireEvent(MobilisrEvents.SAVE, event);
	}
	
	@Override
	public void cancel(){
		log.finer("do cancel");
//		resetForms();
		MobilisrEntityEvent event = new MobilisrEntityEvent(MobilisrEvents.CANCEL, null);
		fireEvent(MobilisrEvents.CANCEL, event);
	}
	
	@Override
	public int getStepSelected(){
		return stepSelected;
	}
	
	@Override
	public List<MyGXTButton> getSteps(){
		return lstButtons;
	}
	
	public WizardStep getStep(int step){
		return lstWizardSteps.get(step);
	}
	
	private void displayWizardWidget(int stepNumber, ViewModel<Campaign> vem){
		log.finer("Display wizard widget: " + stepNumber);
		
		if (vem != null){
			setDirty(vem.isDirty());
		}
				
		if (vem != null && stepNumber == 1) {
			Campaign c = vem.getModelObject();
			log.finer("setting form object for step: " + stepNumber);
			if (c.getType() !=null){
				log.finer("Campaign type: " + c.getType());
				if (c.getType() == CampaignType.FLEXI){
					generateGenericWizardSteps(false);
				}else if (c.getType() == CampaignType.DAILY){
					generateRecipientSpecificWizardSteps(false);
				}	
			}
		}

		switch(stepNumber){
			case 0:
			case 1:
			case 2:
			case 3:
					WizardStep s = getStep(stepNumber);
					stepSelected =  stepNumber;
					displayWizard(stepNumber);
					if (vem != null) {
						log.finer("setting form object for step " + stepNumber);
						s.setFormObject(vem);
					}
					lstButtons.get(stepNumber).enable();
					break;
			case 4:
					//Last step
					doFinish(vem);
					break;
			default:
					break;
			
		}
	}
	
	// A way of passing objects from the presenter to the WizardSteps
	public void putItem(String key, Object target){
		mapWarehouse.put(key,target);
	}
	
	public Object getItem(String key){
		return mapWarehouse.get(key);
	}
	
	public void setOrganizationStore(ListStore<BeanModel> store) {
		organisationStore = store;
	}
	
	public ListStore<BeanModel> getOrganizationStore() {
		return organisationStore;
	}

    public ListStore<BeanModel> getCampaignStore() {
        return campaignStore;
    }

    public void setCampaignStore(ListStore<BeanModel> campaignStore) {
        this.campaignStore = campaignStore;
	}

	private void doFinish(ViewModel<Campaign> vem) {
		log.finer("do finish");
		MobilisrEntityEvent event = new MobilisrEntityEvent(MobilisrEvents.WizardFinish, vem.getModelObject());
		resetForms();
		fireEvent(MobilisrEvents.WizardFinish, event);
	}

	@Override
	public void resetForms() {
		log.finer("reset forms");
		for (int i = 1; i< lstButtons.size(); i++){
			lstButtons.get(i).setEnabled(editingNotCreating);
			lstWizardSteps.get(i).resetForm();
		}
	}

	@Override
	public void goCurrent(ViewModel<Campaign> vem) {
		displayWizardWidget(stepSelected, vem);
	}
	
	@Override
	public void setDirty(boolean dirty) {
		for (WizardStep step : lstWizardSteps) {
			if (step instanceof DirtyView){
				((DirtyView)step).setDirty(dirty);
			}
		}
	}
	
	@Override
	public boolean isDirty() {
		for (WizardStep step : lstWizardSteps) {
			if (step instanceof DirtyView && 
					((DirtyView)step).isDirty()){
				return true;
			}
		}
		return false;
	}
}
