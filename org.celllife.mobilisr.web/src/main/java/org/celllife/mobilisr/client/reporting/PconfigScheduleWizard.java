package org.celllife.mobilisr.client.reporting;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.view.gxt.MyGXTDateField;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTRadio;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.RepeatInterval;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.gwttime.time.DateTime;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.binding.SimpleComboBoxFieldBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;

/**
 * This class builds a {@link WizardWindow} which can be used to get the input paramters
 * for a {@link ScheduledPconfig}. The supplied {@link ScheduledPconfig} must contain a
 * valid {@link Pconfig}.
 *
 * A listenr can be added to the WizardWindow ({@link WizardWindow#addFinishListener(Listener)}) to be notified when the Wizard is finished.
 * 
 * Changes to the {@link ScheduledPconfig} model are made directly to the model.
 * 
 * @author Simon Kelly
 */
public class PconfigScheduleWizard {
	
	private static Logger logger = Logger.getLogger("PconfigScheduleWizard");

	public static WizardWindow buildWizard(ScheduledPconfig model,
			EntityStoreProvider storeProvider) {
		applyDefaults(model);

		WizardWindow wiz = new WizardWindow();

		WizardCard reportCard = getPconfigCard(model, storeProvider);
		wiz.addCard(reportCard);

		WizardCard scheduleCard = getScheduleCard(model);
		wiz.addCard(scheduleCard);

		return wiz;
	}

	private static void applyDefaults(ScheduledPconfig model) {
		if (model.getId() == null){
			logger.log(Level.FINE, "Setting defaults for SchduledPconfig");
			
			if (model.getRepeatInterval() == null) {
				model.setRepeatInterval(RepeatInterval.Weekly);
			}
	
			if (model.getIntervalCount() == 0) {
				model.setIntervalCount(1);
			}
	
			if (model.getStartDate() == null) {
				model.setStartDate(new Date());
			}
	
			if (model.getEndDate() == null) {
				model.setEndDate(new DateTime().plusWeeks(1).toDate());
			}
	
			if (model.getScheduledFor() == null) {
				String emailAddress = UserContext.getUser().getEmailAddress();
				model.setScheduledFor(emailAddress);
			}
		}
	}

	private static WizardCard getScheduleCard(final ScheduledPconfig model) {
		WizardCard scheduleCard = new WizardCard("Scheduling parameters");
		FormPanel formPanel = new FormPanel();
		formPanel.setLabelWidth(100);

		FormData formData = new FormData("-20");
		FormBinding formBinding = new FormBinding(formPanel);

		// repeat interval
		SimpleComboBox<RepeatInterval> repeatsComboBox = new SimpleComboBox<RepeatInterval>();
		repeatsComboBox.setTriggerAction(TriggerAction.ALL);
		repeatsComboBox.setFieldLabel("Repeats");
		for (RepeatInterval interval : RepeatInterval.values()) {
			repeatsComboBox.add(interval);
		}
		repeatsComboBox.setSimpleValue(RepeatInterval.Daily);

		formBinding.addFieldBinding(new SimpleComboBoxFieldBinding(
				repeatsComboBox, "repeatInterval"));
		formPanel.add(repeatsComboBox, formData);

		// repeat interval number
		MyGXTNumberField repeatNumberField = new MyGXTNumberField("Every:",
				"numberIntervals", false, "", 1);
		repeatNumberField.setPropertyEditorType(Integer.class);
		repeatNumberField.setWidth(50);
		formBinding.addFieldBinding(new FieldBinding(repeatNumberField,
				"intervalCount"));

		LayoutContainer c1 = new LayoutContainer();
		c1.setLayout(new HBoxLayout());
		c1.add(repeatNumberField, new HBoxLayoutData());
		c1.add(new MyGXTLabelField("days/weeks/months"), new HBoxLayoutData(0,
				0, 0, 10));

		AdapterField intervalAdapter = new AdapterField(c1);
		intervalAdapter.setFieldLabel("Every");
		formPanel.add(intervalAdapter, formData);

		// start date
		MyGXTDateField startingDateField = new MyGXTDateField("Starts On",
				"startDate", true, true, new Date());
		startingDateField.setValue(new Date());
		formPanel.add(startingDateField, formData);

		// end date
		final MyGXTDateField endDateField = new MyGXTDateField("Ends On",
				"endDate", true, true, new DateTime().plusDays(1).toDate());

		// end date radio
		final MyGXTRadio neverRadioButton = new MyGXTRadio("Never");
		final MyGXTRadio dateRadioButton = new MyGXTRadio("Date");
		
		boolean isEdit = model.getId() != null;
		logger.log(Level.FINE, "Wizard mode is edit? : " + isEdit);
		logger.log(Level.FINE, "ScheduledPconfig ID = " + model.getId());
		Date endDate = model.getEndDate();
		logger.log(Level.FINE, "ScheduledPconfig endDate = " + endDate);
		if (isEdit){
			dateRadioButton.setValue(endDate != null);
			neverRadioButton.setValue(endDate == null);
		} else {
			dateRadioButton.setValue(true);
		}

		RadioGroup radioGroup = new RadioGroup();
		radioGroup.setFieldLabel("Ends");
		radioGroup.add(neverRadioButton);
		radioGroup.add(dateRadioButton);
		radioGroup.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if (!dateRadioButton.getValue()) {
					endDateField.clear();
					model.setEndDate(null);
				}
				endDateField.setEnabled(dateRadioButton.getValue());
			}
		});
		formPanel.add(radioGroup, formData);
		formPanel.add(endDateField, formData);
		
		if (isEdit){
			if (endDate != null){
				endDateField.setValue(endDate);
			} else {
				endDateField.disable();
			}
		} else {
			endDateField.setValue(new DateTime().plusDays(1).toDate());
		}

		// email
		MyGXTTextField emailField = new MyGXTTextField("Email Report To",
				"scheduledFor", true, "test@test.com");
		formPanel.add(emailField, formData);

		scheduleCard.setFormPanel(formPanel);
		formBinding.autoBind();
		formBinding.bind(convertToBeanModel(model));
		formBinding.setUpdateOriginalValue(true);
		return scheduleCard;
	}

	/**
	 * @param model
	 * @param storeProvider
	 * @return
	 */
	private static WizardCard getPconfigCard(ScheduledPconfig model,
			EntityStoreProvider storeProvider) {
		WizardCard reportCard = new WizardCard("Report parameters");
		reportCard.setHtmlText("Enter the paramters for the scheduled report");
		FormPanel panel = new FormPanel();
		PconfigParamterFieldFactory.createFieldsOnForm(model.getPconfig(),
				panel, storeProvider, false);
		reportCard.setFormPanel(panel);
		return reportCard;
	}

	private static BeanModel convertToBeanModel(Object item) {
		BeanModelLookup lookup = BeanModelLookup.get();
		BeanModelFactory factory = lookup.getFactory(item.getClass());
		if (factory == null)
			return null;

		return factory.createModel(item);
	}
}
