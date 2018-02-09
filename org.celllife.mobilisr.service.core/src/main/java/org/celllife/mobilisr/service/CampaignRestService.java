package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;

import com.trg.search.Search;

public interface CampaignRestService {

	public abstract PagedListDto<CampaignDto> getCampaigns(User user, Search s, ApiVersion ver);

	public abstract CampaignDto getCampaign(User user, Long id, ApiVersion ver) throws MobilisrException;

	public abstract void addContactsToCampaign(User user, Long id,	List<ContactDto> contacts, ApiVersion ver) throws MobilisrException;

	public abstract void removeContactFromCampaign(User user, Long campaignId,
			String msisdn) throws MobilisrException;

	public abstract Campaign createAndRunCampaign(User user, CampaignDto campaign, ApiVersion ver) throws MobilisrException;

	public abstract void updateContactDetails(User user, String oldMsisdn, ContactDto contact) throws MobilisrException;

	public abstract PagedListDto<MessageStatusDto> getCampaignMessageLogs(User user, Long campaignId, Search s, ApiVersion ver) throws MobilisrException;

	public abstract PagedListDto<MessageStatusDto> getContactMessageLogs(User user, String msisdn, Search s, ApiVersion ver) throws MobilisrException;

}