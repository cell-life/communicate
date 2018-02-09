package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Setting;
import org.celllife.pconfig.model.Parameter;

public class SettingViewModel implements MobilisrEntity, Serializable {

	private static final long serialVersionUID = 2812777496763283280L;

	public static final String PROP_NAME = "name";
	public static final String PROP_VALUE = "value";
	
	private Setting setting;

	private Parameter<?> config;
	
	public SettingViewModel() {
	}

	public SettingViewModel(Setting setting, Parameter<?> config) {
		this.setting = setting;
		this.config = config;
	}

	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

	public Parameter<?> getConfig() {
		return config;
	}

	public void setConfig(Parameter<?> config) {
		this.config = config;
	}
	
	public String getName(){
		return setting.getName();
	}
	
	public Object getValue(){
		Object value = config.getValue();
		if (value == null){
			value = config.getDefaultValue();
		}
		return value;
	}

	@Override
	public Long getId() {
		return setting.getId();
	}

	@Override
	public void setId(Long id) {
	}

	@Override
	public String getIdentifierString() {
		return setting.getIdentifierString();
	}

	@Override
	public boolean isPersisted() {
		return setting.isPersisted();
	}

}
