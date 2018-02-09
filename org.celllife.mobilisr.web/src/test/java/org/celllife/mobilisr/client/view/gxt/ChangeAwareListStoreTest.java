package org.celllife.mobilisr.client.view.gxt;

import org.celllife.mobilisr.client.modelcompare.ContactModelComparer;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.test.MobGWTMockUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;

public class ChangeAwareListStoreTest {

	private ChangeAwareListStore<BeanModel> listStore;

	@Before
	public void setup(){
		listStore = new ChangeAwareListStore<BeanModel>(new ContactModelComparer());
		MobGWTMockUtilities.disarm();
	}
	
	@Test
	public void testAdd(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		listStore.add(convertToBeanModel(contact));
		
		Assert.assertEquals(1, listStore.getCount());
		Assert.assertEquals(1, listStore.getAdded().size());
	}
	
	@Test
	public void testRemove_not_in_list(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		listStore.remove(convertToBeanModel(contact));
		
		Assert.assertEquals(0, listStore.getCount());
		Assert.assertEquals(0, listStore.getRemoved().size());
	}
	
	@Test
	public void testRemove_in_list(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		// TODO investigate not working with different beanModels (uses object equals when
		//	removing from array list which is backing store
		BeanModel beanModel = convertToBeanModel(contact);
		listStore.addIgnoreChange(beanModel);
		listStore.remove(beanModel);
		
		Assert.assertEquals(0, listStore.getCount());
		Assert.assertEquals(1, listStore.getRemoved().size());
	}
	
	@Test
	@Ignore("Expected to work, remove list shouldn't contain items that were added")
	public void testRemove_in_added_list(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		BeanModel beanModel = convertToBeanModel(contact);
		listStore.add(beanModel);
		listStore.remove(beanModel);
		
		Assert.assertEquals(0, listStore.getCount());
		Assert.assertEquals(0, listStore.getAdded().size());
		Assert.assertEquals(0, listStore.getRemoved().size());
	}
	
	@Test
	@Ignore("list store does not use its comparator when adding or removing")
	public void testRemove_add_remove_differentBeanModel(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		BeanModel beanModel1 = convertToBeanModel(contact);
		BeanModel beanModel2 = convertToBeanModel(contact);
		listStore.add(beanModel1);
		listStore.remove(beanModel2);
		
		Assert.assertEquals(0, listStore.getCount());
		Assert.assertEquals(0, listStore.getAdded().size());
		Assert.assertEquals(0, listStore.getRemoved().size());
	}
	
	@Test
	@Ignore("list store does not use its comparator when adding or removing")
	public void testRemove_add_twice(){
		Assert.assertTrue(listStore.getModels().isEmpty());
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		
		BeanModel beanModel1 = convertToBeanModel(contact);
		BeanModel beanModel2 = convertToBeanModel(contact);
		listStore.add(beanModel1);
		listStore.add(beanModel2);
		
		Assert.assertEquals(1, listStore.getCount());
		Assert.assertEquals(2, listStore.getAdded().size());
		Assert.assertEquals(0, listStore.getRemoved().size());
	}
	
	public BeanModel convertToBeanModel(MobilisrEntity entity){
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(entity.getClass());
		return beanModelFactory.createModel(entity);
	}
}
