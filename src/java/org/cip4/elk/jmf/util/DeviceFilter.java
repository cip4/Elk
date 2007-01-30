/*
 * Created on 2005-mar-09
 *
 */
package org.cip4.elk.jmf.util;

import org.cip4.jdflib.jmf.JDFDeviceFilter;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * An interface for filtering devices  
 * 
 * @author Ola Stering, olst6875@student.uu.se 
 */
public interface DeviceFilter {
	
	/**
	 * Filters all the devices in the devicelist according to filter.
	 * 
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-24 DeviceList</a>
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-23 DeviceFilter</a>
     * @param devicelist the list of devices to filter
     * @param filter     the filter to apply to the devicelist
     * @return a new device list, with only devices and fields which matches the filter
     */
    public JDFDeviceList filterDeviceList(JDFDeviceList devicelist, JDFDeviceFilter filter);
    
    
}
