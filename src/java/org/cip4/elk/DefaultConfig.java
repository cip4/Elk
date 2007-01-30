package org.cip4.elk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is a default implementation of the functionality of a configuration.
 * <p>
 * <strong>Note: </strong> This implementation builds on
 * {@link java.util.HashMap HashMap}and is not thread-safe for reading and
 * writing at the same time. However, in most circumstances this configuration
 * will only be written during device/controller startup and only read during
 * execution, so this is acceptable.
 * </p>
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class DefaultConfig implements Config {

    private String _id;
    private String _proxyHost;
    private int _proxyPort;
    private String _localJdfOutputUrl;
    private String _jdfOutputUrl;
    private String _jdfTempUrl;

    private Map _params = new HashMap();

    public DefaultConfig() {
        _id = "No ID configured";
        _proxyHost = null;
        _proxyPort = -1;
        _jdfTempUrl = null;
        _jdfOutputUrl = null;
        _localJdfOutputUrl = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#getComponentID()
     */
    public String getID() {
        return _id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#setComponentID(java.lang.String)
     */
    public void setID(String componentId) {
        if (componentId == null) {
            throw new IllegalArgumentException(
                    "Parameter 'componentId' must not be null.");
        }
        _id = componentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#getConfigParameter(java.lang.String)
     */
    public synchronized String getConfigParameter(String name) {
        return (String) _params.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#getConfigParameterNames()
     */
    public synchronized Set getConfigParameterNames() {
        return Collections.unmodifiableSet(_params.entrySet());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#setConfigParameters(java.util.Map)
     */
    public synchronized void setConfigParameters(Map parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException(
                    "Parameter 'parameters' must not be null.");
        }
        // TODO Validate that all parameter keys and values are strings
        _params.clear();
        _params.putAll(parameters);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#setConfigParameter(java.lang.String,
     *      java.lang.String)
     */
    public synchronized void setConfigParameter(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Parameter 'name' must not be null.");
        } else if (value == null) {
            throw new IllegalArgumentException(
                    "Parameter 'value' must not be null.");
        }
        _params.put(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#setProxyHost(java.lang.String)
     */
    public void setProxyHost(String host) {
        _proxyHost = host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#getProxyHost()
     */
    public String getProxyHost() {
        return _proxyHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#setProxyPort(java.lang.String)
     */
    public void setProxyPort(int port) {
        _proxyPort = port;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.Config#getProxyPort()
     */
    public int getProxyPort() {
        return _proxyPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#setLocalJDFOutputURL(java.net.String)
     */
    public void setLocalJDFOutputURL(String localUrl) {
        _localJdfOutputUrl = localUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#getLocalJDFOutputURL()
     */
    public String getLocalJDFOutputURL() {
        return _localJdfOutputUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#getJDFOutputURL()
     */
    public String getJDFOutputURL() {
        return _jdfOutputUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#setJDFOutputURL(String)
     */
    public void setJDFOutputURL(String outputUrl) {
        _jdfOutputUrl = outputUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#setJDFTempURL(String)
     */
    public void setJDFTempURL(String tempUrl) {
        _jdfTempUrl = tempUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cip4.elk.device.DeviceConfig#getJDFTempURL()
     */
    public String getJDFTempURL() {
        return _jdfTempUrl;
    }

}