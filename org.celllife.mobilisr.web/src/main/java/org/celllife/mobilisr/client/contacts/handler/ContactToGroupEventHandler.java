package org.celllife.mobilisr.client.contacts.handler;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.modelcompare.ContactModelComparer;
import org.celllife.mobilisr.client.view.gxt.ChangeAwareListStore;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ContactToGroupEventHandler extends GenericContactManagementEventHandler<Contact> {

	private GenericContactManagementView<Contact> view;
	private static ContactModelComparer modelComparer = new ContactModelComparer();

	public ContactToGroupEventHandler(GenericContactManagementView<Contact> view) {
		super(view, modelComparer);
		this.view = view;
	}

	@Override
	public void handleFilterInRightGridStore(String filterValue) {
		view.filterInRightGridStore(filterValue, Contact.PROP_MSISDN);
	}

	@Override
	public void handleObjectDisplaySaveMsg(Contact entityObject) {
		MessageBoxWithIds.info("", "Contact " + entityObject.getMsisdn() + " successfully saved", null);
	}

	@Override
	public void handleObjectDisplayFailMessage(Throwable error) {
		MessageBoxWithIds.alert("Error saving", error.getMessage(), null);
	}

	@Override
	public void handleRPCLoadListAllEntities(ViewModel<? extends MobilisrEntity> vem, String searchVal,
			Object loadConfig, AsyncCallback<PagingLoadResult<Contact>> callback) {
		PagingLoadConfig pagingLoadConfig = getLoadingConfig(searchVal, Contact.PROP_MSISDN, loadConfig);
		MobilisrEntity entity = vem.getModelObject();
		Organization org = UserContext.getUser().getOrganization();
		if (entity != null && entity instanceof Campaign){
			org = ((Campaign)entity).getOrganization();
		}
		getContactsServiceAsync().listAllContactsForOrganization(org,pagingLoadConfig, callback);
	}

	@Override
	public void handleRPCLoadListAssociationForObject(ViewModel<? extends MobilisrEntity> viewEntityModel, Object loadConfig,
			AsyncCallback<PagingLoadResult<Contact>> callback) {
		PagingLoadConfig pagingLoadConfig = getLoadingConfig(getSelEntitySearch(), Contact.PROP_MSISDN, loadConfig);
		ContactGroup contactGroup = (ContactGroup) viewEntityModel.getModelObject();

		getContactsServiceAsync().listAllContactsForGroup(contactGroup, pagingLoadConfig, callback);
	}

	@Override
	public void handleRPCObjectSave(ViewModel<? extends MobilisrEntity> vem, String newEntityFieldValue, AsyncCallback<Contact> callback) {
		Contact contact = new Contact();
		contact.setMsisdn(newEntityFieldValue);
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, null, null, false, false);

		Organization organization = null;
		if (vem!=null) {
			MobilisrEntity entity = vem.getModelObject();
			if (entity != null && entity instanceof Campaign) {
				organization = ((Campaign)entity).getOrganization();
			}
			else if (entity != null && entity instanceof ContactGroup) {
				organization = ((ContactGroup)entity).getOrganization();
			}
		}

		if (organization == null){
			organization = UserContext.getUser().getOrganization();
		}

		getContactsServiceAsync().saveOrUpdateContact(organization, contactModel, callback);
	}

	public ContactGroupContactViewModel getContactModel(ContactGroup contactGroup){

		boolean isAddAll = isAddAllSelected();
		boolean isRemoveAll = isRemoveAllSelected();
		ChangeAwareListStore<BeanModel> selectedEntityStore = getSelectedEntityStore();
		List<BeanModel> addedGroupList = selectedEntityStore.getAdded();
		List<BeanModel> removedGroupList = selectedEntityStore.getRemoved();
		List<Contact> addedGroupsList = convertModelListToGroupList(addedGroupList);
		List<Contact> removedGroupsList = convertModelListToGroupList(removedGroupList);
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(contactGroup, addedGroupsList, removedGroupsList,
																									isAddAll, isRemoveAll);
		return contactModel;
	}

	private List<Contact> convertModelListToGroupList(List<BeanModel> beanModels){
		List<Contact> contacts = new ArrayList<Contact>();

		for (BeanModel beanModel : beanModels) {
			Contact contact = beanModel.getBean();
			contacts.add(contact);
		}
		return contacts;

	}
}
