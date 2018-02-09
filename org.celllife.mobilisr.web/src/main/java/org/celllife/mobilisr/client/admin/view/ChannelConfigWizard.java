package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.reporting.PconfigParamterFieldFactory;
import org.celllife.mobilisr.client.view.gxt.FormUtil;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.service.gwt.ChannelConfigViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class ChannelConfigWizard extends WizardWindow {

	private final ChannelConfigViewModel model;

	public ChannelConfigWizard(final ChannelConfigViewModel model, ListStore<BeanModel> handlerStore) {
		super();
		this.model = model;

		setHideOnFinish(true);

		WizardCard addNameAndTypeCard = addNameAndTypeCard(handlerStore);
		final WizardCard addConfigCard = addConfigCard();
		addNameAndTypeCard.addListener(MobilisrEvents.WizardStep, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				FormPanel formPanel = addConfigCard.getFormPanel();
				formPanel.removeAll();
				PconfigParamterFieldFactory.createFieldsOnForm(model.getHandler(), formPanel, null, false);
			}
		});
	}

	private WizardCard addNameAndTypeCard(ListStore<BeanModel> handlerStore) {
		WizardCard card = new WizardCard("Config details");

		final FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);

		TextField<String> nameField = new MyGXTTextField(
				Messages.INSTANCE.compulsory()
						+ Messages.INSTANCE.channelConfigName(),
				ChannelConfig.PROP_NAME, false, "e.g. WASP ABC");

		ComboBox<BeanModel> channelHandlerCombo = new ComboBox<BeanModel>();
		channelHandlerCombo.setName(ChannelConfig.PROP_HANDLER);
		channelHandlerCombo.setFieldLabel("Channel Handler");
		channelHandlerCombo.setForceSelection(true);
		channelHandlerCombo.setAllowBlank(false);
		channelHandlerCombo.setEmptyText("Select Channel Handler");
		channelHandlerCombo.setDisplayField(Pconfig.PROP_LABEL);
		channelHandlerCombo.setId("channelHandlerCombo");
		channelHandlerCombo.setStore(handlerStore);

		panel.add(nameField);
		panel.add(channelHandlerCombo);

		FormBinding metaBindings = FormUtil.createFormBinding(panel, true);
		metaBindings.bind(ModelUtil.convertEntityToBeanModel(model));

		card.setFormPanel(panel);
		addCard(card);
		return card;
	}

	private WizardCard addConfigCard() {

		WizardCard groupCard = new WizardCard("WASP configuration details");

		final FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);
		panel.setLabelSeparator("");

		groupCard.setFormPanel(panel);
		addCard(groupCard);
		return groupCard;
	}
}
