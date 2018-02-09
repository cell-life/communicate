package org.celllife.mobilisr.client.model;

import java.util.Map;

import org.celllife.mobilisr.domain.MobilisrEntity;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;

public class ViewModel<X> {
	
	public static final String VIEW_MESSAGE = "view.message";

	private X modelObject;

	private BeanModelFactory factory;
	Map<String, Object> properties = new FastMap<Object>();
	private boolean dirty;

	public ViewModel() {
	}
	
	public ViewModel(X modelObject) {
		this.modelObject = modelObject;
	}

	public BeanModel getModelData(){
		 createFactory();
		 if (factory == null)
			 return null;
		 
	     BeanModel model = factory.createModel(modelObject);
	     return model;
	}

	private void createFactory() {
		if (factory == null && modelObject != null){
			BeanModelLookup lookup = BeanModelLookup.get();
			factory = lookup.getFactory(modelObject.getClass());
		}
	}

	public X getModelObject() {
		return modelObject;
	}

	public void setModelObject(X modelObject) {
		this.modelObject = modelObject;
	}
	
	public ViewModel<X> setViewMessage(String message) {
		return putProperty(VIEW_MESSAGE, message);
	}
	
	public void clearViewMessage(){
		removeProperty(VIEW_MESSAGE);
	}

	public String getViewMessage() {
		return (String) getProperty(VIEW_MESSAGE);
	}

	public boolean isDirty() {
		return dirty;
	}
	
	//This gets invoked through EntityCreateTemplateImpl's isDirty()
	//which gets set whenever any value in the field changes
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public ViewModel<X> putProperty(String name, Object value){
		if (name != null && !name.isEmpty() && value != null) {
			properties.put(name, value);
		}
		return this;
	}
	
	public Object getProperty(String name){
		return properties.get(name);
	}
	
	public void removeProperty(String name) {
		properties.remove(name);
	}
	
	public boolean getPropertyBoolean(String name){
		return properties.containsKey(name) ? (Boolean) properties.get(name) : false;
	}
	
	public boolean containsProperty(String name){
		return properties.containsKey(name);
	}
	
	/**
	 * Check if a property exists and its value is equal to Boolean.TRUE
	 * 
	 * @param name
	 *            the name of the property to check
	 * @return true if a property with that name exists and the value of the
	 *         property is equal to Boolean.TRUE
	 */
	public boolean isPropertyTrue(String name){
		Object prop = properties.get(name);
		return Boolean.TRUE.equals(prop);			
	}

	public boolean isModeCreate() {
		return !isModeUpdate();
	}
	
	public boolean isModeUpdate() {
		if (modelObject instanceof MobilisrEntity){
			return ((MobilisrEntity) modelObject).isPersisted();
		}
		return false;
	}
}
