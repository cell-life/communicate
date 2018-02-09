package org.celllife.mobilisr.rest.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.MsisdnValidator;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.CampaignRestService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ContactRestControllerTest {

	private ContactRestController controller;

	@Mock
	private CampaignRestService restService;

	@Mock
	private UserService userService;
	
	@Mock
	private ValidatorFactory validatorFactory;

	private User user;

	@Before
	public void setup() {
		controller = new ContactRestController();
		controller.setRestService(restService);
		controller.setUserService(userService);
		controller.setValidatorFactory(validatorFactory);
		
		user = DomainMockFactory._().on(User.class).create();
		when(userService.getCurrentLoggedInUser()).thenReturn(user);
		
		when(validatorFactory.getMsisdnValidator()).thenReturn(
				new MsisdnValidator(Arrays.asList(new MsisdnRule("SA", "27",
						"^27[1-9][0-9]{8}$"))));
		
	}
	
	
	@Test
	public void testUpdateContact() throws IOException, MobilisrException {
		// setup mocking
		final ContactDto dto = DtoMockFactory._().on(ContactDto.class).create();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		String oldMsisdn = "123";
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.updateContact(request, response,
				oldMsisdn, dto);

		// verify results
		verify(restService).updateContactDetails(eq(user), eq(oldMsisdn), eq(dto));
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		Assert.assertTrue(errors.isEmpty());
		Assert.assertNotNull(response.getHeader("Location"));
		Assert.assertTrue(((String)response.getHeader("Location")).endsWith(dto.getMsisdn()));;
	}
	
	@Test
	public void testUpdateContact_badMsisdn() throws IOException, MobilisrException {
		// setup mocking
		final ContactDto dto = DtoMockFactory._().on(ContactDto.class).create();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		dto.setMsisdn("invalid msisdn");
		String oldMsisdn = "27254561236"; 
		
		when(validatorFactory.validateMsisdn(any(String.class))).thenReturn(
				new ValidationError(ErrorCode.INVALID_MSISDN, ""));
		
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.updateContact(request,response,
				oldMsisdn, dto);

		// verify results
		verify(restService, never()).updateContactDetails(eq(user), eq(oldMsisdn), eq(dto));
		Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
		Assert.assertEquals(1, errors.size());
	}
	
	@Test
	public void testUpdateContact_nonExistantContact() throws IOException, MobilisrException {
		// setup mocking
		final ContactDto dto = DtoMockFactory._().on(ContactDto.class).create();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		String oldMsisdn = "123"; 
		doThrow(new ObjectNotFoundException()).when(restService).updateContactDetails(eq(user), eq(oldMsisdn), eq(dto));
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.updateContact(request,response,
				oldMsisdn, dto);

		// verify results
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
		Assert.assertTrue(errors.isEmpty());
	}
}

