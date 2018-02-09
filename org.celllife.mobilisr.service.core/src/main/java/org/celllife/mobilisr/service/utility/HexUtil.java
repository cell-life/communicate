package org.celllife.mobilisr.service.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexUtil {

    private static Logger log = LoggerFactory.getLogger(HexUtil.class);

    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        try {
            for (int i = 0; i < hex.length() - 1; i += 2) {
                String output = hex.substring(i, (i + 2)); //grab the hex (consisting of 2 chars)
                // TODO: might need a more sophisticated way of doing this. Basically, different phones send different HEX formats eg. 004D004D0043 vs. 4D4D43.
                if (!output.equals("00")) {
                    int decimal = Integer.parseInt(output, 16); //convert hex to decimal
                    sb.append((char) decimal); //convert the decimal to character
                    temp.append(decimal);
                }
            }
        } catch (Exception e) {
            log.warn("Could not process HEX message: " + hex);
            return "";
        }

        return sb.toString().trim();
    }
}
