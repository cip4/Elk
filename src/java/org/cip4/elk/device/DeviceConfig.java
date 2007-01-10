/*
 * Created on Sep 10, 2004
 */
package org.cip4.elk.device;

import org.cip4.elk.Config;
import org.cip4.jdflib.resource.JDFDevice;

/**
 * A configuration object for JDF <em>Device</em>s.
 * 
 * @see org.cip4.elk.Config
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id$  
 */
public interface DeviceConfig extends Config {

   /**
     * Returns a string containing a space separated list of schemes supported
     * for retrieving JDF files. Example: "file ftp http https".
     * 
     * @return a string containing a space separated list of supported schemes
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 5.6.4.8 SubmissionMethods </a>
     */
    public String getURLSchemes();

    /**
     * URL of the device that will accept JMF messages.
     * 
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 7.2.50 Device </a>
     * @see com.heidelberg.JDFLib.resource.JDFDevice
     * @return the URL of the device that accepts JMF messages,
     *         <code>null</code> if none is configured
     */
    public String getJMFURL();

    /**
     * Returns a JDF <em>Device</em> resource element that represents a device
     * configuration.
     * 
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 7.2.50 Device </a>
     * @see com.heidelberg.JDFLib.resource.JDFDevice
     * @return a <code>JDFDevice</code> representing the device's
     *         configuration; <code>null </code> if none is configured
     */
    public JDFDevice getDeviceConfig();

    /**
     * Sets a JDF <em>Device</em> resource element that represents a device
     * configuration.
     * 
     * @param device
     *            the <code>JDFDevice</code> representing a device's
     *            configuration
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 7.2.50 Device </a>
     */
    public void setDeviceConfig(JDFDevice device);
}