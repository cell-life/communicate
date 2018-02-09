package org.celllife.mobilisr.api.mock;

import java.util.Random;

public class MockUtils {
	
	/**
	 * @return string of format ^27[1-9][0-9]{8}$
	 */
	public static String createMsisdn(int seed){
		Random r = new Random(seed);
		StringBuilder b = new StringBuilder();
		b.append("27").append(r.nextInt(9)+1);
		for (int i = 0; i < 8; i++) {
			b.append(r.nextInt(10));
		}
		return b.toString();
	}
	
}
