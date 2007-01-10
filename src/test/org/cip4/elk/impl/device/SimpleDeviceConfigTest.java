/*
 * Created on Sep 10, 2004
 */
package org.cip4.elk.impl.device;

import org.cip4.elk.ElkTestCase;
import org.cip4.elk.impl.util.URLAccessTool;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.resource.JDFDevice;

/**
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class SimpleDeviceConfigTest extends ElkTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetDeviceConfig() {
        log.info("Testing SimpleDeviceConfig...");

        URLAccessTool fileUtil = new URLAccessTool(getResourceAsURL("data/")
                .toString());
        String url = "DeviceWithoutNamespace.xml"; // data/Device.xml
        SimpleDeviceConfig config = new SimpleDeviceConfig(url, fileUtil);
        JDFDevice device1 = config.getDeviceConfig();
        assertNotNull(device1);
        JDFDevice device2 = (JDFDevice) new JDFParser().parseStream(
            getResourceAsStream("data/DeviceExcpected.xml")).getRoot();
        assertNotNull(device2);
        config.setDeviceConfig(device2);
        // log.debug(device1);
        // log.debug(device2);
        log.info("This test must be looked at manually. The Device both"
                + " devices should look the same except for the order of the"
                + " attributes.");
        log.info("Finished testing SimpleDeviceConfig...");

    }

}
