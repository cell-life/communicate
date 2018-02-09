package org.celllife.mobilisr.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.constants.SmsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trg.search.Search;

public class RestUtility {
	
	private static Logger log = LoggerFactory.getLogger(RestUtility.class);

	/**
	 * Converts a string into a campaign status or null if string is null
	 * or does not match a campaign status.
	 * 
	 * @param status
	 * @return CampaignStatus or null
	 */
	public static CampaignStatus getCampaignStatus(String status) {
		CampaignStatus statusEnum = null;
		if (status != null && !status.isEmpty()){
			try {
				statusEnum = CampaignStatus.apiValueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				log.info("Unrecognised CampaignStatus {}", status);
			}
		}
		return statusEnum;
	}

	/**
	 * Converts a string into a campaign type or null if string is null
	 * or does not match a campaign type.
	 * 
	 * @param type
	 * @return CampaignType or null
	 */
	public static CampaignType getCampaignType(String type) {
		CampaignType typeEnum = null;
		if (type != null && !type.isEmpty()){
			try {
				typeEnum = CampaignType.apiValueOf(type.toUpperCase());
			} catch (IllegalArgumentException e) {
				log.info("Unrecognised CampaignType {}", type);
			}
		}
		return typeEnum;
	}

	/**
	 * Creates a new Search and sets the maxResults and firstResult based on the 
	 * arguments.
	 * 
	 * @param offset
	 * @param limit
	 * @param defaultLimit
	 * @param maxLimit
	 * 
	 * @return configured Search object
	 */
	public static Search getSearch(Integer offset, Integer limit, int defaultLimit, int maxLimit) {
		Search s = new Search();

		if (limit == null){
			limit = defaultLimit;
		} else if (limit > maxLimit){
			limit = maxLimit;
		}
		
		s.setMaxResults(limit);
		
		if (offset != null){
			s.setFirstResult(offset);
		}
		return s;
	}
	
	public static void addSearchFilterEqual(Search search, String prop, Object value){
		if (search != null && prop != null && !prop.isEmpty() && value != null){
			search.addFilterEqual(prop, value);
		}
	}
	
	public static void addSearchFilterGte(Search search, String prop, Object value){
		if (search != null && prop != null && !prop.isEmpty() && value != null){
			search.addFilterGreaterOrEqual(prop, value);
		}
	}
	
	public static void addSearchFilterLte(Search search, String prop, Object value){
		if (search != null && prop != null && !prop.isEmpty() && value != null){
			search.addFilterLessOrEqual(prop, value);
		}
	}
	
	/**
	 * @param list list to check
	 * @param class1 expected class type
	 * @return a list of errors if there are any
	 */
	public static PagedListDto<ErrorDto> checkPageListSizeAndType(PagedListDto<? extends MobilisrDto> list, Class<?> class1) {
		PagedListDto<ErrorDto> errors = new PagedListDto<ErrorDto>();
		if (list.isEmpty()){
			errors.addElement(new ErrorDto(ErrorCode.EMPTY_LIST, "No data in list"));
		} else  if(!list.getElements().get(0).getClass().isAssignableFrom(class1)){
			errors.addElement(new ErrorDto(ErrorCode.UNSUPPORTED_DATA, "Expected " + class1.getName()));
		}
		return errors;
	}

	/**
	 * Checks each contacts msisdn and if it is invalid removes it from the
	 * input list leaving only valid contacts.
	 * 
	 * @param contactList
	 * @return a list of errors, one for each invaliad msisdn
	 */
	public static List<ErrorDto> validateContactList(final List<ContactDto> contactList, ValidatorFactory validatorFactory) {
		List<ErrorDto> errors = new ArrayList<ErrorDto>();
		Iterator<ContactDto> iterator = contactList.iterator();
		while (iterator.hasNext()){
			ContactDto contact = iterator.next();
			ValidationError error = validatorFactory.getMsisdnValidator().validate(contact.getMsisdn());
			if (error != null){
				iterator.remove();
				errors.add(new ErrorDto(ErrorCode.INVALID_MSISDN,
						contact.getMsisdn() + " is an invalid msisdn"));
			}
            if (contact.getStartDate() != null) {
                ValidationError dateError = validatorFactory.getDateValidator().validate(contact.getStartDate());
                if (dateError != null) {
                    errors.add(new ErrorDto(dateError.getCode(),dateError.getMessage()));
                }
			}
		}
		return errors;
	}

	public static SmsStatus getMessageStatus(String status) {
		SmsStatus statusEnum = null;
		if (status != null && !status.isEmpty()){
			try {
				statusEnum = SmsStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				log.info("Unrecognised CampaignStatus {}", status);
			}
		}
		return statusEnum;
	}
}
