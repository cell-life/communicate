package org.celllife.mobilisr.service.utility;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<T,V> {

	private HashMap<T,V> map;

	public MapBuilder<T,V> put(T key, V value){
		if (map == null){
			map = new HashMap<T, V>();
		}
		map.put(key, value);
		return this;
	}
	
	public Map<T,V> getMap(){
		return map;
	}
	
	public static MapBuilder<String, Object> stringObject(){
		return new MapBuilder<String, Object>();
	}
}
