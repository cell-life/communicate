package org.celllife.mobilisr.client.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.MyGXTDateField;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTSmsBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.pconfig.model.BaseParameter;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

public class PconfigParamterFieldFactory {
	
	private static final String VALIDATOR_MSISDN = "msisdn";
	private static final String VALIDATOR_MSISDN_LIST = "msisdn_list";
	private static final String DSIPLAY_SMS = "sms";
//	private static final String DISPLAY_HTML = "html";

	public static void createFieldsOnForm(Pconfig pconfig, FormPanel formPanel,
			EntityStoreProvider storeProvider, boolean viewOnly) {
		List<? extends Parameter<?>> parameters = pconfig.getParameters();
		if (parameters == null || parameters.isEmpty()) {
			formPanel.add(new LabelField("No parameters required"));
		} else {
			for (final Parameter<?> param : parameters) {
				Field<?> field = null;
				if (!param.isHidden()) {
					field = PconfigParamterFieldFactory.getField(param,
							storeProvider, viewOnly);
					formPanel.add(field,new FormData("-20"));
				}
			}
		}
	}

	public static Field<?> getField(final Parameter<?> param, EntityStoreProvider storeProvider, boolean viewOnly) {
		if (viewOnly){
			Object value = null;
			if (param instanceof StringParameter){
				final StringParameter stringParam = (StringParameter) param;
				value = stringParam.getValue();
			} else if (param instanceof IntegerParameter){
				final IntegerParameter integerParam = (IntegerParameter) param;
				value = integerParam.getValue();
			} else if (param instanceof DateParameter){
				final DateParameter dateParam = (DateParameter) param;
				Date date = dateParam.getValue();
				value = date == null ? "" : DateTimeFormat.getFormat("dd-MM-yyyy").format(date);
			} else if (param instanceof BooleanParameter){
				final BooleanParameter boolParam = (BooleanParameter) param;
				value = boolParam.getValue();
			} else if (param instanceof EntityParameter){
				final EntityParameter entityParam = (EntityParameter) param;
				value = entityParam.getValueLabel();
			}
			
			return new MyGXTLabelField(value == null ? "" : value.toString(), param.getLabel());
		} else {
		
			if (param instanceof StringParameter){
				final StringParameter stringParam = (StringParameter) param;
				return getStringField(stringParam);
			} else if (param instanceof LabelParameter){
				final LabelParameter labelParam = (LabelParameter) param;
				return getLabelField(labelParam);
			} else if (param instanceof IntegerParameter){
				final IntegerParameter integerParam = (IntegerParameter) param;
				return getIntegerField(integerParam);
			} else if (param instanceof DateParameter){
				final DateParameter dateParam = (DateParameter) param;
				return getDateField(dateParam);
			} else if (param instanceof BooleanParameter){
				final BooleanParameter boolParam = (BooleanParameter) param;
				return getBoolField(boolParam);
			} else if (param instanceof EntityParameter){
				EntityParameter entityParam = (EntityParameter) param;
				return getEntityField(entityParam, storeProvider);
			}
			
			return new LabelField("Unknown parameter type");
		}
	}

	private static Field<?> getLabelField(LabelParameter param) {
		MyGXTLabelField field = new MyGXTLabelField(param.getValue() == null ? "" : param.getValue(), 
				param.getLabel() == null ? "" : param.getLabel());
		if (param.getTooltip() != null && !param.getTooltip().isEmpty())
			field.setToolTip(param.getTooltip());
		return field;
	}

