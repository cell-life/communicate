package org.celllife.mobilisr.client.contacts.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.GroupCreateView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextArea;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.Anchor;

public class GroupCreateViewImpl extends EntityCreateTemplateImpl<ContactGroup> implements GroupCreateView{

	private MyGXTTextField groupName;
	private MyGXTTextArea groupDescription;

	private Anchor anchor;
	private AdapterField adapterField;

	private GenericContactManagementView<Contact> contactManagementView;

	private ContactGroup contactGroup;

	@Override
	public void createView() {
		super.createView();
		
		layoutCreateTemplate(Messages.INSTANCE.groupCreateHeader());
		
		groupName = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.groupName(), ContactGroup.PROP_GROUP_NAME, false,
				"e.g. Group A");
		groupDescription = new MyGXTTextArea(Messages.INSTANCE.groupDescription(),
				ContactGroup.PROP_GROUP_DESCRIPTION, true, "" );
		anchor = new Anchor(Messages.INSTANCE.groupManageContacts());
		adapterField = new AdapterField(anchor);
		contactManagementView = new ContactAddToGroupViewImpl();

		displayGroupCreateForm();
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}

	private void displayGroupCreateForm() {
		final FormData formData = new FormData();
		formData.setMargins(new Margins(10));

		groupName.setMinLength(3);
		formElementContainer.add(groupName, formData);
		formElementContainer.add(groupDescription, formData);
		formElementContainer.add(adapterField, formData);
	}

	@Override
	public void setFormObject(ViewModel<ContactGroup> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		contactGroup = getModel();

		setErrorMessage("");
		
		boolean modeUpdate = viewEntityModel.isModeUpdate();

		if (modeUpdate) {
			getHeaderLabel().setText("ContactGroup: " + contactGroup.getGroupName());
		} else {
			getHeaderLabel().setText(Messages.INSTANCE.groupCreateHeader());
		}
	}

	@Override
	public GenericContactManagementView<Contact> getAddPopup() {
		return contactManagementView;
	}

	@Override
	public Anchor getAnchor() {
		return anchor;
	}

	@Override
	public String getGroupName() {
		return groupName.getValue();
	}

	@Override
	public void showAddContactPopup(String groupName) {
		contactManagementView.showPopup(groupName);
		contactManagementView.addListener(Events.Hide, new Listener<WindowEvent>() {
			@Override
			public void handleEvent(WindowEvent be) {
				if (contactManagementView.isDirty()){
					setDirty(true);
				}
			}
		});
	}
}
