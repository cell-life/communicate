package org.celllife.communicate.util;

import java.net.MalformedURLException;

import org.celllife.communicate.page.LoginPage;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "classpath:mobilisr-applicationContext.xml" },inheritLocations=false)
public abstract class AbstractBaseTest extends AbstractDBTest {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractBaseTest.class);
	private static RemoteWebDriver driver;
	protected static CommonHelper testHelper;
	protected static DbHelper dbHelper;
	
	@BeforeClass
	public static void verifyPropertiesOverride() {
		// Make sure this property is set so that we don't clean out the wrong database
		if (System.getProperty("propertiesOverride") == null){
			String msg = "System property propertiesOverride is not set";
			log.error(msg);
			throw new RuntimeException(msg);
		}
	}

	// ----- Rules -----
	@Rule
	public MethodRule watchman = new TestWatchman() {
		public void starting(FrameworkMethod method) {
			log.info("---------------- " + method.getName() + " being run ...");
		}
		
		@Override
		public void failed(Throwable e, FrameworkMethod method) {
			log.error("FAIL: " + e.getClass(),e);
			testHelper.saveScreenShot(method.getMethod().getDeclaringClass(), method.getName(), e);
		}
	};
	
	@Rule
	public ErrorCollector collector= new ErrorCollector();


	@BeforeClass
	public static void setup() throws MalformedURLException {
		testHelper = new CommonHelper();
		driver = testHelper.setup();
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null)
			driver.quit();
	}


	@Before
	public void before() {
		dbHelper = new DbHelper(getGeneralDao());
	}
	
	@After
	public void after() {
		// Logout (even if redundant) to ensure a consistent start point. 
		testHelper.forceLogout();
	}
	
	protected LoginPage getLogin(){
		return new LoginPage(driver);
	}
	
	public DbHelper db(){
		return dbHelper;
	}

}
