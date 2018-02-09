package org.celllife.mobilisr.rest.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.DateValidator;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.MsisdnValidator;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.CampaignRestService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.trg.search.Search;

@RunWith(MockitoJUnitRunner.class)
public class CampaignRestControllerTest {

	private CampaignRestController controller;

	@Mock
	private CampaignRestService restService;

	@Mock
	private UserService userService;

	private User user;

	@Mock
	private ValidatorFactory validatorFactory;

	@Before
	public void setup() {
		controller = new CampaignRestController();
		controller.setRestService(restService);
		controller.setUserService(userService);
		controller.setValidatorFactory(validatorFactory);
		
		user = DomainMockFactory._().on(User.class).create();
		when(userService.getCurrentLoggedInUser()).thenReturn(user);
		
		when(validatorFactory.getMsisdnValidator()).thenReturn(
				new MsisdnValidator(Arrays.asList(new MsisdnRule("SA", "27",
						"^27[1-9][0-9]{8}$"))));

        when(validatorFactory.getDateValidator()).thenReturn(new DateValidator());
	}
	
	@Test
	public void testGetCampaign() throws MobilisrException, IOException {
		// setup mocking
		MockHttpServletResponse response = new MockHttpServletResponse();
		CampaignDto value = DtoMockFactory._().on(CampaignDto.class).create();
		when(restService.getCampaign(eq(user), eq(13L), eq(ApiVersion.getLatest()))).thenReturn(value);

		// call test method
		CampaignDto dto = controller.getCampaign(response, 13L);

		// verify results
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		Assert.assertEquals(value.getId(), dto.getId());
		Assert.assertEquals(value.getName(), dto.getName());
	}
	
