package org.celllife.mobilisr.rest.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Campaign;
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

@Controller("campaignRestController")
@RequestMapping("/v2/campaigns")
public class CampaignRestController extends AbstractRestController {

	private static Logger log = LoggerFactory
			.getLogger(CampaignRestController.class);

	@Autowired
	private CampaignRestService restService;

	@Autowired
	private ValidatorFactory validatorFactory;

	protected ApiVersion apiVersion;
	
	public CampaignRestController() {
		setApiVersion(ApiVersion.getLatest());
	}
	
	public void setApiVersion(ApiVersion apiVersion) {
		this.apiVersion = apiVersion;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public PagedListDto<CampaignDto> getCampaignList(
			@RequestParam(value = "offset", required = false) Integer offset,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "status", required = false) String status) {
		User user = userService.getCurrentLoggedInUser();

		CampaignType typeEnum = RestUtility.getCampaignType(type);
		CampaignStatus statusEnum = RestUtility.getCampaignStatus(status);

		Search s = RestUtility.getSearch(offset, limit, 50, 200);
		RestUtility.addSearchFilterEqual(s, Campaign.PROP_TYPE, typeEnum);
		RestUtility.addSearchFilterEqual(s, Campaign.PROP_STATUS, statusEnum);

		PagedListDto<CampaignDto> campaigns = restService.getCampaigns(user, s, apiVersion);
		log.debug("getCampaignList [type={},status={}] return size={}",
				new Object[] { type, status, campaigns.size() });

		return campaigns;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody
	public CampaignDto getCampaign(HttpServletResponse response,
			@PathVariable("id") final Long id) throws IOException {
		final User user = userService.getCurrentLoggedInUser();

		CampaignDto campaign = new EHRunnable<CampaignDto>(response) {
			@Override
			public CampaignDto handled() throws Exception {
				return restService.getCampaign(user, id, apiVersion);
			}
		}.run();
		return campaign;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/contacts")
	@ResponseBody
	public PagedListDto<ErrorDto> addContactsToCampaign(
			HttpServletResponse response, @PathVariable("id") final Long id,
			@RequestBody PagedListDto<ContactDto> contacts) throws IOException {
		final User user = userService.getCurrentLoggedInUser();
		PagedListDto<ErrorDto> errors = new PagedListDto<ErrorDto>();

		errors = RestUtility.checkPageListSizeAndType(contacts,
				ContactDto.class);
		if (!errors.isEmpty()) {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return errors;
		}

		final List<ContactDto> contactList = contacts.getElements();
		errors.addElements(RestUtility.validateContactList(contactList, validatorFactory));

		if (!contactList.isEmpty()) {
			EHRunnable<Void> runnable = new EHRunnable<Void>(response) {
				@Override
				public Void handled() throws Exception {
					restService.addContactsToCampaign(user, id, contactList, apiVersion);
					return null;
				}
			};
			runnable.run();
			if (runnable.isSuccess()) {
				response.setStatus(HttpServletResponse.SC_OK);
			}
		}

		return errors;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{campaignId}/contacts/{msisdn}")
	public void removeContactFromCampaign(HttpServletResponse response,
			@PathVariable("campaignId") final Long campaignId,
			@PathVariable("msisdn") final String msisdn) throws IOException {
		final User user = userService.getCurrentLoggedInUser();

		EHRunnable<Void> runnable = new EHRunnable<Void>(response) {
			@Override
			public Void handled() throws Exception {
				restService.removeContactFromCampaign(user, campaignId, msisdn);
				return null;
			}
		};
		runnable.run();
		if (runnable.isSuccess()) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public PagedListDto<ErrorDto> createNewFixedCampaign(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody final CampaignDto campaign) throws IOException {
		final User user = userService.getCurrentLoggedInUser();
		PagedListDto<ErrorDto> errors = new PagedListDto<ErrorDto>();

		if (campaign.getMessages() == null
				|| campaign.getMessages().size() != 1) {
			errors.addElement(new ErrorDto(ErrorCode.UNSUPPORTED_DATA,
					"Expecting only one message for campaign"));
		}

		if (campaign.getContacts() == null || campaign.getContacts().isEmpty()) {
			errors.addElement(new ErrorDto(ErrorCode.UNSUPPORTED_DATA,
					"No contacts for campaign"));
		} else {
			List<ContactDto> contacts = campaign.getContacts();
			errors.addElements(RestUtility.validateContactList(contacts, validatorFactory));
		}

		if (!errors.isEmpty()) {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return errors;
		}

		EHRunnable<Campaign> runnable = new EHRunnable<Campaign>(response) {
			@Override
			public Campaign handled() throws Exception {
				return restService.createAndRunCampaign(user, campaign, apiVersion);
			}
		};
		Campaign camp = runnable.run();
		if (runnable.isSuccess()) {
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.addHeader(LOCATION, buildLocation(request, camp.getId()));
		}

		return errors;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{campaignId}/messages")
	@ResponseBody
	public PagedListDto<MessageStatusDto> getCampaignMessages(HttpServletResponse response,
			@PathVariable("campaignId") final Long campaignId,
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

		log.debug("getCampaignMessages [campaignId={},status={}]",
				new Object[] { campaignId, status});
		
		PagedListDto<MessageStatusDto> messages = new EHRunnable<PagedListDto<MessageStatusDto>>(response) {
			@Override
			public PagedListDto<MessageStatusDto> handled() throws Exception {
				return restService.getCampaignMessageLogs(user, campaignId, s, apiVersion);
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
