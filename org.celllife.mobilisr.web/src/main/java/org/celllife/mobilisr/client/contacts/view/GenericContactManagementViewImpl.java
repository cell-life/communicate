package org.celllife.mobilisr.client.contacts.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.view.gxt.ChangeAwareListStore;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;

public abstract class GenericContactManagementViewImpl<T> extends Window implements GenericContactManagementView<T> {

	public class ContactManagementText {
		public String listEntityPanelHeading;
		public String addAllAnchorText;
		public String selEntityPanelHeading;
		public String removeAllAnchorText;
		public String addNewEntityFieldSetHeading;
		public String addNewEntityBtnText;
		public String popupHeading;
	}
	
	private AbstractImagePrototype nextImgPrototype = GXT.IMAGES.paging_toolbar_next();
	
	private Anchor addAllAnchor = new Anchor();
	private Anchor removeAllAnchor = new Anchor();
	
	private ContentPanel listEntityPanel = new ContentPanel();
	private ToolBar leftTopToolBar = new ToolBar();
	private Grid<BeanModel> listOfEntityGrid;
	private PagingToolBar listEntityToolBar;
	
	private ContentPanel selEntityPanel = new ContentPanel();
	private Grid<BeanModel> selectedEntityGrid;
	private MyGXTButton nextResultButton;
	private Label numOfRecordLabel;
	
	private FieldSet addNewEntityFieldSet;
	private TextField<?> newEntityField;
	private MyGXTButton addNewEntityBtn;
		
	private MyGXTButton doneBtn;
	private MyGXTButton cancelBtn;;
	
	private ButtonBar buttonBar = new ButtonBar();
	
	private int currentRecordsDisplayed = 0;
	private int totalRecords = 0;
	
	private boolean isAddAllSelected = false;
	private boolean isRemoveAllSelected = false;

	private ContactManagementText text;
	private int addAllMode = GenericContactManagementView.MODE_ADDALL;
	private int maxAddAllValue = 0;

