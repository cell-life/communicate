package org.celllife.communicate.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.celllife.mobilisr.test.TestUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Example relevant System Properties
// -DtestUrl=http://127.0.0.1:8181/communicate
// -Dwebdriver.firefox.bin="C:\Program Files\Mozilla Firefox 3.x\firefox.exe"
// -DpropertiesOverride=communicate_override.properties
// -DseleniumHubUrl=http://172.16.23.117:5556/wd/hub
public class CommonHelper {

	// ** TEST_URL ** -- Over-night tests on Jenkins depend on this being set correctly. Don't
	//   commit changes to this unless Jenkins setup has changed. If you want to change this to run
	//   tests via Eclipse, rather set it via "VM arguments" in "Run Configurations". Or, if you are
	//   running tests locally via command line "mvn verify", pass it as a parameter
	//   "mvn verify -DtestUrl=http://localhost:8181/communicate".
	private static final String TEST_URL = "http://dev.cell-life.org:8181/communicate";
	private static final Logger log = LoggerFactory.getLogger(CommonHelper.class);

	private RemoteWebDriver driver;
	private static String testURL;



	public CommonHelper() {
		// Use default URL until setup() called, and testURL dynamically determined.
		testURL = TEST_URL;
	}


	RemoteWebDriver setup() throws MalformedURLException {
		String hubUrl = System.getProperty("seleniumHubUrl");

		if ((hubUrl == null) || hubUrl.isEmpty() ) {
			log.info("No Selenium Hub URL, using local Firefox.");
			log.info("webdriver.firefox.bin: {}", System.getProperty("webdriver.firefox.bin") );
			log.info("Using profile: {}", System.getProperty("webdriver.firefox.profile") );
			driver = new FirefoxDriver();
		} else {
			log.info("Selenium Hub URL: " + hubUrl);
			DesiredCapabilities capability = DesiredCapabilities.firefox();
			driver = new RemoteWebDriver(new URL(hubUrl), capability);
		}

		testURL = System.getProperty("testUrl");
		if ((testURL == null) || testURL.isEmpty() ) {
			testURL = TEST_URL;
			log.info("Test URL not specified, using {}", testURL);
		} else {
			log.info("Test URL: {}", testURL);
		}

		// Check the testURL takes us to the right place
		driver.get(testURL);
		try {
			checkAtLoginPage();
		}
		catch (AssertionError e)
		{
			String msg = "Setup(): Unable to verify navigation to " + testURL
			+ "\nCheck network, and that Communicate is running.";
			log.error(msg);
			driver.close();
			Assert.fail(msg + "\n" + e.getMessage() );
		}

		maximizeWindow();

		return driver;
	}

	private void maximizeWindow() {
		if (driver instanceof JavascriptExecutor) {
			((JavascriptExecutor) driver).executeScript("if (window.screen) {"
					+ "window.moveTo(0, 0);"
					+ "window.resizeTo(window.screen.availWidth,"
					+ "window.screen.availHeight);" + "};");
		}
	}

	public static void wait_ms(int period_ms) {
		try {
			Thread.sleep(period_ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private void checkAtLoginPage() {
		try {
			WebElement heading = driver.findElement(By.id("login_heading"));
			Assert.assertEquals("Login page heading doesn't match expected text",
					"Communicate Login", heading.getText() );

			driver.findElement(By.name("j_username"));
		}
		catch (NoSuchElementException e) {
				Assert.fail(e.getMessage());
		}
	}


	public void forceLogout() {
		log.info("Logging out");
		driver.get(testURL + "/mobilisr/j_spring_security_logout");
		checkAtLoginPage();
	}


	public void saveScreenShot(TakesScreenshot driver, Class<?> testClass, String strFileNamePrefix) {
		File screenshotFile = driver.getScreenshotAs(OutputType.FILE);

		try {
			File destFile = getTargetFile(testClass, strFileNamePrefix);
			FileUtils.copyFile(screenshotFile, destFile);
			log.info("Screenshot saved to: {}", destFile.getAbsolutePath());
		} catch (IOException e) {
			log.error("Problem saving screenshot: " + e);
		}
	}


	public void saveScreenShot(Class<?> testClass, String strFileNamePrefix, Throwable e) {
		// If this is a local firefox instance, use the more appropriate method.
		if (driver instanceof TakesScreenshot) {
			saveScreenShot((TakesScreenshot) driver, testClass, strFileNamePrefix);
			return;
		}
		else {
			WebDriver augDriver = new Augmenter().augment(driver);
			if (augDriver instanceof TakesScreenshot) {
				saveScreenShot( (TakesScreenshot)augDriver, testClass, strFileNamePrefix);
				return;
			}
		}

		if (e == null)
		{
			log.warn("Skipping screenshot (Throwable is null.)");
			return;
		}

		Throwable cause = e.getCause();
		if (cause instanceof ScreenshotException) {
			byte[] imageBytes = Base64.decodeBase64(
					( (ScreenshotException)cause).getBase64EncodedScreenshot() );
			try {
				File destFile = getTargetFile(testClass, strFileNamePrefix);
				FileUtils.writeByteArrayToFile(destFile, imageBytes);
				log.info("Screenshot saved to: {}", destFile.getAbsolutePath());
			} catch (IOException e1) {
				log.error("Problem saving screenshot: " + e1);
			}
		}
		else
			log.warn("Skipping screenshot (Not a ScreenshotException).");
	}


	/**
	 * @param testClass
	 * @param strFileName
	 * @return
	 * @throws IOException
	 */
	private File getTargetFile(Class<?> testClass, String strFileNamePrefix) throws IOException {
		String strFileName = getTimeNowString(strFileNamePrefix) + ".png";
		File directory = new File("target/surefire-reports/" + testClass.getName() + "/");
		FileUtils.forceMkdir(directory);
		File destFile = new File(directory.getAbsolutePath() + File.separator + strFileName);
		return destFile;
	}


	public String getTimeNowString(String strPrefix) {
		DateTime tNow = new DateTime();
		String strTimeNow = strPrefix + "__"
			+ tNow.toString("yyy-MM-dd_HH") + "h"
			+ tNow.toString("mm") + "m" + tNow.toString("ss");
		return strTimeNow;
	}

	public static String getLoremIpsum(int numCharacters) {
		return TestUtils.getLoremIpsum(numCharacters);
	}
	
	public static String getTestURL() {
		return testURL;
	}
}
