package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.service.AdminService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ValidatorFactory")
public class ServiceValidatorFactoryImpl extends ValidatorFactoryImpl implements InitializingBean{
	
	@Autowired
	private AdminService adminService;
	
	public void refreshCountryRules() {
		List<NumberInfo> numberInfoList = adminService.getNumberInfoList();
		ArrayList<MsisdnRule> list = new ArrayList<MsisdnRule>(numberInfoList.size());
		for (NumberInfo numberInfo : numberInfoList) {
			list.add(numberInfo.getMsisdnRule());
		}
		setCountryRules(list);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refreshCountryRules();
	}
}
