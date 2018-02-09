package org.celllife.mobilisr.client.view.gxt;

import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;

public class CsvFileDialog extends FormDialog {
	
	private static final Logger log = Logger.getLogger(CsvFileDialog.class.getName());
	
	protected Listener<FormEvent> submitListener;

	private LabelField infoLabel;

	public CsvFileDialog() {
		buildDialog();
		setHideOnSubmit(false);
	}

	@Override
	protected void createFormContents(final FormPanel formPanel) {
		formPanel.setLayout(new RowLayout());
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setAction(GWT.getModuleBaseURL() + "readCsv");
		formPanel.addListener(Events.Submit, new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent fe) {
				hide();
				if (submitListener != null){
					log.finer("Fire for submit event");
					submitListener.handleEvent(fe);
				} else {
					log.finer("Fire for submit event: no submit listener");
				}
			}
		});
		
		infoLabel = new LabelField();
		add(infoLabel);
		
		final FileUploadField newField = new FileUploadField();
		newField.setWidth(250);
		newField.setName("csvFile");
		newField.setId("csvFile");
		newField.setFieldLabel("File");
		newField.setValidator(new Validator() {
			@Override
			public String validate(Field<?> field, String value) {
				String fileNm = value.toLowerCase();
				if (!fileNm.endsWith(".csv")) {
					return "Only .csv files are accepted";
				}
				return null;
			}
		});

		formPanel.add(newField);
		
		getSaveButton().setText("Upload");
		getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				formPanel.submit();
			}
		});
	}
	
	public void setSubmitListener(Listener<FormEvent> submitListener) {
		this.submitListener = submitListener;
	}
	
	public void setInfoText(String text){
		infoLabel.setText(text);
	}

}
