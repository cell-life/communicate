package org.celllife.mobilisr.client.admin.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.admin.ChannelCreateView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class ChannelCreateViewImpl extends EntityCreateTemplateImpl<ChannelViewModel> implements
		ChannelCreateView {

	private TextField<String> name;
	private TextField<String> shortcode;
	private ComboBox<BeanModel> channelHandlerCombo;
	private ComboBox<BeanModel> channelConfigCombo;
	private SelectionChangedListener<BeanModel> handlerChangeListener;

	@Override
	public void createView() {
		super.createView();

		layoutCreateTemplate(Messages.INSTANCE.channelCreateHeader(ChannelType.IN));
		
		createForm();
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}
	
	private void createForm() {
		name = new MyGXTTextField(Messages.INSTANCE.channelName(),
				Channel.PROP_NAME, false, Messages.INSTANCE.channelNameExample());
		shortcode = new MyGXTTextField(Messages.INSTANCE.channelShortcode(),
				Channel.PROP_SHORT_CODE, false, Messages.INSTANCE.channelShortcodeExample());
		
		channelHandlerCombo = new ComboBox<BeanModel>();
		channelHandlerCombo.setName(Channel.PROP_HANDLER);
		channelHandlerCombo.setFieldLabel("Channel Handler");
		channelHandlerCombo.setForceSelection(true);
		channelHandlerCombo.setAllowBlank(false);
		channelHandlerCombo.setEmptyText("Select Channel Handler");
		channelHandlerCombo.setDisplayField(Pconfig.PROP_LABEL);
		channelHandlerCombo.setId("channelHandlerCombo");
		
		channelConfigCombo = new ComboBox<BeanModel>();
		channelConfigCombo.setName(Channel.PROP_CONFIG);
		channelConfigCombo.setFieldLabel("Handler Config");
		channelConfigCombo.setForceSelection(true);
		channelConfigCombo.setAllowBlank(false);
		channelConfigCombo.setEmptyText("Select Handler Config");
		channelConfigCombo.setDisplayField(ChannelConfig.PROP_NAME);
		channelConfigCombo.setId("channelConfigCombo");
		
		formElementContainer.add(name);
		formElementContainer.add(shortcode);
		formElementContainer.add(channelHandlerCombo);
		formElementContainer.add(channelConfigCombo);
		
		handlerChangeListener = new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				channelConfigCombo.clear();
			}
		};
	}
	
	@Override
	public void setChannelConfigStore(ListStore<BeanModel> store) {
		channelConfigCombo.setStore(store);
	}
	
	@Override
	public void setChannelHandlerStore(ListStore<BeanModel> store) {
		channelHandlerCombo.setStore(store);
	}
	
	@Override
	public void enableConfigSelection(boolean enable) {
		channelConfigCombo.clear();
		channelConfigCombo.setEnabled(enable);
		channelConfigCombo.setAllowBlank(!enable);
	}
	
	@Override
	public ComboBox<BeanModel> getChannelHandlerCombo() {
		return channelHandlerCombo;
	}
	
	@Override
	public void setFormObject(ViewModel<ChannelViewModel> viewModel) {
		channelHandlerCombo.removeSelectionListener(handlerChangeListener);
		super.setFormObject(viewModel);
		ChannelType type = viewModel.getModelObject().getType();
		getHeaderLabel().setText(Messages.INSTANCE.channelCreateHeader(type));
		switch (type){
		case IN:
			shortcode.setVisible(true);
			shortcode.setAllowBlank(false);
			enableShortcode(viewModel.isModeCreate());
			break;
		case OUT:
			shortcode.setVisible(false);
			shortcode.setAllowBlank(true);
			break;
		}
		channelHandlerCombo.addSelectionChangedListener(handlerChangeListener);
	}
	
	@Override
	public ViewModel<ChannelViewModel> getFormObject() {
		ViewModel<ChannelViewModel> formObject = super.getFormObject();
		if (channelConfigCombo.getSelection().isEmpty()){
			formObject.getModelObject().setConfig(null);
		}
		return formObject;
	}
	
	@Override
	public void enableShortcode(boolean enable) {
		shortcode.setEnabled(enable);
	}
}
