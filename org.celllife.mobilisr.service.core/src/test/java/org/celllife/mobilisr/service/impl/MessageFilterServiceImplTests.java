package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.MessageFilterDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.MessageFilterService;
import org.celllife.mobilisr.service.action.AddToGroupAction;
import org.celllife.mobilisr.service.action.EmailAction;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.filter.KeywordFilter;
import org.celllife.mobilisr.service.filter.MatchAllFilter;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class MessageFilterServiceImplTests extends AbstractServiceTest {

	@Autowired
	private MessageFilterDAO messageFilterDAO;
	@Autowired
	private MessageFilterService filterService;
	@Autowired
	private MessageFilterServiceImpl serviceImpl;
	
	private Organization organization;

	@Before
	public void setUp() throws Exception {
		organization = getGeneralDao().findAll(Organization.class).get(0);		
		
		serviceImpl = TestUtils.getTargetObject(filterService, MessageFilterServiceImpl.class);
	}

	@Test (expected = UniquePropertyException.class)
	public void testValidFilter_Channel() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);

		MessageFilter testFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		testFilter.setChannel(channel);

		serviceImpl.validateFilterOrthogonality(testFilter);
	}
	
	@Test
	public void testValidFilter_channel_with_keyword() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		messageFilterDAO.save(newFilter);

		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(keyFilter));
	}
	
	@Test
	public void testValidFilter_saveChannelFilterTwice() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		messageFilterDAO.save(newFilter);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(newFilter));
	}
	
	@Test
	public void testValidFilter_saveKeywordFilterTwice() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		messageFilterDAO.save(keyFilter);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(keyFilter));
	}

	private Channel getChannel() {

		Channel channel = DomainMockFactory._().on(Channel.class).create();
		channel.setType(ChannelType.IN);
		getGeneralDao().save(channel);
		return channel;
	}

	@Test(expected = UniquePropertyException.class)
	public void testValidFilter_Keyword() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyw Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("HELP");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		serviceImpl.validateFilterOrthogonality(testFilter2);

	}
	
	@Test
	public void testValidFilter_Keyword_subOfExisting() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP1");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyword Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("HELP");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter2));
	}
	
	@Test
	public void testValidFilter_Keyword_extensionOfExisting() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyword Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("HELP1");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter2));
	}
	
	@Test(expected = UniquePropertyException.class)
	public void testValidFilter_MultipleKeyword_match() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP,CAT,DOG,DONKEY");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyw Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("FISH,OCTOPUS,CAT");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		serviceImpl.validateFilterOrthogonality(testFilter2);
	}
	
	@Test
	public void testValidFilter_MultipleKeyword_noMatch() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP,CAT,DOG,DONKEY");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyw Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("FISH,JELLYFISH,OCTOPUS");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter2));
	}
	
	@Test
	public void testValidFilter_MultipleKeyword_similar() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP,CAT,DOG,DONKEY");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyw Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("CATCH,HELPME,DOGMATIC");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter2));
	}
	
	@Test
	public void testValidFilter_MultipleKeyword_similar_reverse() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("CATCH,HELPME,DOGMATIC");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilter testFilter2 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter2.setName("Test Keyw Filter");
		testFilter2.setOrganization(organization);
		keyword.setValue("HELP,CAT,DOG,DONKEY");
		testFilter2.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter2.setChannel(channel);

		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter2));
	}
	
	@Test
	public void testValidFilter_Keyword_Pass() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter keyFilter = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keyword = new StringParameter(KeywordFilter.KEYWORD, "");
		keyword.setValue("HELP");
		keyFilter.setProps(YamlUtils.dumpParameterList(keyword));
		keyFilter.setChannel(channel);
		keyFilter.setActive(true);
		messageFilterDAO.save(keyFilter);
		
		MessageFilterServiceImpl serviceImpl = TestUtils.getTargetObject(filterService, MessageFilterServiceImpl.class);

		MessageFilter testFilter3 = new MessageFilter(KeywordFilter.BEAN_NAME, "");
		testFilter3.setName("Test Keyw Filter");
		testFilter3.setOrganization(organization);
		keyword.setValue("ENTER");
		testFilter3.setProps(YamlUtils.dumpParameterList(keyword));
		testFilter3.setChannel(channel);
		
		Assert.assertTrue(serviceImpl.validateFilterOrthogonality(testFilter3));
	}
	
	@Test
	public void testValidateUniqueName_pass() throws UniquePropertyException{
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);

		MessageFilter testFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		testFilter.setName("ChannelFilter with different name");
		testFilter.setOrganization(organization);
		testFilter.setChannel(channel);
		
		serviceImpl.validateUniqueName(testFilter);
		// pass if no exception is thrown
	}
	
	@Test(expected=UniquePropertyException.class)
	public void testValidateUniqueName_nameClash() throws UniquePropertyException{
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);

		MessageFilter testFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		testFilter.setName(MatchAllFilter.BEAN_NAME);
		testFilter.setOrganization(organization);
		testFilter.setChannel(channel);
		
		serviceImpl.validateUniqueName(testFilter);
	}
	
	@Test
	public void testValidateUniqueName_differentOrgs() throws UniquePropertyException{
		Channel channel = getChannel();
		
		Organization organization1 = getGeneralDao().findAll(Organization.class).get(1);		
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);

		MessageFilter testFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		testFilter.setName(MatchAllFilter.BEAN_NAME);
		testFilter.setOrganization(organization1);
		testFilter.setChannel(channel);
		
		serviceImpl.validateUniqueName(testFilter);
		// pass if no exception is thrown
	}
	
	@Test
	public void testValidateUniqueName_validateSelf() throws UniquePropertyException{
		Channel channel = getChannel();
		
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);

		serviceImpl.validateUniqueName(newFilter);
		// pass if no exception is thrown
	}

	@Test
	public void testSaveFilterActions_allNew() throws UniquePropertyException{
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);
		
		// create filter action descriptors
		List<Pconfig> actionDs = new ArrayList<Pconfig>();
		Pconfig pconfig = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter)pconfig.getParameter(AddToGroupAction.GROUP_ID)).setValue("17");
		actionDs.add(pconfig);
		pconfig = new EmailAction().getConfigDescriptor();
		((StringParameter)pconfig.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		actionDs.add(pconfig);
		
		serviceImpl.saveFilterActions(actionDs, newFilter);
		
		// validate actions saved
		Search s = new Search(FilterAction.class);
		s.addFilterEqual(FilterAction.PROP_FILTER, newFilter);
		@SuppressWarnings("unchecked")
		List<FilterAction> savedActions = getGeneralDao().search(s);
		
		Assert.assertEquals(2,savedActions.size());
		Assert.assertEquals(AddToGroupAction.BEAN_NAME, savedActions.get(0).getType());
		Assert.assertTrue(savedActions.get(0).getProps().contains("17"));
		Assert.assertEquals(EmailAction.BEAN_NAME, savedActions.get(1).getType());
		Assert.assertTrue(savedActions.get(1).getProps().contains("test@test.com"));
	}
	
	@Test
	public void testSaveFilterActions_updateExisting() throws UniquePropertyException{
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);
		
		// create filter action descriptors
		List<Pconfig> actionDs = new ArrayList<Pconfig>();
		Pconfig pconfig = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter)pconfig.getParameter(AddToGroupAction.GROUP_ID)).setValue("17");
		actionDs.add(pconfig);
		pconfig = new EmailAction().getConfigDescriptor();
		((StringParameter)pconfig.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		actionDs.add(pconfig);
		
		serviceImpl.saveFilterActions(actionDs, newFilter);
		
		// load saved actions
		actionDs = serviceImpl.getActionsForFilter(newFilter.getId());
		Assert.assertEquals(2, actionDs.size());
		
		// update action descriptor
		((StringParameter)actionDs.get(1).getParameter(EmailAction.MAIL_TO)).setValue("new@test.com");
		serviceImpl.saveFilterActions(actionDs, newFilter);
		
		// validate action updated
		Search s = new Search(FilterAction.class);
		s.addFilterEqual(FilterAction.PROP_FILTER, newFilter);
		@SuppressWarnings("unchecked")
		List<FilterAction> savedActions = getGeneralDao().search(s);
		
		Assert.assertEquals(2,savedActions.size());
		Assert.assertEquals(actionDs.get(1).getId(), savedActions.get(1).getId().toString());
		Assert.assertTrue(savedActions.get(1).getProps().contains("new@test.com"));
	}
	
	@Test
	public void testSaveFilterActions_deleteOld() throws UniquePropertyException{
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);
		
		// create filter action descriptors
		List<Pconfig> actionDs = new ArrayList<Pconfig>();
		Pconfig pconfig = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter)pconfig.getParameter(AddToGroupAction.GROUP_ID)).setValue("17");
		actionDs.add(pconfig);
		pconfig = new EmailAction().getConfigDescriptor();
		((StringParameter)pconfig.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		actionDs.add(pconfig);
		
		serviceImpl.saveFilterActions(actionDs, newFilter);
		
		// load saved actions
		actionDs = serviceImpl.getActionsForFilter(newFilter.getId());
		Assert.assertEquals(2, actionDs.size());
		
		// remove first action
		actionDs.remove(0);
		serviceImpl.saveFilterActions(actionDs, newFilter);
		
		// validate action updated
		Search s = new Search(FilterAction.class);
		s.addFilterEqual(FilterAction.PROP_FILTER, newFilter);
		@SuppressWarnings("unchecked")
		List<FilterAction> savedActions = getGeneralDao().search(s);
		
		Assert.assertEquals(1,savedActions.size());
		Assert.assertEquals(EmailAction.BEAN_NAME, savedActions.get(0).getType());
		Assert.assertTrue(savedActions.get(0).getProps().contains("test@test.com"));
	}
	
	@Test
	public void testGetActionsForFilter(){
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilter newFilter = new MessageFilter(MatchAllFilter.BEAN_NAME, "");
		newFilter.setName(MatchAllFilter.BEAN_NAME);
		newFilter.setOrganization(organization);
		newFilter.setChannel(channel);
		newFilter.setActive(true);
		messageFilterDAO.save(newFilter);
		
		// create action
		StringParameter param = new StringParameter(EmailAction.MAIL_TO, "");
		param.setValue("test@test.com");
		FilterAction action = new FilterAction("EmailAction", YamlUtils.dumpParameterList(param));
		action.setFilter(newFilter);
		getGeneralDao().save(action);
		
		List<Pconfig> actions = serviceImpl.getActionsForFilter(newFilter.getId());
		
		// validate loaded action 
		Assert.assertEquals(1, actions.size());
		Pconfig actionD = actions.get(0);
		Assert.assertEquals(new EmailAction().getConfigDescriptor().getLabel(),
				actionD.getLabel());
		Assert.assertEquals("test@test.com",
				(String) actionD.getParameter(EmailAction.MAIL_TO).getValue());
	}
	
	@Test
	public void testSaveMessageFilter() throws UniquePropertyException{
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilterViewModel viewModel = new MessageFilterViewModel(new MessageFilter());
		viewModel.setName("filter name");
		viewModel.setOrganization(organization);
		viewModel.setChannel(channel);
		
		Pconfig type = new KeywordFilter().getConfigDescriptor();
		((StringParameter)type.getParameter(KeywordFilter.KEYWORD)).setValue("KEYWORD");
		viewModel.setTypeDescriptor(type);
		
		// create filter action descriptors
		List<Pconfig> actionDs = new ArrayList<Pconfig>();
		Pconfig pconfig = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter)pconfig.getParameter(AddToGroupAction.GROUP_ID)).setValue("19");
		actionDs.add(pconfig);
		pconfig = new EmailAction().getConfigDescriptor();
		((StringParameter)pconfig.getParameter(EmailAction.MAIL_TO)).setValue("test1@test.com");
		actionDs.add(pconfig);
		
		viewModel.setActionDescriptors(actionDs);
		
		//call test method
		serviceImpl.saveMessageFilter(viewModel);
		
		// validate message filter saved correctly
		MessageFilter filter = messageFilterDAO.getFilterByNameAndOrganization(viewModel.getName(), organization);
		Assert.assertNotNull(filter);
		List<Parameter<?>> parameterList = YamlUtils.loadParameterList(filter.getProperties());
		Assert.assertEquals(1, parameterList.size());
		Assert.assertEquals("KEYWORD", parameterList.get(0).getValue());
		
		// validate actions saved correctly
		Search s = new Search(FilterAction.class);
		s.addFilterEqual(FilterAction.PROP_FILTER, filter);
		@SuppressWarnings("unchecked")
		List<FilterAction> savedActions = getGeneralDao().search(s);
		Assert.assertEquals(2, savedActions.size());
		Assert.assertEquals(AddToGroupAction.BEAN_NAME, savedActions.get(0).getType());
		Assert.assertTrue(savedActions.get(0).getProps().contains("19"));
		Assert.assertEquals(EmailAction.BEAN_NAME, savedActions.get(1).getType());
		Assert.assertTrue(savedActions.get(1).getProps().contains("test1@test.com"));
	}
	
	@Test
	public void testGetMessageFilterViewModel() throws UniquePropertyException{
		// create channel
		Channel channel = getChannel();
		
		// create filter
		MessageFilterViewModel viewModel = new MessageFilterViewModel(new MessageFilter());
		viewModel.setName("filter name");
		viewModel.setOrganization(organization);
		viewModel.setChannel(channel);
		
		Pconfig type = new KeywordFilter().getConfigDescriptor();
		((StringParameter)type.getParameter(KeywordFilter.KEYWORD)).setValue("KEYWORD");
		viewModel.setTypeDescriptor(type);
		
		// create filter action descriptors
		List<Pconfig> actionDs = new ArrayList<Pconfig>();
		Pconfig pconfig = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter)pconfig.getParameter(AddToGroupAction.GROUP_ID)).setValue("13");
		actionDs.add(pconfig);
		pconfig = new EmailAction().getConfigDescriptor();
		((StringParameter)pconfig.getParameter(EmailAction.MAIL_TO)).setValue("test1@test.com");
		actionDs.add(pconfig);
		
		viewModel.setActionDescriptors(actionDs);
		
		//call test method
		serviceImpl.saveMessageFilter(viewModel);
		
		Search s = new Search(MessageFilter.class);
		s.addFilterEqual(MessageFilter.PROP_NAME, viewModel.getName());
		s.addFetch(MessageFilter.PROP_CHANNEL);
		s.addFetch(MessageFilter.PROP_ORGANIZATION);
		MessageFilter filter = (MessageFilter) getGeneralDao().searchUnique(s);

		MessageFilterViewModel model = serviceImpl.getMessageFilterViewModel(filter.getId());
		
		// validate filter properties
		Assert.assertEquals(filter.getName(), model.getName());
		Assert.assertEquals(filter.getChannel(), model.getChannel());
		Assert.assertEquals(filter.getOrganization(), model.getOrganization());
		
		// validate type
		Assert.assertEquals(KeywordFilter.BEAN_NAME, model.getTypeDescriptor().getResource());
		Assert.assertEquals("KEYWORD", model.getTypeDescriptor().getParameter(KeywordFilter.KEYWORD).getValue());
		
		// validate actions
		List<Pconfig> actions = model.getActionDescriptors();
		Assert.assertEquals(2, actions.size());
		Assert.assertEquals(new AddToGroupAction().getConfigDescriptor().getLabel(),
				actions.get(0).getLabel());
		Assert.assertEquals("13",
				(String) actions.get(0).getParameter(AddToGroupAction.GROUP_ID).getValue());
		Assert.assertEquals(new EmailAction().getConfigDescriptor().getLabel(),
				actions.get(1).getLabel());
		Assert.assertEquals("test1@test.com",
				(String) actions.get(1).getParameter(EmailAction.MAIL_TO).getValue());
		
	}
}
	


