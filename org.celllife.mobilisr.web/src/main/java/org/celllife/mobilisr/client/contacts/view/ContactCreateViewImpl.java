package org.celllife.mobilisr.client.contacts.view;



import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.contacts.ContactCreateView;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Anchor;

/**
 * View class for the person create
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
public class ContactCreateViewImpl extends EntityCreateTemplateImpl<Contact> implements ContactCreateView {

	private GenericContactManagementView<ContactGroup> groupManagementView;
	private ContactForm contactForm;
	
	@Override
	public void createView(){
		super.createView();
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		layoutCreateTemplate(Messages.INSTANCE.contactCreateHeader());
		
		groupManagementView = new GroupAddToContactViewImpl();

		contactForm = new ContactForm(formElementContainer);
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}

	
	@Override
	public void showAddGroupPopup(){
		groupManagementView.showPopup(getMsisdn());
		groupManagementView.addListener(Events.Hide, new Listener<WindowEvent>() {
			@Override
			public void handleEvent(WindowEvent be) {
				if (groupManagementView.isDirty()){
					setDirty(true);
				}
			}
		});
	}

	@Override
	public void setFormObject(ViewModel<Contact> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		
		Contact contact = getModel();
		boolean modeCreate = viewEntityModel.isModeCreate();
		if (modeCreate) {
			getHeaderLabel().setText(Messages.INSTANCE.contactCreateHeader());
		} else {
			getHeaderLabel().setText("Contact: " + contact.getMsisdn());
		} 
	}

	@Override
	public Anchor getAnchor() {
		return contactForm.getAnchor();
	}

	@Override
	public String getMsisdn() {
		return contactForm.getMsisdnField().getRawValue();
	}

	@Override
	public GenericContactManagementView<ContactGroup> getAddPopup() {
		return groupManagementView;
	}	

}
