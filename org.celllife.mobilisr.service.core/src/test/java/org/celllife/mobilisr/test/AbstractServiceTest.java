package org.celllife.mobilisr.test;

import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.service.impl.ServiceValidatorFactoryImpl;
import org.celllife.mobilisr.service.security.MobilisrSecurityService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AbstractServiceTest extends AbstractDBTest {

	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Autowired
	private MobilisrSecurityService securityService;
	
	@Before
	public void refreshNumberInfoList() throws Exception{
		ServiceValidatorFactoryImpl targetObject = TestUtils.getTargetObject(validatorFactory, ServiceValidatorFactoryImpl.class);
		targetObject.refreshCountryRules();
	}
	
	@Override
	protected void login(String username, String password) {
		UserDetails userDetails = securityService.loadUserByUsername(username);
		
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userDetails, password));
	}
}
