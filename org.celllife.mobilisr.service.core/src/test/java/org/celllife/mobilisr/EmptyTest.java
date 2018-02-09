package org.celllife.mobilisr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class EmptyTest {

    private static Logger log = LoggerFactory.getLogger(EmptyTest.class);

    @Test
    public void testApplicationContext() {
		log.trace("---------- test trace log ----------");
		log.debug("---------- test debug log ----------");
		log.info("---------- test info log ----------");
		log.warn("---------- test warn log ----------");
		log.error("---------- test error log ----------");
    }

}
