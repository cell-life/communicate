package org.celllife.mobilisr.liquibase;

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.util.MobilisrPropertyPlaceholderConfigurer;
import org.celllife.mobilisr.util.SpringLiquibaseUpdater;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

@Ignore("Used for manual testing of liquibase changesets")
public class LiquibaseChangesetTests extends AbstractDBTest {
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	@Autowired
	private SpringLiquibaseUpdater updater;
	
	@Autowired
	MobilisrPropertyPlaceholderConfigurer properties;
	
	@Before
	public void setupClass(){
		// remove record of previously run changelogs to allow us to run them again
		getGeneralDao()
				.getSession()
				.createSQLQuery(
						"delete from liquibase_changelog where ID like '%_test'")
				.executeUpdate();
		
		// delete all channelconfigs
		getGeneralDao().getSession()
				.createSQLQuery(
						"delete from channelconfig")
				.executeUpdate();
		
		// delete all channels
		getGeneralDao().getSession()
				.createSQLQuery(
						"delete from channel")
				.executeUpdate();
		
		Channel channel = new Channel("test channel1", ChannelType.OUT,
				"telfreeOutChannel", null);
		channel.setVoided(true);
		getGeneralDao().save(channel);
		channel = new Channel("test channel2", ChannelType.OUT,
				"telfreeSmpp", null);
		channel.setVoided(true);
		getGeneralDao().save(channel);
		channel = new Channel("test channel3", ChannelType.OUT,
				"integratOutChannel", null);
		channel.setVoided(true);
		getGeneralDao().save(channel);
		
		channel = new Channel("test channel4", ChannelType.IN,
				null, "123");
		channel.setVoided(true);
		getGeneralDao().save(channel);
		channel = new Channel("test channel5", ChannelType.IN,
				null, "456");
		channel.setVoided(true);
		getGeneralDao().save(channel);
		
	}
	
	@Test
	public void testMoveChangeSettingsToDatabase() throws Exception{
		updater.setChangeLog("classpath:org/celllife/mobilisr/liquibase/MoveChannelSettingsToDatabase_test.xml");
		updater.init();
		
		List<ChannelConfig> list = getGeneralDao().findAll(ChannelConfig.class);
		Assert.assertEquals(3, list.size());

		for (ChannelConfig config : list) {
			if (config.getHandler().equals("telfreeOutChannel")){
				
				String configProps = config.getProperties();
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_HTTP_PASSWORD)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_HTTP_USERNAME)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_HTTP_URL)));
				
			} else if (config.getHandler().equals("telfreeSmpp")){
				
				String configProps = config.getProperties();
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_HOST)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_PASSWORD)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_PORT)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_SERVICE_TYPE)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_SOURCE_ADDRESS)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_SYSTEM_TYPE)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.TELFREE_SMPP_USERNAME)));
				
			} else if (config.getHandler().equals("integratOutChannel")){
				
				String configProps = config.getProperties();
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.INTEGRAT_HTTP_PASSWORD)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.INTEGRAT_HTTP_SERVICECODE)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.INTEGRAT_HTTP_TAG)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.INTEGRAT_HTTP_URL)));
				collector.checkThat(configProps, containsString(getProp(MoveChannelSettingsToDatabase.INTEGRAT_HTTP_USERNAME)));
				
			} else {
				Assert.fail("Unexpected config type: " + config.getHandler());
			}
		}
		
		Search search = new Search(Channel.class);
		search.addFetch(Channel.PROP_CONFIG);
		@SuppressWarnings("unchecked")
		List<Channel> channel = getGeneralDao().search(search);
		for (Channel chan : channel) {
			ChannelConfig config = chan.getConfig();
			if (chan.getType().equals(ChannelType.OUT)) {
				collector.checkThat(config, notNullValue());
			} else { 
				collector.checkThat(config, nullValue());
				collector.checkThat(chan.getHandler(), equalTo("in-http"));
			}
			
			if (config != null)
				collector.checkThat(chan.getHandler(), equalTo(config.getHandler()));
		}
		
	}

	private String getProp(String key) {
		return properties.getProperties().getProperty(key);
	}
}
