/*
 * Created on Sep 10, 2004
 */
package org.cip4.elk.impl.device;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.cip4.elk.DefaultConfig;
import org.cip4.elk.device.DeviceConfig;
import org.cip4.elk.impl.util.URLAccessTool;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.resource.JDFDevice;

/**
 * An object that represents the configuration of a JDF <em>Device</em>.
 * 
 * NOTE: The supported url schemes are hardwired. The Elk Device currently
 * supports these schemes: file, ftp, http, https
 * 
 * @todo modify so that the url schemes are not hard wired (i.e. through
 *       configuration)
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: SimpleDeviceConfig.java 579 2005-07-26 13:18:38Z ola.stering $
 */
public class SimpleDeviceConfig extends DefaultConfig implements DeviceConfig {

    private JDFDevice _jdfDevice;
    private String _deviceConfigUrl;
    private URLAccessTool _urlAccessTool;
    private String urlSchemes = "file ftp http https";

    /**
     * Instantiates a new <code>DeviceConfig</code>. Its configuration must
     * be set using the setter {@link #setDeviceConfig(JDFDevice)}.
     */
    public SimpleDeviceConfig() {
        this(null, null);
    }

    /**
     * Instantiates a new <code>DeviceConfig</code> that loads its
     * configuration from the specified URL using the specified
     * {@link org.cip4.elk.impl.util.URLAccessTool URLAccessTool}.
     * 
     * @param deviceConfigUrl the URL to the Device XML element
     * @param urlAccessTool the <code>URLAccessTool</code> used to access the
     *            URL
     */
    public SimpleDeviceConfig(String deviceConfigUrl,
            URLAccessTool urlAccessTool) {
        super();
        _deviceConfigUrl = deviceConfigUrl;
        _urlAccessTool = urlAccessTool;
    }

    /*
     * @see org.cip4.elk.device.DeviceConfig#getDeviceConfig()
     */
    public synchronized JDFDevice getDeviceConfig() {
        if (_jdfDevice == null) {
            if (_urlAccessTool != null && _deviceConfigUrl != null) {
                try {
                    JDFDevice d = loadDeviceConfiguration(_deviceConfigUrl);
                    _jdfDevice = (JDFDevice) convert2DOMLevel2(d);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return _jdfDevice;
    }

    /**
     * List of supported schemes for retrieving JDF files.
     * 
     * @see org.cip4.elk.device.DeviceConfig#getURLSchemes()
     */
    public String getURLSchemes() {

        return urlSchemes;
    }

    /**
     * Returns the JMFURL of the Device.
     * 
     * @return the JMFURL of the Device.
     * @see org.cip4.elk.device.DeviceConfig#getJMFURL()
     * @throws NullPointerExeception if the <em>JDFDevice</em> of this
     *             configuration is <code>null</code>
     */
    public String getJMFURL() {
        JDFDevice d = getDeviceConfig();
        if (d == null) {
            throw new NullPointerException(
                    "The JDFDevice of this SimpleDeviceConfig is null.");
        }
        return d.getJMFURL();
    }

    /*
     * @see org.cip4.elk.device.DeviceConfig#setDeviceConfig(JDFDevice)
     */
    public synchronized void setDeviceConfig(JDFDevice deviceConfig) {
        _jdfDevice = (JDFDevice) convert2DOMLevel2(deviceConfig);
        super.setID(_jdfDevice.getDeviceID());
    }

    /**
     * This method initiates a JDFElement so that the namespace is set to
     * http://www.CIP4.org/JDFSchema_1_1
     * 
     * This method is needed because there is no easy way to convert an element
     * of DOM level 1 (like the incoming deviceConfig) and a JDFDevice with its
     * (DOM level 2) which must be used to set the namespaces in a correct way.
     * 
     * @param source the KElement which can be DOM level 1 or 2.
     * @param destination root of a DOM level 2 tree if
     *            source.getNamespaceURI().equals("") then source is appended to
     *            destination with the namespace of destination
     * @throws NullPointer if destination is <code>null</code>.
     */
    private void convert2DOMLevel2(JDFElement source, JDFElement destination) {
        if (source != null) {
            if (source.getNamespaceURI().equals("")) {
                JDFElement kElem = (JDFElement) destination
                        .appendElement(source.getNodeName());
                kElem.setAttributes(source.getAttributeMap());

                Vector v = source.getChildElementVector(JDFConstants.WILDCARD,
                    JDFConstants.NONAMESPACE, new JDFAttributeMap(), true, 0);

                for (Iterator it = v.iterator(); it.hasNext();) {
                    JDFElement e = (JDFElement) it.next();
                    convert2DOMLevel2(e, kElem);
                }
            }
        }
    }

    /**
     * This method initiates a JDFElement so that the namespace is set to
     * http://www.CIP4.org/JDFSchema_1_1
     * 
     * This method is needed because there is no easy way to convert an element
     * of DOM level 1 (like the incoming deviceConfig) and a JDFDevice with its
     * (DOM level 2) which must be used to set the namespaces in a correct way.
     * 
     * @param source the KElement which can be DOM level 1 or 2.
     * @return the new JDFElement with its namespace set.
     */
    private JDFElement convert2DOMLevel2(JDFElement source) {
        JDFElement kElem = source;
        if (source != null && source.getNamespaceURI().length() == 0) {
            kElem = new JDFDoc(source.getNodeName()).getRoot();
            kElem.setAttributes(source.getAttributeMap());
            Vector v = source.getChildElementVector(JDFConstants.WILDCARD,
                JDFConstants.NONAMESPACE, new JDFAttributeMap(), true, 0);
            for (Iterator it = v.iterator(); it.hasNext();) {
                convert2DOMLevel2((JDFElement) it.next(), kElem);
            }
        }
        return kElem;
    }

    /**
     * Set the Device/@JMFSenderID, Device/@JMFSenderID of this SimpleDevice.
     * For this device's configuration the DeviceID and the JMFSenderID will
     * always be the same.
     * 
     * To get the these values use getID
     * 
     * @see org.cip4.elk.impl.device.DeviceConfig#getID()
     * 
     * @param id the new id for the Device
     * @throws NullPointerException if id == null
     */
    public void setID(String id) {

        if (id == null)
            throw new NullPointerException("parameter 'id' can not be null");

        _jdfDevice.setJMFSenderID(id);
        _jdfDevice.setDeviceID(id);
        super.setID(id);
    }

    /**
     * Returns a JDF <em>Device</em> resource element that is loaded from the
     * specified URL.
     * 
     * @param deviceConfigUrl a URL to the <em>Device</em> element to return
     * @return the <code>JDFDevice</code> object loaded from the specified URL
     * @throws IOException if an IO problem occurs
     */
    private JDFDevice loadDeviceConfiguration(String deviceConfigUrl)
            throws IOException {
        InputStream inStream = null;
        try {
            inStream = _urlAccessTool.getURLAsInputStream(deviceConfigUrl);
            return (JDFDevice) new JDFParser().parseStream(inStream).getRoot();
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }
}