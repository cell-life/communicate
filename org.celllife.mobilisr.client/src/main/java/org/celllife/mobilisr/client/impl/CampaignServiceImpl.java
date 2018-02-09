package org.celllife.mobilisr.client.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.client.CampaignService;
import org.celllife.mobilisr.client.command.DeleteCommand;
import org.celllife.mobilisr.client.command.GetCommand;
import org.celllife.mobilisr.client.command.PostCommand;
import org.celllife.mobilisr.client.command.RestCommandFactory;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;

import com.sun.jersey.api.client.ClientResponse.Status;

public class CampaignServiceImpl extends BaseRestService implements CampaignService {

	public CampaignServiceImpl(RestCommandFactory factory, ValidatorFactory vfactory) {
		super(factory, vfactory);
	}

	@Override
	public void addContactToCampaign(Long campaignId, ContactDto contact)
			throws RestCommandException {
		addContactsToCampaign(campaignId, Arrays.asList(contact));
	}
	
	@Override
	public void addContactsToCampaign(Long campaignId, List<ContactDto> contacts)
			throws RestCommandException {
		validateContacts(contacts);
		
		PagedListDto<ContactDto> listDto = new PagedListDto<ContactDto>(contacts);
		PostCommand command = getCommandFactory().getPostCommand("campaigns/{campaignId}/contacts/", listDto, campaignId);
		
		@SuppressWarnings("unchecked")
		PagedListDto<ErrorDto> errors = command.execute(PagedListDto.class, Status.OK.getStatusCode());
		if (!errors.isEmpty()){
			throw new RestCommandException(errors);
		}
	}
	
	@Override
	public CampaignDto getCampaignDetails(Long campaignId)
			throws RestCommandException {
		return (CampaignDto) getCommandFactory()
				.getCommandGet("campaigns/{id}", campaignId).execute(
						CampaignDto.class);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns() throws RestCommandException {
		return getCampaigns(null, null, null, null);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(Integer offset, Integer limit)
			throws RestCommandException {
		return getCampaigns(null, null, offset, limit);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type)
			throws RestCommandException {
		return getCampaigns(type, null, null, null);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignStatus status)
			throws RestCommandException {
		return getCampaigns(null, status, null, null);
	}

	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type,
			Integer offset, Integer limit) throws RestCommandException {
		return getCampaigns(type, null, offset, limit);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignStatus status,
			Integer offset, Integer limit) throws RestCommandException {
		return getCampaigns(null, status, offset, limit);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type,
			CampaignStatus status) throws RestCommandException {
		return getCampaigns(type, status, null, null);
	}
	
	@Override
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type,
			CampaignStatus status, Integer offset, Integer limit)
			throws RestCommandException {
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();
		if (type != null)
			parameters.put("type", type);
		if (status != null)
			parameters.put("status", status);
		if (offset != null)
			parameters.put("offset", offset);
		if (limit != null)
			parameters.put("limit", limit);
		
		return getCampaigns(parameters);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PagedListDto<CampaignDto> getCampaigns(Map<String, Object> parameters)
			throws RestCommandException {
		GetCommand command = getCommandFactory().getCommandGet("campaigns");
		command.setQueryParameters(parameters);
		return (PagedListDto<CampaignDto>) command.execute(PagedListDto.class);
	}

	@Override
	public void removeContactFromCampaign(Long campaignId, String msisdn)
			throws RestCommandException {
		String validMsisdn = validateMsisdn(msisdn);
		
		DeleteCommand command = getCommandFactory().getDeleteCommand(
				"campaigns/{campaignId}/contacts/{msisdn}", campaignId, validMsisdn);
		command.execute(Void.class, Status.NO_CONTENT.getStatusCode());
	}

	@Override
	public void removeContactFromCampaign(Long campaignId, ContactDto contact)
			throws RestCommandException {
		String validMsisdn = validateMsisdn(contact.getMsisdn());
		removeContactFromCampaign(campaignId, validMsisdn);
	}
	
	@Override
	public void createNewCampaign(String name, String description, String message, List<ContactDto> contacts) throws RestCommandException{
		CampaignDto dto = new CampaignDto();
		dto.setName(name);
		dto.setDescription(description);
		MessageDto messageDto = new MessageDto();
		messageDto.setText(message);
		dto.setMessages(Arrays.asList(messageDto));
		dto.setContacts(contacts);
		createNewCampaign(dto);
	}
	
	@Override
	public long createNewCampaign(CampaignDto campaign)
			throws RestCommandException {
		
		validateContacts(campaign.getContacts());
		
		PostCommand command = getCommandFactory().getPostCommand("campaigns", campaign);
		command.execute(Void.class);

		String location = command.getHeader("Location");
		if (location == null){
			throw new RestCommandException("No location header specified in response");
		}
		
		int slash = location.lastIndexOf("/");
		String campaignId = location.substring(slash+1, location.length());
		return Long.valueOf(campaignId);
	}
}
