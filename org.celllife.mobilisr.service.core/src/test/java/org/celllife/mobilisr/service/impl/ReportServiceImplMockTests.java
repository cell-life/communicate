package org.celllife.mobilisr.service.impl;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.reporting.ReportingException;
import org.celllife.reporting.service.ReportService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceImplMockTests {
	
	@Mock
	private ReportService staticReportService;
	
	private ReportServiceImpl service;

	@Mock
	private UserService userService;
	
	@Before
	public void setup(){
		service = new ReportServiceImpl();
		service.setUserService(userService);
		service.setStaticReportService(staticReportService);
	}
	
	@Test
	public void testGetReports_noPermsissions(){
		List<Pconfig> reports = getMockReports(null);
		when(staticReportService.getReports()).thenReturn(reports);
		when(userService.getCurrentLoggedInUser()).thenReturn(new User());
		
		List<Pconfig> returnedReports = service.getReports();
		Assert.assertEquals(reports, returnedReports);
	}
	
	@Test
	public void testGetReports_withPermissionsNotInUser(){
		List<Pconfig> reportsWPerm = getMockReports(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS);
		List<Pconfig> reportsWOPerm = getMockReports(null);
		List<Pconfig> allReports = new ArrayList<Pconfig>(reportsWPerm);
		allReports.addAll(reportsWOPerm);
		when(staticReportService.getReports()).thenReturn(allReports);
		when(userService.getCurrentLoggedInUser()).thenReturn(new User());
		
		List<Pconfig> returnedReports = service.getReports();
		Assert.assertEquals(reportsWOPerm, returnedReports);
	}
	
	@Test
	public void testGetReports_withPermissionsInUser(){
		List<Pconfig> reportsWPerm = getMockReports(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS);
		List<Pconfig> reportsWOPerm = getMockReports(null);
		List<Pconfig> allReports = new ArrayList<Pconfig>(reportsWPerm);
		allReports.addAll(reportsWOPerm);
		when(staticReportService.getReports()).thenReturn(allReports);
		User user = new User();
		user.addRole(new Role("test", MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS.name()));
		when(userService.getCurrentLoggedInUser()).thenReturn(user);
		
		List<Pconfig> returnedReports = service.getReports();
		Assert.assertEquals(allReports, returnedReports);
	}
	
	@Test
	public void testGetReports_nullUser(){
		List<Pconfig> reportsWPerm = getMockReports(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS);
		List<Pconfig> reportsWOPerm = getMockReports(null);
		List<Pconfig> allReports = new ArrayList<Pconfig>(reportsWPerm);
		allReports.addAll(reportsWOPerm);
		when(staticReportService.getReports()).thenReturn(allReports);
		
		List<Pconfig> returnedReports = service.getReports();
		Assert.assertEquals(allReports, returnedReports);
	}
	
	@Test
	public void testGenerateReport_autoFill() throws ReportingException{
		Pconfig report = new Pconfig();
		EntityParameter param = new EntityParameter();
		param.setAutofill(true);
		param.setEntityClass(User.class.getName());
		param.setDisplayProperty(User.PROP_USERNAME);
		param.setValueProperty(User.PROP_ID);
		List<Parameter<?>> list = new ArrayList<Parameter<?>>();
		list.add(param);
		report.setParameters(list);
		
		final User user = DomainMockFactory._().on(User.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		
		when(userService.getCurrentLoggedInUser()).thenReturn(user);
		
		service.generateReport(report);
		
		verify(staticReportService).generateReport(argThat(new ArgumentMatcher<Pconfig>() {
			@Override
			public boolean matches(Object argument) {
				List<? extends Parameter<?>> parameters = ((Pconfig)argument).getParameters();
				EntityParameter parameter = (EntityParameter) parameters.get(0);
				
				return parameter.getValue().equals(user.getId().toString())
					&& parameter.getValueLabel().equals(user.getUserName());
			}
		}));
	}

	private List<Pconfig> getMockReports(MobilisrPermission withPerm) {
		List<Pconfig> list = new ArrayList<Pconfig>();
		for (int i = 0; i < 5; i++) {
			Pconfig report = new Pconfig();
			if(withPerm != null){
				Map<String, String> map = new MapBuilder<String, String>().put(
						ServiceAndUIConstants.PROP_REPORT_PERMISSIONS,
						withPerm.name()).getMap();
				report.setProperties(map);
			}
			list.add(report);
		}
		return list;
	}
	

}
