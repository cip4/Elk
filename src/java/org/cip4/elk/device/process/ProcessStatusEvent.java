package org.cip4.elk.device.process;

import org.cip4.elk.ElkEvent;

import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * An event that is generated each time a process's status changes.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class ProcessStatusEvent extends ElkEvent {
    
    public static final JDFDeviceInfo.EnumDeviceStatus CLEANUP = JDFDeviceInfo.EnumDeviceStatus.Cleanup;
    public static final JDFDeviceInfo.EnumDeviceStatus DOWN = JDFDeviceInfo.EnumDeviceStatus.Down;
    public static final JDFDeviceInfo.EnumDeviceStatus IDLE = JDFDeviceInfo.EnumDeviceStatus.Idle;
    public static final JDFDeviceInfo.EnumDeviceStatus RUNNING = JDFDeviceInfo.EnumDeviceStatus.Running;
    public static final JDFDeviceInfo.EnumDeviceStatus SETUP = JDFDeviceInfo.EnumDeviceStatus.Setup;
    public static final JDFDeviceInfo.EnumDeviceStatus STOPPED = JDFDeviceInfo.EnumDeviceStatus.Stopped;
    public static final JDFDeviceInfo.EnumDeviceStatus UNKNOWN = JDFDeviceInfo.EnumDeviceStatus.Unknown;   
    
    private JDFDeviceInfo.EnumDeviceStatus _status;
    
	/**
     * Creates a new process status event.
	 * @param status   the process's status
	 * @param source   the process that generated this event
	 */
	public ProcessStatusEvent(JDFNotification.EnumClass eventClass, JDFDeviceInfo.EnumDeviceStatus status, Process source, String description) {
		super(eventClass, source, description);
        _status = status;
	}
		
	/**
	 * Returns the process that generated this event.
	 * @return the process that generated this event
	 */
	public Process getProcess() {
		return (Process) getSource();
	}
	
	/**
	 * Returns the status of the process.
	 * @return the process's status
	 */
	public JDFDeviceInfo.EnumDeviceStatus getProcessStatus() {
		return _status;
	}

    public String toString()
    {
        return "ProcessStatusEvent[Status: " + _status + ";  Source: " + getSource() + ";  Time stamp: " + getTimestamp() + "]";
    }
   
}