	private static Field<?> getEntityField(final EntityParameter param, EntityStoreProvider presenter) {
		final String displayProperty = param.getDisplayProperty();
		final String valueProperty = param.getValueProperty();
		String searchFields = param.getSearchFields();
		searchFields = searchFields == null ? displayProperty : searchFields;
		
		final ComboBox<ModelData> field = new ComboBox<ModelData>(); 
		field.setEmptyText("Search by " + searchFields);
		field.setFieldLabel(param.getLabel());
		
		if (param.getTooltip() != null && !param.getTooltip().isEmpty())
			field.setToolTip(param.getTooltip());
		
		field.setDisplayField(displayProperty);  
		field.setAllowBlank(param.isOptional());
		field.setStore(presenter.getEntityStore(param.getEntityClass(), searchFields));  
//	    field.setHideTrigger(true);  
	    field.setForceSelection(true);
	    field.setPageSize(10);
	    field.setMinChars(2);
	    
	    applyDefaultValue(field, param);
	    
	    field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
					List<ModelData> selOrgList = field.getSelection();
					ModelData bm = selOrgList.get(0);
					
					param.setValue(bm.get(valueProperty).toString());
					Object valueLabel = bm.get(displayProperty);
					param.setValueLabel(valueLabel.toString());
			}
		});
		
		return field;
	}

	private static void applyDefaultValue(ComboBox<ModelData> field, final EntityParameter param) {
		String value = param.getValue();
		String defaultValue = param.getDefaultValue();
		
		List<ModelData> selection = new ArrayList<ModelData>();
		
		if (value != null) {
			ModelData model = getBeanModel(param, value);
			selection.add(model);
			field.setSelection(selection);
		} else if (defaultValue != null){
			param.setValue(defaultValue);
		} else {
			field.clear();
		}
		
	}

	private static ModelData getBeanModel(EntityParameter param, String value) {
		ModelData model = new BaseModelData();
		model.set(param.getDisplayProperty(), param.getValueLabel());
		model.set(param.getValueProperty(), value);
		return model;
	}

	/**
	 * @param param
	 * @return
	 */
	private static Field<?> getBoolField(final BooleanParameter param) {
		final CheckBox field = new CheckBox();
		field.setFieldLabel(param.getLabel());

		if (param.getTooltip() != null && !param.getTooltip().isEmpty())
			field.setToolTip(param.getTooltip());
		
		field.setName(BaseParameter.PROP_VALUE);
		
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(field.getValue());
			}
		});
		
		applyDefaultValue(param, field);
		return field;
	}

	/**
	 * @param param
	 * @return
	 */
	private static Field<?> getDateField(final DateParameter param) {
		final MyGXTDateField field = new MyGXTDateField(
				param.getLabel(), BaseParameter.PROP_VALUE, param.isOptional(), true,
				null);

		if (param.getTooltip() != null && !param.getTooltip().isEmpty())
			field.setToolTip(param.getTooltip());
		
		if (param.isAllowFuture() && !param.isAllowPast()){
			field.setMinValue(new Date());
		} else if (!param.isAllowFuture() && param.isAllowPast()){
			field.setMaxValue(new Date());
		}
		
		applyDefaultValue(param, field);
		
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(field.getValue());
			}
		});
		
		return field;
	}

	/**
	 * @param param
	 * @param field
	 */
	private static <T> void applyDefaultValue(final Parameter<T> param,
			final Field<T> field) {
		T value = param.getValue();
		T defaultValue = param.getDefaultValue();
		if (value != null) {
			field.setValue(value);
		} else if (defaultValue != null){
			field.setValue(defaultValue);
			param.setValue(defaultValue);
		} else {
			field.clear();
		}
	}

	/**
	 * @param param
	 * @return
	 */
	private static Field<?> getIntegerField(final IntegerParameter param) {
		final NumberField field = new MyGXTNumberField(
				param.getLabel(), BaseParameter.PROP_VALUE, param.isOptional(), null,
				null);
		field.setPropertyEditorType(Integer.class);
		
		if (param.getTooltip() != null && !param.getTooltip().isEmpty())
			field.setToolTip(param.getTooltip());
		
		if (param.getMax() != null){
			field.setMaxValue(param.getMax());
		}
		if (param.getMin() != null){
			field.setMinValue(param.getMin());
		}
		
		Integer value = param.getValue();
		Integer defaultValue = param.getDefaultValue();
		if (value != null){
			field.setValue(value);
		} else if (defaultValue != null){
			field.setValue(defaultValue);
			param.setValue(defaultValue);
		}
		
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(field.getValue().intValue());
			}
		});
		
		return field;
	}

	/**
	 * @param param
	 * @param param
	 * @return
	 */
	private static Field<?> getStringField(final StringParameter param) {
		String displayType = param.getDisplayType();
		if (displayType != null){
			// currently disabled since binding is tricky
			/*if (displayType.equals(DISPLAY_HTML)){
				return getHtmlField(param);
			} else*/ if (displayType.equals(DSIPLAY_SMS)){
				return getSmsField(param);
			}
		}
			
		
		final MyGXTTextField field = new MyGXTTextField(
				param.getLabel(), BaseParameter.PROP_VALUE,
				param.isOptional(), null);

		if (param.getTooltip() != null && !param.getTooltip().isEmpty()) {
			field.setToolTip(param.getTooltip());
		}
		
		if (param.getValidator() != null) {
			String validator = param.getValidator();
			if (VALIDATOR_MSISDN.equals(validator)){
				field.setValidator(ValidatorFactory.getMsisdnValidator());
			} else if (VALIDATOR_MSISDN_LIST.equals(validator)){
				field.setValidator(ValidatorFactory.getMsisdnListValidator());
			}
		} else if (param.getRegex() != null && !param.getRegex().isEmpty()) {
			field.setRegex(param.getRegex(), param.getErrorMessage());
		}
		
		if (displayType != null){
			if (displayType.equals(StringParameter.TYPE_MEDIUM)){
				field.setHeight(100);
			} else if (displayType.equals(StringParameter.TYPE_LARGE)){
				field.setHeight(200);
			}
		}
		
		applyDefaultValue(param, field);
		
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(field.getValue());
			}
		});
		return field;
	}

	private static Field<?> getSmsField(final StringParameter param) {
		final MyGXTSmsBox smsbox = new MyGXTSmsBox();
		// set width initially to force correct size. Thereafter
		// parent will resize if dialog is resized.
		smsbox.getMsgTxtArea().setSize(339, 180);
		
		smsbox.getMsgTxtArea().setAllowBlank(param.isOptional());
		
		if (param.getTooltip() != null && !param.getTooltip().isEmpty()) {
			smsbox.getMsgTxtArea().setToolTip(param.getTooltip());
		}
		
		applyDefaultValue(param, smsbox.getMsgTxtArea());
		
		smsbox.getMsgTxtArea().addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(smsbox.getMsgTxtArea().getValue());
			}
		});
		
		LayoutContainer c1 = new LayoutContainer();
        c1.setLayout(new RowLayout(Orientation.VERTICAL));
        c1.add(smsbox.getMsgTxtArea(), new RowData(1,-1));
        c1.add(smsbox.getToolBar(),new RowData());
        
        AdapterField typeAdapter = new AdapterField(c1);
        typeAdapter.setResizeWidget(true);
        typeAdapter.setFieldLabel(param.getLabel());
        
		return typeAdapter;
	}

	/**
	 * FIXME: unable to add listener to HtmlEditor to do binding.
	 * 
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unused")
	private static Field<?> getHtmlField(final StringParameter param) {
		final HtmlEditor field = new HtmlEditor();
		field.setFieldLabel(param.getLabel());
		
		applyDefaultValue(param, field);
		
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				param.setValue(field.getRawValue());
			}
		});
		
		return field;
	}
}
