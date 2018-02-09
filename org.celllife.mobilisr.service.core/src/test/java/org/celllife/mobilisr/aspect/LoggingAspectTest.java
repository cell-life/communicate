package org.celllife.mobilisr.aspect;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.mock.MockLogger;
import org.celllife.mobilisr.mock.MockLogger.LogMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class LoggingAspectTest {
    
    @Autowired
    private MockLogger logger;

    @Autowired
    @Qualifier(value = "simpleBean")
    public SimpleBean simpleBean;

    @Autowired
    public SimpleBeanSubclass simpleBeanSubclass;

    private Date date;

    @Before
    public void before() throws ParseException {
	logger.setLogLevel(SimpleBean.class, LogLevel.TRACE);
	logger.setLogLevel(SimpleBeanSubclass.class, LogLevel.TRACE);
	logger.resetLoggers();
	date = DateUtils.parseDate("01/01/2010", new String[] { "dd/MM/yyyy" });
    }

    @Test
    public void testSimpleBean_SetDateProperty() throws Exception {
	simpleBean.setDateProperty(date);

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE,
		"[ entering < setDateProperty > with params ( "+date+" ) ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE, "[ leaving < setDateProperty > ]");
    }

    @Test
    public void testSimpleBean_SetIntegerProperty() {
	simpleBean.setIntegerProperty(100);

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE,
		"[ entering < setIntegerProperty > with params ( 100 ) ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE, "[ leaving < setIntegerProperty > ]");
    }

    @Test
    public void testSimpleBean_SetStringProperty() {
	simpleBean.setStringProperty("stringProperty");

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE,
		"[ entering < setStringProperty > with params ( stringProperty ) ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE, "[ leaving < setStringProperty > ]");
    }

    @Test
    public void testSimpleBean_GetDateProperty() {
	simpleBean.getDateProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE, "[ entering < getDateProperty > ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE,
		"[ leaving < getDateProperty > returning ( "+date+" ) ]");
    }

    @Test
    public void testSimpleBean_GetIntegerProperty() {
	simpleBean.getIntegerProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE, "[ entering < getIntegerProperty > ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE,
		"[ leaving < getIntegerProperty > returning ( 100 ) ]");
    }

    @Test
    public void testSimpleBean_GetStringProperty() {
	simpleBean.getStringProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE, "[ entering < getStringProperty > ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE,
		"[ leaving < getStringProperty > returning ( stringProperty ) ]");
    }
    
    @Test
    public void testSimpleBean_multiParamMethod() {
	simpleBean.multiParamMethod("stringProperty", 100);

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE,
		"[ entering < multiParamMethod > with params ( stringProperty,100 ) ]");
	assertEquals(logger.getMessages(SimpleBean.class).get(1), LogLevel.TRACE, "[ leaving < multiParamMethod > ]");
    }
    
    @Test
    @Ignore("removed this functionality from the logging aspect")
    public void testSimpleBean_throwException() throws Exception {
	try {
	    simpleBean.throwException();
	} catch (Exception ignore) {
	    //ignore exception
	}

	Assert.assertEquals(2, logger.getMessages(SimpleBean.class).size());
	assertEquals(logger.getMessages(SimpleBean.class).get(0), LogLevel.TRACE,"[ entering < throwException > ]");
	LogMessage logMessage = logger.getMessages(SimpleBean.class).get(1);
	assertEquals(logMessage, LogLevel.ERROR,"[ exception thrown from < throwException > exception message \"test exception\" with params (  ) ]");
	Assert.assertTrue(logMessage.getThrowable() instanceof IllegalArgumentException);
    }

    @Test
    public void testSimpleBeanSubclass_SetDateProperty() throws Exception {
	simpleBeanSubclass.setDateProperty(date);

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < setDateProperty > with params ( "+date+" ) ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < setDateProperty > ]");
    }

    @Test
    public void testSimpleBeanSubclass_SetDecimalProperty() {
	simpleBeanSubclass.setDecimalProperty(new BigDecimal("0.25"));

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < setDecimalProperty > with params ( 0.25 ) ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < setDecimalProperty > ]");
    }

    @Test
    public void testSimpleBeanSubclass_SetIntegerProperty() {
	simpleBeanSubclass.setIntegerProperty(100);

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < setIntegerProperty > with params ( 100 ) ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < setIntegerProperty > ]");
    }

    @Test
    public void testSimpleBeanSubclass_SetStringProperty() {
	simpleBeanSubclass.setStringProperty("stringProperty");

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < setStringProperty > with params ( stringProperty ) ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < setStringProperty > ]");
    }

    @Test
    public void testSimpleBeanSubclass_GetDateProperty() {
	simpleBeanSubclass.getDateProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < getDateProperty > ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < getDateProperty > returning ( "+date+" ) ]");
    }

    @Test
    public void testSimpleBeanSubclass_GetDecimalProperty() {
	simpleBeanSubclass.getDecimalProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < getDecimalProperty > ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < getDecimalProperty > returning ( 0.25 ) ]");
    }

    @Test
    public void testSimpleBeanSubclass_GetIntegerProperty() {
	simpleBeanSubclass.getIntegerProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < getIntegerProperty > ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < getIntegerProperty > returning ( 100 ) ]");
    }

    @Test
    public void testSimpleBeanSubclass_GetStringProperty() {
	simpleBeanSubclass.getStringProperty();

	Assert.assertEquals(2, logger.getMessages(SimpleBeanSubclass.class).size());
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(0), LogLevel.TRACE,
		"[ entering < getStringProperty > ]");
	assertEquals(logger.getMessages(SimpleBeanSubclass.class).get(1), LogLevel.TRACE,
		"[ leaving < getStringProperty > returning ( stringProperty ) ]");
    }
    
    private void assertEquals(LogMessage logMessage, LogLevel logLevel, String message) {
	Assert.assertEquals(logLevel, logMessage.getLogLevel());

	Assert.assertEquals(message, logMessage.getMessage());
    }
}
