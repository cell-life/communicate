package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;

public class ModelUtil {
	
	public static BeanModel convertEntityToBeanModel(Object entityobject){
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(entityobject.getClass());
		BeanModel beanModel = beanModelFactory.createModel(entityobject);
		return beanModel;
	}
	
	public static List<BeanModel> convertEntityListToBeanList(List<?> entitylist){
		if (entitylist == null || entitylist.isEmpty()){
			return new ArrayList<BeanModel>();
		}
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(entitylist.get(0).getClass());
		List<BeanModel> list = beanModelFactory.createModel(entitylist);
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> convertBeanListToEntityList(List<BeanModel> beanModelList){
		List<T> entityList = new ArrayList<T>();
		for (BeanModel beanModel : beanModelList) {
			entityList.add((T)beanModel.getBean());
		}
		return entityList;
	}
}
