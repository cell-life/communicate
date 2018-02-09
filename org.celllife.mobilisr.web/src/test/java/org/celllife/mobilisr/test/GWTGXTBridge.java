package org.celllife.mobilisr.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.dev.About;

/**
 * A dummy implementation of {@link GWTBridge}, which instantiates nothing.
 * 
 * @see MobGWTMockUtilities
 */
class GWTGXTBridge extends GWTBridge {
  private static final Logger logger = Logger.getLogger(GWTGXTBridge.class.getName());

  /**
   * @return null
   */
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> classLiteral) {
		// if we try to create an instance of BeanModelLookup,
		// return an instance of my own BeanModelLookup
		// otherwise return null
		T obj = null;
		if (BeanModelLookup.class.equals(classLiteral)) {
			obj = (T) new TestBeanModelLookup();
		}
		return obj;
	}

  /**
   * @return the current version of GWT ({@link About#getGwtVersionNum()})
   */
  public String getVersion() {
    return About.getGwtVersionNum();
  }

  /**
   * @return false
   */
  public boolean isClient() {
    return true;
  }

  /**
   * Logs the message and throwable to the standard logger, with level {@link
   * Level#SEVERE}.
   */
  public void log(String message, Throwable e) {
    logger.log(Level.SEVERE, message, e);
  }
}
