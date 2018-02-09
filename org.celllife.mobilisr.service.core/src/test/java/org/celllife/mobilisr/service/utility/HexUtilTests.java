package org.celllife.mobilisr.service.utility;

import junit.framework.Assert;
import org.junit.Test;

public class HexUtilTests {

    @Test
    public void testConvertHexToString() {
       String hexValue = "4D4D430D0A";
        Assert.assertEquals("MMC",HexUtil.convertHexToString(hexValue));
    }

    @Test
    public void testConvertHexToString_invalidHex() {
        String hexValue = "4X4D430D0X";
        Assert.assertEquals("",HexUtil.convertHexToString(hexValue));
    }
}
