package org.celllife.mobilisr.rest.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.rest.EHRunnable;
import org.celllife.mobilisr.rest.RestUtility;
import org.celllife.mobilisr.service.CampaignRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trg.search.Search;

@Controller("contactRestController")
@RequestMapping("/v2/contacts")
public class ContactRestController extends AbstractRestController{

	private static Logger log = LoggerFactory
			.getLogger(ContactRestController.class);

	@Autowired
	private CampaignRestService restService;
	
	@Autowired
	private ValidatorFactory validatorFactory;

	protected ApiVersion apiVersion;
	
	public ContactRestController() {
		setApiVersion(ApiVersion.getLatest());
	}

	public void setApiVersion(ApiVersion apiVersion) {
		this.apiVersion = apiVersion;
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/{msisdn}")
	@ResponseBody
	public PagedListDto<ErrorDto> updateContactPut(HttpServletRequest request, 
			HttpServletResponse response,
			@PathVariable(value = "msisdn") String oldMsisdn, 
			@RequestBody ContactDto dto) throws IOException {
		log.debug("updateContactPut [oldMsisdn={}] [newMsisdn={}]", oldMsisdn, dto.getMsisdn());
		return updateContact(request, response, oldMsisdn, dto);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{msisdn}")
	@ResponseBody
	public PagedListDto<ErrorDto> updateContactPost(HttpServletRequest request, 
			HttpServletResponse response,
			@PathVariable(value = "msisdn") String oldMsisdn, 
			@RequestBody ContactDto dto) throws IOException {
		log.debug("updateContactPost [oldMsisdn={}] [newMsisdn={}]", oldMsisdn, dto.getMsisdn());
		return updateContact(request, response, oldMsisdn, dto);
	}
	
	PagedListDto<ErrorDto> updateContact(HttpServletRequest request, HttpServletResponse response,
			final String oldMsisdn, final ContactDto contact) throws IOException {
		final User user = userService.getCurrentLoggedInUser();
		
		PagedListDto<ErrorDto> errors = new PagedListDto<ErrorDto>();
		
		ValidationError error = validatorFactory.validateMsisdn(contact.getMsisdn());
		if (error != null){
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			errors.addElement(new ErrorDto(ErrorCode.INVALID_MSISDN, "Contacts mobile number ("+contact.getMsisdn()+") is invalid"));
			return errors;
		}
		
		EHRunnable<Void> runnable = new EHRunnable<Void>(response) {
			@Override
			public Void handled() throws Exception {
				restService.updateContactDetails(user, oldMsisdn, contact);
				return null;
			}
		};
		runnable.run();
		if (runnable.isSuccess()) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.addHeader(LOCATION, buildLocation(request, contact.getMsisdn()));
		}
		
		return errors;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{msisdn}/messages")
	@ResponseBody
	public PagedListDto<MessageStatusDto> getContactMessages(HttpServletResponse response,
			@PathVariable("msisdn") final String msisdn,
			@RequestParam(value = "start", required = false) Date startDate,
			@RequestParam(value = "end", required = false) Date endDate,
			@RequestParam(value = "offset", required = false) Integer offset,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "status", required = false) String status) throws IOException {
		final User user = userService.getCurrentLoggedInUser();

		SmsStatus statusEnum = RestUtility.getMessageStatus(status);

		final Search s = RestUtility.getSearch(offset, limit, 50, 200);
		RestUtility.addSearchFilterEqual(s, SmsLog.PROP_STATUS, statusEnum);
		RestUtility.addSearchFilterGte(s, SmsLog.PROP_DATE_TIME, startDate);
		RestUtility.addSearchFilterLte(s, SmsLog.PROP_DATE_TIME, endDate);

		log.debug("getContactMessages [msisdn={},status={}]",
				new Object[] { msisdn, status});

		PagedListDto<MessageStatusDto> messages = new EHRunnable<PagedListDto<MessageStatusDto>>(response) {
			@Override
			public PagedListDto<MessageStatusDto> handled() throws Exception {
				return restService.getContactMessageLogs(user, msisdn, s, apiVersion);
			}
		}.run();

		return messages;
	}

	void setRestService(CampaignRestService restService) {
		this.restService = restService;
	}
	
	void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}
}
