package org.celllife.mobilisr.client;

import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;

/**
 * Campaign service for interfacing with Mobilisr REST Campaign API's.
 * 
 * @see http://dev.cell-life.org/confluence/display/mobilisr/REST+API+Requirements
 * @author Simon Kelly <simon@cell-life.org>
 */
public interface CampaignService {
	
	/**
	 * Get all campaigns
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns() throws RestCommandException;
	
	/**
	 * Get all campaigns (paged)
	 * 
	 * @param offset paging offset (first result)
	 * @param limit paging limit (max results)
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(Integer offset, Integer limit) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified type
	 * 
	 * @param type Campaign type
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified type (paged)
	 * 
	 * @param type Campaign type
	 * @param offset paging offset (first result)
	 * @param limit paging limit (max results)
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type, Integer offset, Integer limit) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified status
	 * 
	 * @param status Campaign status
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignStatus status) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified status (paged)
	 * 
	 * @param status Campaign status
	 * @param offset paging offset (first result)
	 * @param limit paging limit (max results)
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignStatus status, Integer offset, Integer limit) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified type and status
	 * 
	 * @param type Campaign type
	 * @param status Campaign status
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type, CampaignStatus status) throws RestCommandException;
	
	/**
	 * Get all campaigns of specified type and status (paged)
	 * 
	 * @param type Campaign type
	 * @param status Campaign status
	 * @param offset paging offset (first result)
	 * @param limit paging limit (max results)
	 * @return paged list of campaigns
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(CampaignType type, CampaignStatus status, Integer offset, Integer limit) throws RestCommandException;
	

	/**
	 * Get all campaigns with configurable parameters
	 * 
	 * @param parameters Map of URL parameters. Accepted keys are 
	 * <ul>
	 * 	<li>type - filter by campaign type
	 * 	<li>status - filter by campaign status
	 * 	<li>offset - paging offset (first result)
	 * 	<li>limit - paging limit (max results)
	 * 
	 * @return Paged List of Campaign DTO's
	 * @throws RestCommandException if error occurs during API call
	 */
	public PagedListDto<CampaignDto> getCampaigns(Map<String, Object> parameters) throws RestCommandException;

	/**
	 * @param campaignId
	 * @return Campaign DTO
	 * @throws RestCommandException if error occurs during API call
	 */
	public CampaignDto getCampaignDetails(Long campaignId) throws RestCommandException;

	/**
	 * Add a single contact to a campaign.
	 * @param campaignId
	 * @param contact
	 * 
	 * @throws RestCommandException if msisdn is not in correct format or error occurs during API call
	 */
	public void addContactToCampaign(Long campaignId, ContactDto contact) throws RestCommandException;
	
	/**
	 * Add multiple contacts to a campaign.
	 * 
	 * @param campaignId
	 * @param contacts list of contacts to add.
	 * 
	 * @throws RestCommandException if msisdn is not in correct format or error occurs during API call
	 */
	void addContactsToCampaign(Long campaignId, List<ContactDto> contacts) throws RestCommandException;

	/**
	 * Remove a contact from a campaign
	 * 
	 * @param campaignId
	 * @param msisdn
	 * @throws RestCommandException if msisdn is not in correct format or error occurs during API call
	 */
	public void removeContactFromCampaign(Long campaignId, String msisdn) throws RestCommandException;
	
	/**
	 * Remove a contact from a campaign
	 * 
	 * @param campaignId
	 * @param contact
	 * @throws RestCommandException if msisdn is not in correct format or error occurs during API call
	 */
	public void removeContactFromCampaign(Long campaignId, ContactDto contact) throws RestCommandException;

	/**
	 * Create new FIXED campaign in Mobilisr
	 * 
	 * @param campaign
	 * @return 
	 */
	public long createNewCampaign(CampaignDto campaign) throws RestCommandException;

	/**
	 * Create new FIXED campaign in Mobilisr
	 * 
	 * @param name
	 * @param description
	 * @param message
	 * @param contacts
	 * @throws RestCommandException 
	 */
	public void createNewCampaign(String name, String description, String message,
			List<ContactDto> contacts) throws RestCommandException;
}
