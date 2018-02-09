package org.celllife.mobilisr.service.utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.FilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.LoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;
import com.trg.search.SearchResult;

public class ServiceUtil {

	public static final String PROP_SEARCH_QUERY = "query";
	public static final String PROP_SEARCH_FIELDS = "fields";
	
	public enum FilterType {
		STRING("string"),
		NUMBERIC("numeric"),
		DATE("date"),
		BOOLEAN("boolean"),
		LIST("list");
		
		private String type;
		
		private FilterType(String type) {
			this.type = type;
		}
		
		public String type() {
			return type;
		}
		
		public static FilterType valueByType(String type) throws IllegalArgumentException {
			for (FilterType item : values()) {
				if (item.type().equals(type)) {
					return item;
				}
			}
			throw new IllegalArgumentException(
					"No enum const FilterType with type " + type);
		}
	}
	
	public enum ComparisonOp{
		lt(Filter.OP_LESS_THAN), 
		gt(Filter.OP_GREATER_THAN), 
		eq(Filter.OP_EQUAL), 
		before(Filter.OP_LESS_THAN), 
		after(Filter.OP_GREATER_THAN), 
		on(Filter.OP_EQUAL);
		
		private final int operator;

		private ComparisonOp(int op) {
			this.operator = op;
		}
		
		public int operator() {
			return operator;
		}
		
		public static ComparisonOp safeValueOf(String value){
			ComparisonOp item = null;
			try {
				item = valueOf(value);
			} catch (Exception ignore) {
				// ignore exception
			}
			return item;
		}
	}
	
	/**
	 * @param entityClass
	 *            the class to search for
	 * @param loadConfig
	 *            the {@link LoadConfig} from which to get the sortField,
	 *            sortDirection, limit, offset as well as filter parameters.
	 *            Filter parameters are stored in the loadConfig using the
	 *            following properties:
	 *            <ul>
	 *            <li>{@link ServiceUtil#PROP_SEARCH_FIELDS} - The fields to use
	 *            for searching (comma separated list of strings).</li>
	 *            <li>{@link ServiceUtil#PROP_SEARCH_QUERY} - The value to
	 *            search for (Object).</li>
	 *            </ul>
	 * 
	 *            If the loadConfig is an instance of {@link FilterConfig} then
	 *            filtering will also be applied based on the config parameters.
	 * @param defaultSortField
	 *            in the case where a sort field is not supplied in the
	 *            loadConfig then use this field for sorting.
	 * @return the configured Search
	 */
	public static Search getSearchFromLoadConfig(
			Class<? extends MobilisrEntity> entityClass,
			PagingLoadConfig loadConfig, String defaultSortField) {
		Search search = new Search(entityClass);

		// apply sorting
		String sortField = loadConfig.getSortField();
		sortField = (sortField == null ? defaultSortField : sortField);
		SortDir sortDir = loadConfig.getSortDir();
		if (sortField != null) {
			search.addSort(sortField, sortDir == SortDir.DESC);
		}

		// apply search
		Object searchValue = loadConfig.get(PROP_SEARCH_QUERY);
		String csvFields = loadConfig.get(PROP_SEARCH_FIELDS);
		String[] searchFields = null;
		if (csvFields != null && !csvFields.isEmpty()){
			searchFields = csvFields.split(",");
		}
		
		if (searchValue != null && searchFields != null && searchFields.length > 0) {
			Filter[] searchFilters = new Filter[searchFields.length];
			for (int i = 0; i < searchFields.length; i++) {
				searchFilters[i] = Filter.ilike(searchFields[i], "%" + searchValue + "%");
			}
			search.addFilterOr(searchFilters);
		}

		// apply filtering
		if (loadConfig instanceof FilterPagingLoadConfig){
			FilterPagingLoadConfig flc = (FilterPagingLoadConfig) loadConfig;
			List<FilterConfig> filterConfigs = flc.getFilterConfigs();
			if (filterConfigs != null && !filterConfigs.isEmpty()){
				for (FilterConfig filterConfig : filterConfigs) {
					Filter filter = getFilter(entityClass, filterConfig);
					if (filter != null){
						search.addFilter(filter);
					}
				}
			}
		}
		
		search.setMaxResults(loadConfig.getLimit());
		search.setFirstResult(loadConfig.getOffset());
		return search;
	}
	
	private static Filter getFilter(Class<? extends MobilisrEntity> entityClass, 
			FilterConfig loadConfig) {
		String typeString = loadConfig.getType();
		String field = loadConfig.getField();
		String comparison = loadConfig.getComparison();
		Object value = loadConfig.getValue();
		
		if (value == null || field == null || field.isEmpty()) {
			return null;
		}

		FilterType type = FilterType.valueByType(typeString);
		ComparisonOp comparisonOp = ComparisonOp.safeValueOf(comparison);
		
		switch (type){
		case STRING:
			return Filter.ilike(field, "%" + value + "%");
		case NUMBERIC:
			comparisonOp = comparisonOp == null ? ComparisonOp.eq : comparisonOp;
			return new Filter(field, value, comparisonOp.operator());
		case BOOLEAN:
			return Filter.equal(field, value);
		case DATE:
			comparisonOp = comparisonOp == null ? ComparisonOp.on : comparisonOp;
			switch (comparisonOp){
			case before:
				return Filter.lessThan(field,
						MobilisrUtility.getBeginningOfDay((Date) value));
			case after:
				return Filter.greaterThan(field,
						MobilisrUtility.getEndOfDay((Date) value));
			case on:
				Date begin = MobilisrUtility.getBeginningOfDay((Date) value);
				Date end = MobilisrUtility.getEndOfDay((Date) value);
				return Filter.and(Filter.greaterOrEqual(field, begin),
						Filter.lessOrEqual(field, end));
			}
		case LIST:
			List<?> list = (List<?>) value;
			if (list.isEmpty()){
				return null;
			}
			
			list = convertValues(entityClass, field, list);
			
			if (list.size() == 1){
				// more efficient query
				return Filter.equal(field, list.get(0));
			} else {
				return Filter.in(field, list);
			}
		}
		
		return null;
	}

	private static List<?> convertValues(Class<? extends MobilisrEntity> entityClass, String field, List<?> list) {
		if (SmsLog.class.equals(entityClass)){
			if (SmsLog.PROP_STATUS.equals(field)){
				List<SmsStatus> statusList = new ArrayList<SmsStatus>();
				for (Object value : list) {
					statusList.add(SmsStatus.valueOf((String) value));
				}
				list = statusList;
			}
		} else if (Campaign.class.equals(entityClass)){
			if (Campaign.PROP_STATUS.equals(field)){
				List<CampaignStatus> statusList = new ArrayList<CampaignStatus>();
				for (Object value : list) {
					statusList.add(CampaignStatus.valueOf((String) value));
				}
				list = statusList;
			}
		}
		return list;
	}

	public static <T> PagingLoadResult<T> getPagingLoadResult(PagingLoadConfig loadConfig, SearchResult<T> searchResult) {
		List<T> list = searchResult.getResult();
		int totalNum = searchResult.getTotalCount();
		int offset = loadConfig == null ? 0 : loadConfig.getOffset();
		
		return new BasePagingLoadResult<T>(list, offset, totalNum);
	}
}
