/*
 * Created on Sep 10, 2004
 */
package org.cip4.elk.device;

import java.util.List;

import org.cip4.elk.Config;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFDevice;

/**
 * A configuration object for JDF <em>Device</em>s.
 * 
 * @see org.cip4.elk.Config
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @author Ola Stering (olst6875@student.uu.se)
 * @author Rainer Prosi (rainer.prosi@heidelberg.com)
 * @version $Id: DeviceConfig.java,v 1.5 2006/11/17 15:44:09 buckwalter Exp $
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

    /**
     * Returns a list of all JDF nodes that match the device's capabilites. Note
     * that this does not mean that all JDF nodes returned are executable; a
     * node may match the device's capabilities, but all input resources may not
     * be available.
     * 
     * @param jdf
     *            the JDF instance to get process nodes from
     * @return A list of <code>JDFNode</code>s that match the device's
     *         capabilities. This list will be empty (size 0) if no nodes match.
     * @author prosi
     */
    public List getProcessableNodes(JDFNode jdf);

}
