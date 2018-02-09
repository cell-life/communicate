package org.celllife.mobilisr.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import junit.framework.Assert;

import org.celllife.mobilisr.domain.Setting;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.gwt.SettingViewModel;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.pconfig.model.Parameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingServiceImplTests extends AbstractServiceTest {

	@Autowired
	private SettingService service;
	
	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	@Test
	public void testSettings_null_values() {
		for (SettingsEnum setting : SettingsEnum.values()) {
			Parameter<?> config = setting.getConfig();
			Object value = service.getSettingValue(setting);
			errorCollector.checkThat(value, equalTo((Object) config.getDefaultValue()));
		}
	}
	
	@Test
	public void testSettings_load() {
		SettingsEnum setting = SettingsEnum.CREDIT_NOTIFICATIONS_EMAIL;
		@SuppressWarnings("unchecked")
		Parameter<String> config = (Parameter<String>) setting.getConfig();
		String value = "test@test.com";
		config.setValue(value);
		
		Setting s = new Setting(setting.getSettingName(), YamlUtils.dumpParameterList(config));
		getGeneralDao().save(s);
		
		String settingValue = service.getSettingValue(setting);
		Assert.assertEquals(value, settingValue);
	}
	
	@Test
	public void testSettings_save() {
		SettingsEnum setting = SettingsEnum.CREDIT_NOTIFICATIONS_EMAIL;
		@SuppressWarnings("unchecked")
		Parameter<String> config = (Parameter<String>) setting.getConfig();
		String expected = "test@test.com";
		config.setValue(expected);

		Setting s = new Setting(setting.getSettingName(), "");
		SettingViewModel model = new SettingViewModel(s, config);
		service.saveSetting(model);
		
		Setting loaded = service.getSetting(setting.getSettingName());
		String valueString = loaded.getValueString();
		Assert.assertEquals(YamlUtils.dumpParameterList(config), valueString);
		
		String value = service.getSettingValue(setting);
		Assert.assertEquals(expected, value);
	}
	
	@Test
	public void testSettings_not_in_db() {
		SettingsEnum setting = SettingsEnum.CREDIT_NOTIFICATIONS_EMAIL;
		Setting s = service.getSetting(setting.getSettingName());
		
		getGeneralDao().remove(s);
		
		String value = service.getSettingValue(setting);
		Assert.assertEquals(setting.getSettingValue(), value);
	}
}