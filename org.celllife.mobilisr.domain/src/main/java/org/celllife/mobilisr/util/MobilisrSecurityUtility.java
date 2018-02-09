package org.celllife.mobilisr.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MobilisrSecurityUtility {

	/**
	 * This method will hash <code>strToEncode</code> using the preferred
	 * algorithm.  Currently, Mobilisr's preferred algorithm is hard coded
	 * to be SHA-1.
	 *  
	 * @param strToEncode string to encode
	 * @return the SHA-1 encryption of a given string
	 */
	public static String encodeString(String strToEncode) {
		try{
			String algorithm = "SHA1";
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] input = strToEncode.getBytes(); 
			return hexString(md.digest(input));
		}
		catch(NoSuchAlgorithmException ex){
			ex.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Convenience method to convert a byte array to a string.
	 * This solves a bug in the above method.
	 * 
	 * @param b
	 * @return
	 */
	private static String hexString(byte[] b) {
		StringBuffer buf = new StringBuffer();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		int len = b.length;
		int high = 0;
		int low = 0;
		for (int i = 0; i < len; i++) {
			high = ((b[i] & 0xf0) >> 4);
			low = (b[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		
		return buf.toString();
	}
	
	/**
	 * This method will generate a random string 
	 * 
	 * @return a secure random token.
	 */
	public static String getRandomToken() 
	{
		Random rng = new Random();
		return encodeString(Long.toString(System.currentTimeMillis()) 
				+ Long.toString(rng.nextLong()));
	}
}
