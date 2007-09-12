package org.cip4.elk;

import java.util.Map;
import java.util.Set;

/**
 * A configuration object for JDF <em>Device</em> s and <em>Controller</em>
 * s. This configuration object should be populated with data during
 * device/controller startup and then passed to any device/controller components
 * that need access to the configuration, for example a
 * {@link org.cip4.elk.queue.Queue Queue}or a
 * {@link org.cip4.elk.device.process.Process Process}.
 * 
 * @todo Does this interface really need to provide modifiers?
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2, 2.1.2 Workflow Component Roles </a>
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface Config {

    /**
     * Returns the device/controller ID. This property maps to several attribute
     * values of JDF elements, among others:
     * <ul>
     * <li><em>JMF/@SenderID</em></li>
     * <li><em>JDFController/@ControllerID</em></li>
     * <li><em>Device/@DeviceID</em></li>
     * </ul>
     * 
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, Table 5-1: Contents of the JMF element
     *      </a>
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 5.5.2 Device/Operator Status and Job
     *      Progress Messages </a>
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, Table 5-21: Contents of the JDFController
     *      element </a>
     * @return the device/controller ID
     */
    public String getID();

    /**
     * Sets the device/controller ID. This property maps to several attribute
     * values of JDF elements, see {@link #getID()}.
     * 
     * @param senderId
     *            the device/controller ID
     * @see #getID()
     */
    public void setID(String senderId);

    /**
     * Returns the value of a configuration parameter.
     * 
     * @param name
     *            the name of the configuration parameter
     */
    public String getConfigParameter(String name);

    /**
     * Returns a set containing the names of the available configuration
     * parameters.
     * 
     * @return the names of the configuration parameters
     */
    public Set getConfigParameterNames();

    /**
     * Sets a map of configuration parameters. The map's key/value pairs must be
     * configuration name/value pairs. All existing configuration parameters are
     * replaced by the ones in the map.
     * 
     * @param parameters
     *            a map of new configuration parameters
     */
    public void setConfigParameters(Map parameters);

    /**
     * Sets a configuration parameter. If a configuration parameter with the
     * specified name already exists it will be replaced.
     * 
     * @param name
     *            the name of the configuration parameter
     * @param value
     *            the value of the configuration parameter
     */
    public void setConfigParameter(String name, String value);

    /**
     * Sets the hostname of the proxy that should be used when accessing remote
     * URLs.
     * 
     * @param host
     *            the proxy's hostname or IP address; use <code>null</code> to
     *            disable the proxy hostname
     */
    public void setProxyHost(String host);

    /**
     * Returns the hostname of the proxy that be used should when accessing
     * remote URLs.
     * 
     * @return the proxy's hostname or IP address; <code>null</code> if no
     *         host is configured
     */
    public String getProxyHost();

    /**
     * Sets the port of the proxy the device/controller should use when
     * accessing remote URLs.
     * 
     * @param port
     *            the proxy's port; use <code>-1</code> to disable the proxy
     *            port
     */
    public void setProxyPort(int port);

    /**
     * Returns the port of the proxy that should be used when accessing remote
     * URLs.
     * 
     * @return the proxy's port; <code>-1</code> if no port is configured
     */
    public int getProxyPort();

    /**
     * Sets the directory where the device/controller should publish processed
     * JDF files and resource files. JDF files should be stored directly in this
     * directory. Resource files should be stored in a sub-directory of the
     * directory. It is recommended that the name of the sub-directory is that
     * of the job ID of JDF that owns the resource files.
     * <p>
     * The <em>local URL</em>, used by this component to write JDF files and
     * resource files to its output directory, will often use the
     * <code>file</code> scheme. Usually the local URL will be mapped in some
     * implementation specific manner to the same location as the
     * <em>public URL</em>({@link #setJDFOutputURL(String)}.
     * </p>
     * <p>
     * <em>
     * The local URL should not be made public. For example, it should never be
     * written into a JDF file that an external component has access to.
     * </em>
     * </p>
     * 
     * @see #setJDFOutputURL(String)
     * @param localUrl
     *            the local URL used for writing files to the device/controller
     *            output directory
     */
    public void setLocalJDFOutputURL(String localUrl);

    /**
     * Returns the URL used by the device/controller to write processed JDF
     * files and resource files to its output directory.
     * <p>
     * This URL should not be made public. When returning the URL of a JDF file
     * that the device/controller has processed the output URL should be used
     * instead.
     * </p>
     * 
     * @see #getJDFOutputURL()
     * @return the URL to the output directory used by the device/controller to
     *         write processed JDF files and resource files; <code>null</code>
     *         if none is configured
     */
    public String getLocalJDFOutputURL();

    /**
     * Returns the URL used by third parties to read JDF files and resource
     * files from the output directory of the device/controller.
     * <p>
     * This property maps to Device/@JDFOutputURL, see the JDF specification.
     * </p>
     * 
     * @see #getLocalJDFOutputURL()
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 7.2.50 Device </a>
     * @return the URL used by this device/controller to write processed JDF
     *         files and resource files to an output directory;
     *         <code>null</code> if none is configured
     */
    public String getJDFOutputURL();

    /**
     * Sets the directory where the device/controller should publish processed
     * JDF files and resource files. This is the URL used by third parties to
     * read JDF files and resources files produced by the device/controller. JDF
     * files should be stored directly in this directory. Resource files should
     * be stored in a sub-directory of the directory. It is recommended that the
     * name of the sub-directory is that of the job ID of JDF that owns the
     * resource files.
     * <p>
     * This property maps to Device/@JDFOutputURL, see the JDF specification.
     * </p>
     * <p>
     * The <em>local JDF output URL</em> will often use the <code>http</code>
     * scheme. Usually the output URL will be mapped in some implementation
     * specific manner to the same location as the <em>local URL</em>(
     * {@link #setJDFOutputURL(String)}).
     * </p>
     * 
     * @see #setLocalJDFOutputURL(String)
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, 7.2.50 Device </a>
     * @param outputUrl
     *            the public URL used for reading files from the
     *            device/controller output directory
     */
    public void setJDFOutputURL(String outputUrl);

    /**
     * Sets the temporary directory where the device/controller temporarily
     * stores JDF files and resource files.
     * 
     * @param tempUrl
     *            the URL to the temporary directory
     */
    public void setJDFTempURL(String tempUrl);

    /**
     * Returns the temporary directory where the device/controller temporarily
     * stores JDF files and resource files.
     * 
     * @return the URL to the temporary directory; <code>null</code> if none
     *         is configured
     */
    public String getJDFTempURL();
}
