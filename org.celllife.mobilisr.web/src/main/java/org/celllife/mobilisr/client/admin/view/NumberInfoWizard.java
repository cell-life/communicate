package org.celllife.mobilisr.client.admin.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.view.gxt.FormUtil;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.StepValidator;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class NumberInfoWizard extends WizardWindow {

	private final NumberInfo model;
	private WizardCard card;

	public NumberInfoWizard(final NumberInfo model, ListStore<BeanModel> channelStore) {
		super();
		this.model = model;

		addCard(channelStore);
	}

	private WizardCard addCard(ListStore<BeanModel> channelStore) {
		card = new WizardCard("Details");
		card.setHtmlText("");
		
		final FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);

		TextField<String> nameField = new MyGXTTextField(
				Messages.INSTANCE.compulsory()
						+ Messages.INSTANCE.numberInfoName(),
				NumberInfo.PROP_NAME, false, "e.g. South Africa 2772");
		
		TextField<String> prefix = new MyGXTTextField(
				Messages.INSTANCE.compulsory()
						+ Messages.INSTANCE.numberInfoPrefix(),
				NumberInfo.PROP_PREFIX, false, "e.g. 2772");
		
		TextField<String> validator = new MyGXTTextField(
				Messages.INSTANCE.compulsory()
						+ Messages.INSTANCE.numberInfoRegex(),
				NumberInfo.PROP_VALIDATOR, false, "e.g. ^2772[0-9]{7}$");

		ComboBox<BeanModel> channelCombo = new ComboBox<BeanModel>();
		channelCombo.setName(NumberInfo.PROP_CHANNEL);
		channelCombo.setFieldLabel(Messages.INSTANCE.compulsory()
				+ Messages.INSTANCE.numberInfoChannel());
		channelCombo.setForceSelection(true);
		channelCombo.setAllowBlank(false);
		channelCombo.setEmptyText("Select Channel");
		channelCombo.setDisplayField(Channel.PROP_NAME);
		channelCombo.setId("channelCombo");
		channelCombo.setStore(channelStore);

		panel.add(nameField);
		panel.add(prefix);
		panel.add(validator);
		panel.add(channelCombo);

		FormBinding metaBindings = FormUtil.createFormBinding(panel, true);
		metaBindings.bind(ModelUtil.convertEntityToBeanModel(model));

		card.setFormPanel(panel);
		addCard(card);
		return card;
	}

	public void setErrorMessage(String message) {
		if (message != null && !message.isEmpty()) {
			card.setHtmlText("<b><span style=\"color: #f00;\">" + message + "</span></b>");
		}
	}
	
	public void setSaveCallback(StepValidator callback){
		card.setValidator(callback);
	}
}
