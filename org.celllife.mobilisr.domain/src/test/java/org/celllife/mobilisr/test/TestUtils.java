package org.celllife.mobilisr.test;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

public class TestUtils {
	
	/**
	 * @see http://www.techper.net/2009/06/05/how-to-acess-target-object-behind-a-spring-proxy/
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({"unchecked"})
	public static <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
	  if (AopUtils.isJdkDynamicProxy(proxy)) {
	    return (T) ((Advised)proxy).getTargetSource().getTarget();
	  } else {
	    return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
	  }
	}
	
	public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
	" elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut" +
	" enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip" +
	" ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate" +
	" velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat" +
	" cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est" +
	" laborum.";

	public static String getLoremIpsum(int numCharacters) {
		String strLorem;
		if (numCharacters <= LOREM_IPSUM.length() ) {
			strLorem = LOREM_IPSUM.substring(0, numCharacters).trim();
		}
		else {
			StringBuilder tmpString = new StringBuilder(LOREM_IPSUM + " " + LOREM_IPSUM);
			while (tmpString.length() < numCharacters)
				tmpString.append(LOREM_IPSUM);
			strLorem = tmpString.substring(0, numCharacters).trim();
		}

		// We might have lost 1 character if it was a space
		while (strLorem.length() < numCharacters)
			strLorem = strLorem + ";";
		return strLorem;
	}

}