	@Test
	public void testGetCampaign_restricted() throws MobilisrException,IOException {
		// setup mocking
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(restService.getCampaign(eq(user), eq(13L), eq(ApiVersion.getLatest()))).thenThrow(
				new ObjectNotFoundException());

		// call test method
		CampaignDto dto = controller.getCampaign(response, 13L);

		// verify results
		Assert.assertNull(dto);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND,
				response.getStatus());
	}

	@Test
	public void testGetCampaign_notfound() throws IOException, MobilisrException {
		// setup mocking
		MockHttpServletResponse response = new MockHttpServletResponse();
		String message = "object not found";
		when(restService.getCampaign(eq(user), eq(13L), eq(ApiVersion.getLatest()))).thenThrow(
				new ObjectNotFoundException(message));

		// call test method
		CampaignDto dto = controller.getCampaign(response, 13L);

		// verify results
		Assert.assertNull(dto);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND,
				response.getStatus());
		Assert.assertEquals(message, response.getErrorMessage());
	}

	@Test
	public void testGetCampaignList_basic() {
		// setup mocking
		List<CampaignDto> expected = DtoMockFactory._().on(CampaignDto.class)
				.create(7);
		when(restService.getCampaigns(eq(user), any(Search.class), eq(ApiVersion.getLatest()))).thenReturn(
				new PagedListDto<CampaignDto>(expected));

		// call test method
		PagedListDto<CampaignDto> actual = controller.getCampaignList(null,
				null, null, null);

		// verify results
		Assert.assertEquals(expected, actual.getElements());
	}

	@Test
	public void testGetCampaignList_full() {
		// setup mocking
		List<CampaignDto> expected = DtoMockFactory._().on(CampaignDto.class)
				.create(7);

		when(restService.getCampaigns(eq(user), any(Search.class), eq(ApiVersion.getLatest()))).thenReturn(
				new PagedListDto<CampaignDto>(expected));

		// call test method
		PagedListDto<CampaignDto> actual = controller.getCampaignList(3, 9,
				CampaignType.FIXED.name(), CampaignStatus.SCHEDULED.name());

		// verify results
		Assert.assertEquals(expected, actual.getElements());

		Search s = new Search();
		s.setMaxResults(9).setFirstResult(3)
				.addFilterEqual(Campaign.PROP_TYPE, CampaignType.FIXED)
				.addFilterEqual(Campaign.PROP_STATUS, CampaignStatus.SCHEDULED);
		verify(restService).getCampaigns(user, s, ApiVersion.getLatest());
	}
	
	@Test
	public void testAddContactsToCampaign() throws IOException, JAXBException,
		MobilisrException {
		// setup mocking
		final List<ContactDto> dtos = DtoMockFactory._().on(ContactDto.class)
				.withMode(DtoMockFactory.MODE_GET).create(3);

		MockHttpServletResponse response = new MockHttpServletResponse();
		
		long id = 13L;
		
		// call test method
		PagedListDto<?> actual = controller.addContactsToCampaign(response,
				id, new PagedListDto<ContactDto>(dtos));

		// verify results
		verify(restService).addContactsToCampaign(eq(user), eq(id), argThat(new ArgumentMatcher<List<ContactDto>>() {
			@Override
			public boolean matches(Object list) {
				return ((List<?>) list).size() == dtos.size();
			}
		}), eq(ApiVersion.getLatest()));
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		Assert.assertTrue(actual.getElements().isEmpty());
	}
	
	@Test
	public void testAddContactsToCampaign_campaignNotRunning() throws IOException, JAXBException,
		MobilisrException {
		// setup mocking
		final List<ContactDto> dtos = DtoMockFactory._().on(ContactDto.class)
				.withMode(DtoMockFactory.MODE_GET).create(3);

		MockHttpServletResponse response = new MockHttpServletResponse();
		
		long id = 13L;
		doThrow(new CampaignStateException())
				.when(restService)
				.addContactsToCampaign(eq(user), eq(id),
						anyListOf(ContactDto.class), eq(ApiVersion.getLatest()));		
		// call test method
		PagedListDto<?> actual = controller.addContactsToCampaign(response,
				id, new PagedListDto<ContactDto>(dtos));

		// verify results
		verify(restService).addContactsToCampaign(eq(user), eq(id), argThat(new ArgumentMatcher<List<ContactDto>>() {
			@Override
			public boolean matches(Object list) {
				return ((List<?>) list).size() == dtos.size();
			}
		}), eq(ApiVersion.getLatest()));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertTrue(actual.getElements().isEmpty());
	}
	
	@Test
	public void testAddContactsToCampaign_incorrectTypeDataPost() throws IOException, JAXBException,
		MobilisrException {
		// setup mocking
		List<CampaignDto> dtos = DtoMockFactory._().on(CampaignDto.class).create(3);
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		// call test method
		// SK: Suppress warnings, since we are deliberately using the wrong type here.
		@SuppressWarnings({ "rawtypes", "unchecked" })
		PagedListDto<?> actual = controller.addContactsToCampaign(response,	13L, new PagedListDto(dtos));

		// verify results
		Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
		Assert.assertEquals(1, actual.size());
		ErrorDto error = (ErrorDto) actual.getElements().get(0);
		Assert.assertEquals(ErrorCode.UNSUPPORTED_DATA, error.getErrorCode());
	}
	
	@Test
	public void testAddContactsToCampaign_emptyListPost() throws IOException, JAXBException,
		MobilisrException {
		// setup mocking
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		// call test method
		PagedListDto<?> actual = controller.addContactsToCampaign(response,
				13L, new PagedListDto<ContactDto>());

		// verify results
		Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
		Assert.assertEquals(1, actual.size());
		ErrorDto error = (ErrorDto) actual.getElements().get(0);
		Assert.assertEquals(ErrorCode.EMPTY_LIST, error.getErrorCode());
	}
	
	@Test
	public void testAddContactsToCampaign_invalidMsisdn() throws IOException, JAXBException,
		MobilisrException {
		// setup mocking
		final List<ContactDto> dtos = DtoMockFactory._().on(ContactDto.class)
				.withMode(DtoMockFactory.MODE_GET).create(3);
		dtos.get(0).setMsisdn("invalidmsisdn");

		MockHttpServletResponse response = new MockHttpServletResponse();
		
		// call test method
		long id = 13L;
		PagedListDto<?> actual = controller.addContactsToCampaign(response, id,
				new PagedListDto<ContactDto>(dtos));

		// verify results
		verify(restService).addContactsToCampaign(eq(user), eq(id), argThat(new ArgumentMatcher<List<ContactDto>>() {
			@Override
			public boolean matches(Object list) {
				return ((List<?>) list).size() == dtos.size()-1;
			}
		}), eq(ApiVersion.getLatest()));
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		Assert.assertEquals(1, actual.size());
		ErrorDto error = (ErrorDto) actual.getElements().get(0);
		Assert.assertEquals(ErrorCode.INVALID_MSISDN, error.getErrorCode());
	}
	
	@Test
	public void testCreateNewCampaign_emptyMessages() throws IOException{
		// setup mocking
		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		
		CampaignDto dto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		dto.setMessages(null);
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.createNewFixedCampaign(request, response, dto);
		
		// verify
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(ErrorCode.UNSUPPORTED_DATA, errors.getElements().get(0).getErrorCode());
	}
	
	@Test
	public void testCreateNewCampaign_emptyContacts() throws IOException{
		// setup mocking
		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		
		CampaignDto dto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		dto.setContacts(null);
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.createNewFixedCampaign(request, response, dto);

		// verify
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(ErrorCode.UNSUPPORTED_DATA, errors.getElements().get(0).getErrorCode());
	}
	
	@Test
	public void testCreateNewCampaign_invalidContacts() throws IOException{
		// setup mocking
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		CampaignDto dto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		dto.getContacts().get(0).setMsisdn("invalid");
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.createNewFixedCampaign(request, response, dto);

		// verify
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(ErrorCode.INVALID_MSISDN, errors.getElements().get(0).getErrorCode());
		Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
	}
	
	@Test
	public void testCreateNewCampaign_success() throws IOException, MobilisrException{
		// setup mocking
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/campaigns");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		CampaignDto dto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		Campaign camp = DomainMockFactory._().on(Campaign.class).withMode(DomainMockFactory.MODE_LOAD).create();
		when(restService.createAndRunCampaign(user, dto, ApiVersion.getLatest())).thenReturn(camp);
		
		// call test method
		PagedListDto<ErrorDto> errors = controller.createNewFixedCampaign(request, response, dto);

		// verify
		Assert.assertEquals(0, errors.size());
		Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
		Assert.assertEquals("http://localhost:80/api/campaigns/" + camp.getId(),response.getHeader("Location"));
	}

}

