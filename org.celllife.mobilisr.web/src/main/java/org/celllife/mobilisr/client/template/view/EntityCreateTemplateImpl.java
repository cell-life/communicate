package org.celllife.mobilisr.client.template.view;

import org.celllife.mobilisr.client.view.gxt.CenterTopLayout;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;

public abstract class EntityCreateTemplateImpl<T> extends BaseFormView<T> {

	protected LayoutContainer formCenterContainer = new LayoutContainer();
	protected LayoutContainer formElementContainer = new LayoutContainer();
	protected TabPanel tabPanel = new TabPanel();

	public void layoutCreateTemplate(String titleLabelText) {

		addTitleLabel(titleLabelText);
		
		configureFormPanel();
		
		configureFormElementContainer();
		
		configureFormCenterContainer();
		
		Margins m = new Margins(10);
		add(getMessageLabel(), new RowData(1,-1, m));
		add(getFormPanel(), new RowData(1,1,m));
	}

	protected void configureFormCenterContainer() {
		formCenterContainer.setSize("100%", "100%");
		formCenterContainer.setLayout(new CenterTopLayout());
		formCenterContainer.add(formElementContainer);
		getFormPanel().add(formCenterContainer, new RowData(1, 1, new Margins(10)));
	}

	protected void configureFormElementContainer() {
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(150);
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoWidth(true);
		formElementContainer.setAutoHeight(true);
	}
	
	public void layoutCreateTemplateUsingTabs(String titleLabelText) {

		addTitleLabel(titleLabelText);
				
		configureFormPanel();		
		
		addTabPanel();
		
		Margins m = new Margins(10);
		add(getHeaderLabel(), new RowData(1,-1, m));
		add(getMessageLabel(), new RowData(1,-1, m));
		add(getFormPanel(), new RowData(1,1,m));
	}

	protected void addTabPanel() {
		getFormPanel().add(tabPanel, new RowData(1, 1, new Margins(10)));
	}

}
