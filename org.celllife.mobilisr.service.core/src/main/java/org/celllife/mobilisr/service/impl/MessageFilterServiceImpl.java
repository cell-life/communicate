package org.celllife.mobilisr.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.dao.api.FilterActionDAO;
import org.celllife.mobilisr.dao.api.MessageFilterDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.MessageFilterService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.filter.Filter;
import org.celllife.mobilisr.service.filter.KeywordFilter;
import org.celllife.mobilisr.service.filter.MatchAllFilter;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.util.PconfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;
import com.trg.search.SearchResult;

@Service("messageFilterService")
public class MessageFilterServiceImpl extends BaseServiceImpl implements MessageFilterService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private List<Filter> filterTypes;

	@Autowired
	private List<Action> actions;

	@Autowired
	private MessageFilterDAO filterDao;

	@Autowired
	private FilterActionDAO actionDao;

	@Autowired
	private ChannelDAO channelDao;

	private Map<String, Action> actionMap;
	private Map<String, Filter> filterMap;

	@Override
	public PagingLoadResult<MessageFilter> listMessageFilters(
			Organization organization, Boolean showVoided, PagingLoadConfig loadConfig) {
		Search search = ServiceUtil.getSearchFromLoadConfig(MessageFilter.class, loadConfig, MessageFilter.PROP_NAME);
		if (organization != null){
			search.addFilterEqual(MessageFilter.PROP_ORGANIZATION, organization);
		}
		if (showVoided!=null){
			search.addFilterEqual(MessageFilter.PROP_VOIDED, showVoided);
		}
		SearchResult<MessageFilter> searchResult = filterDao.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Override
	public MessageFilterViewModel getMessageFilterViewModel(Long filterId){
		MessageFilter messageFilter = filterDao.find(filterId);
		MessageFilterViewModel model = new MessageFilterViewModel(messageFilter);
		List<Pconfig> actions = getActionsForFilter(filterId);
		model.setActionDescriptors(actions);

		String type = messageFilter.getType();
		Filter filter = getFilter(type);

		String props = messageFilter.getProps();
		List<Parameter<?>> parameterList = YamlUtils.loadParameterList(props);

		Pconfig typeDescriptor = filter.getConfigDescriptor();
		PconfigUtils.merge(typeDescriptor, parameterList);

		model.setTypeDescriptor(typeDescriptor);

		return model;
	}

	/**
	 * @param messageFilter
	 * @return true if the filter passes the validation
	 * @throws UniquePropertyException if the filter fails the validation
	 */
	public boolean validateFilterOrthogonality(MessageFilter messageFilter) throws UniquePropertyException {

		if (messageFilter.getType().equals(MatchAllFilter.BEAN_NAME)){
			List<MessageFilter> channelFilters = filterDao.getActiveFilters(messageFilter.getChannel(), 
					MatchAllFilter.BEAN_NAME);
			
			if (channelFilters.size() > 0) {
				if (!channelFilters.get(0).getId().equals(messageFilter.getId())) {
					throw new UniquePropertyException("This Channel already contains a Match All Filter");
				}
			}
		} else if (messageFilter.getType().equals(KeywordFilter.BEAN_NAME)){

			List<MessageFilter> keywordFilters = filterDao.getActiveFilters(messageFilter.getChannel(), 
					KeywordFilter.BEAN_NAME);

			if (keywordFilters.size() > 0) {

				List<Parameter<?>> params = YamlUtils.loadParameterList(messageFilter.getProps());
				if (params == null || params.isEmpty()) {
					throw new MobilisrRuntimeException("Empty parameters for keyword filter: " 
							+ messageFilter.getName());
				}
				
				String newKewyord = (String) params.get(0).getValue();
				String regex = "(\\A|,)(" +  newKewyord.replace("," , "|") + ")(,|\\Z)";
				Pattern p = Pattern.compile(regex);
				
				for (MessageFilter keywordFilter : keywordFilters) {
					if (keywordFilter.getId().equals(messageFilter.getId())){
						// don't check against itself
						continue;
					}
					params = YamlUtils.loadParameterList(keywordFilter.getProps());
					if (params == null || params.isEmpty()) {
						log.warn("Keyword filter is has no parameters [id={}] [name={}]", 
								keywordFilter.getId(), keywordFilter.getName());
						continue;
					}

					String existingKw = (String) params.get(0).getValue();
					if (p.matcher(existingKw).find()){
						throw new UniquePropertyException("This Channel already contains " +
								"a Filter with keyword '" + newKewyord + "'");
					}
				}
			}
		}
		return true;
	}

	@Override
	public void saveMessageFilter(MessageFilter messageFilter) throws UniquePropertyException {
		if (messageFilter.isActive()){
			validateFilterOrthogonality(messageFilter);
		}
		filterDao.saveOrUpdate(messageFilter);
	}

	@Override
	public void saveMessageFilter(MessageFilterViewModel model) throws UniquePropertyException {

		MessageFilter messageFilter = model.getMessageFilter();
		validateUniqueName(messageFilter);

		Pconfig typeDescriptor = model.getTypeDescriptor();
		String props = YamlUtils.dumpParameterList(typeDescriptor.getParameters());
		messageFilter.setProps(props);

		validateFilterOrthogonality(messageFilter);

		String actionsLabel = getActionsLabel(model.getActionDescriptors());
		messageFilter.setActionsLabel(actionsLabel);
		
		String rank = typeDescriptor.getProperty(Filter.RANK);
		messageFilter.setRank(Integer.valueOf(rank));
		
		filterDao.saveOrUpdate(messageFilter);

		saveFilterActions(model.getActionDescriptors(), messageFilter);
	}

	/**
	 * @param actionDescriptors
	 *            list of action descriptors
	 * @param messageFilter
	 *            the message filter associated with these actions
	 */
	/*package private*/ void saveFilterActions(List<Pconfig> actionDescriptors,
			MessageFilter messageFilter) {
		List<FilterAction> actions = actionDao.getActionsForFilter(messageFilter);
		
		List<FilterAction> updatedActions = new ArrayList<FilterAction>();

		for (Pconfig actionD : actionDescriptors) {
			String id = actionD.getId();
			if (id == null) {
				createNewFilterAction(messageFilter, actionD);
			} else {
				for (FilterAction filterAction : actions) {
					if (id.equals(filterAction.getId().toString())) {
						updatedActions.add(filterAction);
						updateFilterAction(filterAction, actionD);
						break;
					}
				}
			} 
		}
		
		// delete unused actions
		for (FilterAction action : actions) {
			if (!updatedActions.contains(action)) {
				actionDao.remove(action);
			}
		}
	}

	private String getActionsLabel(List<Pconfig> actionDescriptors) {
		StringBuffer actionString = new StringBuffer();
		for (Pconfig actionD : actionDescriptors) {
			actionString.append(actionD.getLabel()).append(", ");
		}
		if (actionString.length() > 2)
			return actionString.substring(0, actionString.length()-2);
		else 
			return "";
	}

	/*package private*/ void validateUniqueName(MessageFilter filter)
			throws UniquePropertyException {
		Organization organization = filter.getOrganization();
		String name = filter.getName();

		MessageFilter existing = filterDao.getFilterByNameAndOrganization(name, organization);

		// name is in use if existing filter with the same name
		// does not have the same ID as the filter we're saving
		boolean nameInUse = (existing != null)
			&& (!filter.isPersisted() || !existing.getId().equals(filter.getId()));

		if (nameInUse){
			String message = MessageFormat.format("A filter with the name \"{0}\" already "
							+ "exists for organisation \"{1}\"",
					name, organization.getName());
			throw new UniquePropertyException(message);
		}
	}

	private void createNewFilterAction(MessageFilter messageFilter,
			Pconfig actionD) {
		FilterAction filterAction = new FilterAction();
		filterAction.setFilter(messageFilter);

		updateFilterAction(filterAction, actionD);
	}

	private void updateFilterAction(FilterAction filterAction, Pconfig actionD) {
		filterAction.setType(actionD.getResource());

		List<Parameter<?>> parameters = actionD.getParameters();
		String props = YamlUtils.dumpParameterList(parameters);
		filterAction.setProps(props);

		actionDao.saveOrUpdate(filterAction);
	}

	@Override
	public List<Pconfig> getActionsForFilter(Long filterId){
		List<Pconfig> actionDescriptors = new ArrayList<Pconfig>();
		MessageFilter filter = filterDao.find(filterId);
		List<FilterAction> actions = actionDao.getActionsForFilter(filter);
		for (FilterAction fAction : actions) {
			String props = fAction.getProperties();
			List<Parameter<?>> parameterList = YamlUtils.loadParameterList(props);

			String type = fAction.getType();
			Action action = getAction(type);

			Pconfig descriptor = action.getConfigDescriptor();
			descriptor.setId(fAction.getId().toString());
			PconfigUtils.merge(descriptor, parameterList);

			actionDescriptors.add(descriptor);
		}
		return actionDescriptors;
	}

	@Override
	public Collection<Pconfig> listAllActions() {
		List<Pconfig> actionDescriptors = new ArrayList<Pconfig>();
		for (Action action : actions) {
			actionDescriptors.add(action.getConfigDescriptor());
		}
		Collections.sort(actionDescriptors, new Comparator<Pconfig>() {

			@Override
			public int compare(Pconfig o1, Pconfig o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		return actionDescriptors;
	}

	@Override
	public Collection<Pconfig> listAllFilterTypes() {
		List<Pconfig> descriptors = new ArrayList<Pconfig>();
		for (Filter f : filterTypes) {
			descriptors.add(f.getConfigDescriptor());
		}
		return descriptors;
	}

	@Override
	public List<Channel> listAllChannels(){
		return channelDao.getActiveInChannels();
	}

	private Action getAction(String type) {
		initActionMap();

		return actionMap.get(type);
	}

	private void initActionMap() {
		if (actionMap == null){
			actionMap = new HashMap<String, Action>();
			for (Action action : actions) {
				Pconfig descriptor = action.getConfigDescriptor();
				actionMap.put(descriptor.getResource(), action);
			}
		}
	}

	private Filter getFilter(String type) {
		initFilterMap();

		return filterMap.get(type);
	}

	private void initFilterMap() {
		if (filterMap == null){
			filterMap = new HashMap<String, Filter>();
			for (Filter filter : filterTypes) {
				Pconfig descriptor = filter.getConfigDescriptor();
				filterMap.put(descriptor.getResource(), filter);
			}
		}
	}
}