	public GenericContactManagementViewImpl() {
		setWidth(800);
		//setHeight(500);
		setAutoHeight(true);
		setPlain(true);  
		setModal(true);  
		setBlinkModal(true);  
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		doneBtn = new MyGXTButton("doneBtn", Messages.INSTANCE.done());
		cancelBtn = new MyGXTButton("cancelBtn", Messages.INSTANCE.cancel());
		nextResultButton = new MyGXTButton("nextButton", "Next 10 Results");
		numOfRecordLabel = new Label("Displaying {0} of {1} records");
		
		addNewEntityFieldSet = new FieldSet();
		addNewEntityBtn = new MyGXTButton();
		
		listEntityToolBar = new PagingToolBar( Constants.INSTANCE.pageSize() );
		
		newEntityField = getNewEntityField();
		//setting the field name so that the validator autobinds
		newEntityField.setName("newEntityField");
		
		LayoutContainer addNewEntityContainer = new LayoutContainer();
		createNewEntityForm(addNewEntityContainer);
		
		LayoutContainer entityContainer = new LayoutContainer();
		entityContainer.setLayout(new ColumnLayout());
		
		createLeftGridPanel(entityContainer);
		createRightGridPanel(entityContainer);
		
		createButtonBar();
		
		doneBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});
		
		cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});
		
		add(entityContainer, new RowData(1, -1, new Margins(3)));
		add(addNewEntityContainer, new RowData(-1, -1));
		add(buttonBar, new RowData(-1, 1));
	}
	
	protected void init(ContactManagementText text) {
		this.text = text;
		setHeading(text.popupHeading);
		setId("selectContacts");
		getHeader().setId("selectContactsHeader");
		listEntityPanel.setId("allItemsPanel");
		listEntityPanel.setHeading(text.listEntityPanelHeading);
		addAllAnchor.setText(text.addAllAnchorText);
		addAllAnchor.getElement().setId("addAllAnchor");
		selEntityPanel.setId("selectedItemsPanel");
		selEntityPanel.setHeading(text.selEntityPanelHeading);
		removeAllAnchor.setText(text.removeAllAnchorText);
		removeAllAnchor.getElement().setId("removeAllAnchor");
		addNewEntityFieldSet.setHeading(text.addNewEntityFieldSetHeading);
		addNewEntityFieldSet.setId("addNewEntityFieldSet");
		addNewEntityBtn.setText(text.addNewEntityBtnText);
		addNewEntityBtn.setToolTip("Add");
		addNewEntityBtn.setId("addNewEntityBtn");
		
	}
	
	@Override
	public void addAllSelected(int totalCount) {
		setAddAllSelected(true);
		setRemoveAllSelected(false);
		setLeftGridEnabled(false);
		clearRightGrid();
		setNextTenButtonEnabled(false);
		updatePagingLabel(0, totalCount);
	}

	private boolean addBeanToGrid(Grid<BeanModel> grid, BeanModel beanModel){
		boolean isAdded = false;
		ChangeAwareListStore<BeanModel> groupStore = (ChangeAwareListStore<BeanModel>) grid.getStore();
		if(!groupStore.contains(beanModel)){
			groupStore.add(beanModel);
			updatePagingLabel(currentRecordsDisplayed+1, totalRecords+1);
			isAdded = true;
		}
		return isAdded;
	}	
	
	@Override
	public void addEntityToLeftGrid(T entityObject){
		BeanModel beanModel = convertEntityToBeanModel(entityObject);
		ListStore<BeanModel> listStore = listOfEntityGrid.getStore();
		if(!listStore.contains(beanModel)){
			listStore.add(beanModel);
			listStore.getLoader().load();
		}
	}
	
	@Override
	public boolean addEntityToRightGrid(BeanModel beanModel){
		return addBeanToGrid(selectedEntityGrid, beanModel);
	}
	
	@Override
	public boolean addEntityToRightGrid(T entityObject){
		BeanModel beanModel = convertEntityToBeanModel(entityObject);
		return addBeanToGrid(selectedEntityGrid, beanModel);
	}
	
	@Override
	public void buildLeftGroupGridPanel(ListStore<BeanModel> listEntityStore,
			StoreFilterField<BeanModel> listEntityFilter,
			GridCellRenderer<BeanModel> addBtnRenderer){
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig idConfig = new ColumnConfig("id", "Add", 35);
		idConfig.setRenderer(addBtnRenderer);
		configs.add( idConfig);
		configs.add( getGridColumnConfig() );
	
		ColumnModel cm = new ColumnModel( configs );
		
		listOfEntityGrid = new Grid<BeanModel>( listEntityStore, cm );
		
		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");
		
		listOfEntityGrid.setLoadMask(true);
		listOfEntityGrid.setView(gridView);
		listOfEntityGrid.setBorders( false );
		listOfEntityGrid.setAutoHeight(true);
		listOfEntityGrid.setStripeRows( true );
		listOfEntityGrid.recalculate();
		listEntityPanel.add( listOfEntityGrid );
		
		listEntityFilter.bind(listEntityStore);
		listEntityFilter.setEmptyText("Search for a " + getEntityName());
				
		leftTopToolBar.setAlignment(HorizontalAlignment.LEFT);
		leftTopToolBar.setIntStyleAttribute("padding", 5);
		leftTopToolBar.setSpacing(10);
		leftTopToolBar.add(listEntityFilter);  
		leftTopToolBar.add(new FillToolItem());
		leftTopToolBar.add(new AdapterField(addAllAnchor));
		
		listEntityPanel.setTopComponent(leftTopToolBar);
		listEntityPanel.setBottomComponent( listEntityToolBar );
	}
	
	@Override
	public void buildRightGroupGridPanel(ListStore<BeanModel> selectedEntityStore,
			StoreFilterField<BeanModel> selectedEntityFilter,
			GridCellRenderer<BeanModel> removeBtnRenderer){
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig idConfig = new ColumnConfig("id", "Remove", 35);
		idConfig.setRenderer(removeBtnRenderer);
		idConfig.setAlignment(HorizontalAlignment.CENTER);
		configs.add( idConfig);
		configs.add( getGridColumnConfig() );
		
		ColumnModel cm = new ColumnModel( configs );
		
		selectedEntityGrid = new Grid<BeanModel>( selectedEntityStore, cm );
		
		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");
		
		selectedEntityGrid.setLoadMask(true);
		selectedEntityGrid.setView(gridView);
		selectedEntityGrid.setBorders( false );
		selectedEntityGrid.setHeight("100%");
		selectedEntityGrid.setStripeRows( true );
		selectedEntityGrid.recalculate();
		selEntityPanel.add( selectedEntityGrid );
		
		selectedEntityFilter.bind(selectedEntityStore);
		selectedEntityFilter.setEmptyText("Search for a " + getEntityName());
		
		ToolBar topToolBar = new ToolBar();  
		topToolBar.setAlignment(HorizontalAlignment.LEFT);
		topToolBar.setIntStyleAttribute("padding", 5);
		topToolBar.setSpacing(10);
		topToolBar.add(selectedEntityFilter);  
		topToolBar.add(new FillToolItem());
		topToolBar.add(new AdapterField(removeAllAnchor));
		
		selEntityPanel.setTopComponent(topToolBar);
		
		nextResultButton.setIcon(nextImgPrototype);
		nextResultButton.setToolTip("Next 10 Results");
		nextResultButton.disable();
		
		updatePagingLabel(0, selectedEntityStore.getCount());
		
		ToolBar bottomToolBar = new ToolBar();  
		bottomToolBar.setAlignment(HorizontalAlignment.LEFT);
		bottomToolBar.setSpacing(10);
		bottomToolBar.add(new AdapterField(nextResultButton));
		bottomToolBar.add(numOfRecordLabel);  
		
		selEntityPanel.setBottomComponent(bottomToolBar);
	}

	@Override
	public void clearAddNewEntityFieldValue() {
		newEntityField.clear();
	}
	
	@Override
	public void clearRightGrid() {
		ChangeAwareListStore<BeanModel> storeList = (ChangeAwareListStore<BeanModel>) selectedEntityGrid.getStore();
		storeList.removeAllIgnoreChanges();
	}
	
	@Override
	public void clearUI() {
		//Reset anchor selections
		setAddAllSelected(false);
		setRemoveAllSelected(false);
		
		//Enable the left grid
		setLeftGridEnabled(true);
		
		//Clear the right grid
		clearRightGrid();
		updatePagingLabel(0, 0);
		
		//Clear new entity field value
		clearAddNewEntityFieldValue();
	}
	
	private BeanModel convertEntityToBeanModel(T entityobject){
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(entityobject.getClass());
		BeanModel beanModel =  beanModelFactory.createModel(entityobject);
		return beanModel;
	}
	
	private void createButtonBar(){
		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.setSpacing(10);
		buttonBar.add(doneBtn);
		buttonBar.add(cancelBtn);
	}

	private void createLeftGridPanel(LayoutContainer entityContainer){
		listEntityPanel.setHeight(400);
		addAllAnchor.setHref("#");
		LayoutContainer leftContainer = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		leftContainer.add(listEntityPanel, new RowData(1, -1, new Margins(3)));
		entityContainer.add(leftContainer, new ColumnData(.5));
	}

	private void createNewEntityForm(LayoutContainer addNewEntityContainer){
		FormPanel formContainer = new FormPanel();
		formContainer.setHeaderVisible(false);
		formContainer.setBorders(false);
		formContainer.setBodyBorder(false);
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		FormData formData = new FormData();
		formData.setMargins(new Margins(3));
		
		LayoutContainer groupContainer = new LayoutContainer();
		groupContainer.setBorders(false);
        groupContainer.setLayout(formLayout);
        groupContainer.add(newEntityField, formData);		
		
		addNewEntityBtn.setIntStyleAttribute("margin", 5);
		addNewEntityFieldSet.setCollapsible(true);
		addNewEntityFieldSet.setLayout(new ColumnLayout());
		addNewEntityFieldSet.add(groupContainer, new ColumnData(0.7));
		addNewEntityFieldSet.add(addNewEntityBtn, new ColumnData(0.3));
		
		formContainer.add(addNewEntityFieldSet);

		//binding the button to the forms to enable validation
		FormButtonBinding formButtonBinding = new FormButtonBinding(formContainer);
		formButtonBinding.addButton(addNewEntityBtn);
		
		addNewEntityContainer.add(formContainer);
		addNewEntityContainer.setBorders(false);
	}

	protected abstract <D> TextField<D> getNewEntityField();

	private void createRightGridPanel(LayoutContainer entityContainer){
		selEntityPanel.setHeight(400);
		selEntityPanel.setLayout(new FitLayout());
		selEntityPanel.setBodyBorder(false);
		selEntityPanel.setBorders(true);
		
		LayoutContainer rightContainer = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		removeAllAnchor.setHref("#");
		rightContainer.add(selEntityPanel, new RowData(1, -1, new Margins(3)));
		entityContainer.add(rightContainer, new ColumnData(.5));
	}

	@Override
	public void filterInRightGridStore(String filterValue, String filterProperty) {
		ListStore<BeanModel> storeList = selectedEntityGrid.getStore();
		storeList.filter(filterProperty);
	}

	@Override
	public Anchor getAddAllAnchor() {
		return addAllAnchor;
	}

	@Override
	public Button getAddNewEntityButton() {
		return addNewEntityBtn;
	}

	@Override
	public String getAddNewEntityFieldValue() {
		return newEntityField.getRawValue();
	}

	@Override
	public int getCurrentRecordsDisplayed() {
		return currentRecordsDisplayed;
	}

	protected abstract String getEntityName();

	protected abstract ColumnConfig getGridColumnConfig();

	@Override
	public PagingToolBar getListOfEntityToolBar() {
		return listEntityToolBar;
	}

	@Override
	public Button getNextTenButton() {
		return nextResultButton;
	}
	
	@Override 
	public Button getDoneButton(){
		return doneBtn;
	}

	@Override
	public Anchor getRemoveAllAnchor() {
		return removeAllAnchor;
	}

	protected ContentPanel getSelectedEntityPanel(){
		return selEntityPanel;
	}

	@Override
	public int getTotalRecords() {
		return totalRecords;
	}

	@Override
	public boolean isAddAllSelected() {
		return isAddAllSelected;
	}

	@Override
	public boolean isRemoveAllSelected() {
		return isRemoveAllSelected;
	}

	private void removeAllFromSelectedGrid() {
		ChangeAwareListStore<BeanModel> selEntityStore = (ChangeAwareListStore<BeanModel>) selectedEntityGrid.getStore();
		selEntityStore.removeAllClearChanges();
		updatePagingLabel(0, selEntityStore.getCount());
	}

	@Override
	public void removeAllSelected() {
		removeAllFromSelectedGrid();
		setAddAllSelected(false);
		setRemoveAllSelected(true);
		setLeftGridEnabled(true);
		setNextTenButtonEnabled(false);
		updatePagingLabel(0, 0);
	}

	@Override
	public void removeEntityFromSelectedGrid(BeanModel beanModel) {
		ChangeAwareListStore<BeanModel> groupStore = (ChangeAwareListStore<BeanModel>) selectedEntityGrid.getStore();
		groupStore.remove(beanModel);
		updatePagingLabel(currentRecordsDisplayed-1, totalRecords-1);
	}

	private void setAddAllSelected(boolean addAll) {
		isAddAllSelected = addAll;
	}

	private void setLeftGridEnabled(boolean enableStatus) {
		leftTopToolBar.setEnabled(enableStatus);
		listOfEntityGrid.setEnabled(enableStatus);
		listEntityToolBar.setEnabled(enableStatus);
	}

	@Override
	public void setNextTenButtonEnabled(boolean buttonStatus) {
		nextResultButton.setEnabled(buttonStatus);
	}

	private void setRemoveAllSelected(boolean removeAll) {	
		isRemoveAllSelected = removeAll;
	}
	
	@Override
	public void showPopup(String entityValueTxt) {
		setHeading(Format.substitute(text.popupHeading, entityValueTxt));
		getAddAllAnchor().setText(Format.substitute(text.addAllAnchorText, entityValueTxt));
		getSelectedEntityPanel().setHeading(Format.substitute(text.selEntityPanelHeading, entityValueTxt));
		show();
		center();
	}
	
	@Override
	public void updatePagingLabel(int currentRecordsDisplayed, int totalRecords) {
		this.currentRecordsDisplayed = currentRecordsDisplayed;
		this.totalRecords = totalRecords;
		String label = "Displaying " + currentRecordsDisplayed + "of " + totalRecords + " records";
		numOfRecordLabel.setText(label);
	}
	
	@Override
	public void showAddAllAnchor( boolean showAnchor ){
		addAllAnchor.setVisible(showAnchor);
	}
	
	@Override
	public void setAddAllMode(int mode, int limitVal){
		addAllMode = mode;
		maxAddAllValue = limitVal;
	}

	@Override
	public int getMaxAddAllValue() {
		return maxAddAllValue;
	}

	@Override
	public int getAddAllMode(){
		return addAllMode;
	}

	@Override
	public boolean isDirty() {
		if (isAddAllSelected || isRemoveAllSelected)
			return true;
		
		ChangeAwareListStore<BeanModel> store = (ChangeAwareListStore<BeanModel>) selectedEntityGrid.getStore();
		return (store!=null)? store.isDirty(): true;
	}
	
}
