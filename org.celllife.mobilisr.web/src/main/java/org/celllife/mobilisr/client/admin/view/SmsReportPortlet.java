package org.celllife.mobilisr.client.admin.view;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTDateField;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.ReportServiceAsync;
import org.celllife.mobilisr.service.gwt.SmsReportData;
import org.gwttime.time.DateTime;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.Legend;
import com.extjs.gxt.charts.client.model.Legend.Position;
import com.extjs.gxt.charts.client.model.LineDataProvider;
import com.extjs.gxt.charts.client.model.ScaleProvider;
import com.extjs.gxt.charts.client.model.axis.XAxis;
import com.extjs.gxt.charts.client.model.axis.YAxis;
import com.extjs.gxt.charts.client.model.charts.FilledBarChart;
import com.extjs.gxt.charts.client.model.charts.LineChart;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SmsReportPortlet implements MobilisrPortlet {

	private Organization organization;
	private Date dateStart;
	private Date dateEnd;
	private DatePeriod period = DatePeriod.DAY;

	private Dialog configDialog;
	private CheckBox chkAllOrgs = new CheckBox();
	private ComboBox<BeanModel> cmbOrganization = new MyGXTComboBox<BeanModel>("Select an Organisation",
			Organization.PROP_NAME, true);
	private MyGXTDateField dateFrom = new MyGXTDateField("Start date", null, false, true, null);
	private MyGXTDateField dateTo = new MyGXTDateField("End date", null, false, true, null);
	private SimpleComboBox<DatePeriod> periodType = new SimpleComboBox<DatePeriod>();

	private Chart chart;
	protected OrganizationServiceAsync orgService;
	private ListStore<BeanModel> orgListStore;
	private ReportServiceAsync reportService;
	private ListStore<BeanModel> dataStore;
	
	public SmsReportPortlet(OrganizationServiceAsync orgService, ReportServiceAsync reportService) {
		this.orgService = orgService;
		this.reportService = reportService;
		bind();
	}

	@Override
	public Portlet getPortlet() {
		Portlet portlet = new Portlet();
		portlet.setHeading("Usage graph");
		portlet.setLayout(new RowLayout(Orientation.VERTICAL));
		portlet.setPinned(false);
		
		portlet.getHeader().addTool(new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>() {  
	           @Override  
	           public void componentSelected(IconButtonEvent ce) {  
	        	   showConfigDialog();
	           }  
         }));
		portlet.add(getChart(), new RowData(1,1,new Margins(0)));
		reset();
		updateConfiguration();
		return portlet;
	}

	protected void showConfigDialog() {
		if (configDialog == null) {
			configDialog = new Dialog();
			configDialog.setHeading("Configure chart");
			configDialog.setButtons(Dialog.CLOSE);
			configDialog.setBodyStyleName("pad-text");
			configDialog.setScrollMode(Scroll.AUTO);
			configDialog.setHideOnButtonClick(true);
			configDialog.setLayout(new BorderLayout());
			configDialog.setHeight(300);
			configDialog.setWidth(400);
			configDialog.add(getDetails(), new BorderLayoutData(LayoutRegion.CENTER, 250));
			configDialog.getButtonById(Dialog.CLOSE).addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					updateConfiguration();
				}
			});
		}
		configDialog.show();
	}

	protected void updateConfiguration() {
		dateStart = dateFrom.getValue();
		dateEnd = dateTo.getValue();
		period = periodType.getSimpleValue();
		
		BeanModel value = cmbOrganization.getValue();
		if (value != null) {
			organization = value.getBean();
		} else {
			organization = null;
		}
		updateChart();
	}

	private void updateChart() {
		dataStore.getLoader().load();
	}

	private void bind() {
		RpcProxy<PagingLoadResult<Organization>> orgProxy = new RpcProxy<PagingLoadResult<Organization>>() {

			@Override
			protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<Organization>> callback) {
				PagingLoadConfig config = (PagingLoadConfig) loadConfig;
				config.set(RemoteStoreFilterField.PARM_FIELDS, Organization.PROP_NAME);
				orgService.listAllOrganizations(config, false, callback);
			}
		};

		final ListLoader<PagingLoadResult<Organization>> orgLoader = new BasePagingLoader<PagingLoadResult<Organization>>(
				orgProxy, new BeanModelReader());
		orgListStore = new ListStore<BeanModel>(orgLoader);
		cmbOrganization.setStore(orgListStore);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				orgLoader.load();
			}
		});
		
		RpcProxy<List<SmsReportData>> reportDataProxy = new RpcProxy<List<SmsReportData>>() {

			@Override
			protected void load(Object loadConfig,
					AsyncCallback<List<SmsReportData>> callback) {
				reportService.getSmsReportData(organization, dateStart, dateEnd,
						period, callback);
			}
		};

		ListLoader<ListLoadResult<SmsReportData>> reportLoader = new BaseListLoader<ListLoadResult<SmsReportData>>(reportDataProxy, new BeanModelReader());
		reportLoader.addLoadListener(new LoadListener(){
			@Override
			public void loaderLoad(LoadEvent le) {
				BaseListLoadResult<BeanModel> loadResult = le.getData();
				List<BeanModel> data = loadResult.getData();
				dataStore.removeAll();
				dataStore.add(data);
				chart.setChartModel(getChartModel());
				chart.getChartModel().getXAxis().getLabels().setRotationAngle(45);
			}
		});

		dataStore = new ListStore<BeanModel>(reportLoader);
		
		periodType.add(Arrays.asList(DatePeriod.values()));
	}

	private LayoutContainer getDetails() {
		LayoutContainer lc = new LayoutContainer();
		VBoxLayout vLayout = new VBoxLayout();
		vLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		lc.setLayout(vLayout);

		FieldSet controls = new FieldSet();
		controls.setHeading("Parameters");
		FormLayout fl = new FormLayout();
		fl.setLabelWidth(100);
		controls.setLayout(fl);
		
		chkAllOrgs.setFieldLabel("All organisations");
		chkAllOrgs.setValue(true);
		chkAllOrgs.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				updateOrganisationCombo();
			}
		});
		controls.add(chkAllOrgs);

		cmbOrganization.setFieldLabel("Organisation");
		cmbOrganization.setAllowBlank(false);
		cmbOrganization.setEnabled(false);

		controls.add(cmbOrganization);
		
		dateFrom.setMaxValue(new Date());
		dateFrom.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				dateTo.setMinValue(dateFrom.getValue());
			}
		});
		controls.add(dateFrom);
		
		dateTo.setMaxValue(new Date());
		dateTo.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				dateFrom.setMaxValue(dateTo.getValue());
			}
		});
		controls.add(dateTo);
		
		periodType.setFieldLabel("Show by");
		periodType.setTriggerAction(TriggerAction.ALL);
		periodType.setForceSelection(true);
		periodType.setEditable(false);
		periodType.setAllowBlank(false);
		periodType.setToolTip("Do you want to see the messages per day, month or year.");
		controls.add(periodType);
		
		MyGXTButton btnReset = new MyGXTButton("Reset");
		btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				reset();
			}
		});
		AdapterField btnResetAdapter = new AdapterField(btnReset);
		btnResetAdapter.setFieldLabel("Reset values");
		controls.add(btnResetAdapter);
		
		lc.add(controls);
		return lc;
	}

	protected void updateOrganisationCombo() {
		if (chkAllOrgs.getValue()){
			cmbOrganization.setEnabled(false);
			cmbOrganization.clear();
			cmbOrganization.setAllowBlank(true);
		} else {
			cmbOrganization.setEnabled(true);
			cmbOrganization.setAllowBlank(false);
		}
	}

	protected void reset() {
		dateFrom.setValue(new DateTime().minusMonths(1).toDate());
		dateFrom.setMaxValue(new Date());
		
		dateTo.setValue(new Date());
		dateTo.setMinValue(dateFrom.getValue());
		
		periodType.setSimpleValue(DatePeriod.DAY);
		chkAllOrgs.setValue(true);
		updateOrganisationCombo();
	}

	private Widget getChart() {
		String url = "gxt/chart/open-flash-chart.swf";

		chart = new Chart(url);
		chart.setBorders(true);
		chart.setChartModel(getEmptyChartModel());
		chart.setSize(250, 300);
		return chart;
	}

	private ChartModel getEmptyChartModel() {
		ChartModel cm = new ChartModel("Messages per day");
		cm.setDecimalSeparatorComma(true);
		XAxis xa = new XAxis();
		xa.setLabels("no data");
		xa.getLabels().setRotationAngle(45);
		cm.setXAxis(xa);
		YAxis ya = new YAxis();
		ya.setRange(0, 10);
		ya.setSteps(5);
		cm.setYAxis(ya);
		FilledBarChart bchart = new FilledBarChart();
		bchart.addValues(0);
		cm.addChartConfig(bchart);
		return cm;
	}

	private ChartModel getChartModel() {
		String titleText = "Messages per " + period.toString().toLowerCase() + " for ";
		if (organization == null){
			titleText += "all organisations";
		} else {
			titleText += organization.getName();
		}
		ChartModel cm = new ChartModel(titleText);
		cm.setScaleProvider(ScaleProvider.ROUNDED_NEAREST_SCALE_PROVIDER);
		cm.setLegend(new Legend(Position.TOP));
	
		LineChart total = new LineChart(); 
		total.setText("Total messages");
		LineDataProvider dataProvider = new LineDataProvider(SmsReportData.PROP_TOTAL, SmsReportData.PROP_LABEL);  
		dataProvider.bind(dataStore);  
		total.setDataProvider(dataProvider);  
		cm.addChartConfig(total);
		
		LineChart failures = new LineChart();
		failures.setText("Failures");
		failures.setColour("#FF0000");
		dataProvider = new LineDataProvider(SmsReportData.PROP_FAILURES, SmsReportData.PROP_LABEL);  
		dataProvider.bind(dataStore);  
		failures.setDataProvider(dataProvider);
		cm.addChartConfig(failures);
		return cm;
	}

}