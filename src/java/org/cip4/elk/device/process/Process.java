/*
 * Created on Aug 27, 2004
 */
package org.cip4.elk.device.process;

import org.cip4.elk.device.DeviceConfig;
import org.cip4.elk.impl.jmf.preprocess.JDFPreprocessor;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.node.JDFNode;

/**
 * An interface for implementing a JDF <em>Device</em>'s process. A
 * <code>Process</code> is owned by a device and is responsible for executing
 * the jobs on its device's {@link org.cip4.elk.queue.Queue Queue}. For
 * example, if a device were to implement the JDF specification's
 * <em>Screening</em> process then it would have a <code>Process</code>
 * class that would control the <em>Machine</em> that performs the actual
 * screening.
 * <p>
 * This interface is usually the only thing you need to implement when you want
 * to implement a process from the JDF specification. In general, the
 * implementations of the rest of the Elk interfaces can be reused by different
 * process implementations once they have been written.
 * </p>
 * 
 * @author Claes Buckwalter (clabu@itn.lu.se)
 * @version
 * @version $Id$
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2 </a>
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2, 5.5.2 Device/Operator Status and Job Progress
 *      Messages </a>
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2, 6 Processes </a>
 */
public interface Process {
    /**
     * Calling this method will initialize this process. This should be is
     * called when this process is loaded at device startup or if this process
     * is restarted. Typically, implementations of this method would load any
     * process specific configuration data and any a previously persisted state.
     * When this method returns this process must be ready to run jobs.
     */
    public void init();

    /**
     * Calling this method will shut down this process. Typically,
     * implementations of this method would persist the process's state so that
     * execution can continue when the process is started again.
     * <p>
     * This method maps to the JMF command <em>Shutdown</em> with
     * <em>ShutDownCmdParams/@ShutdownType="Full"</em>.
     * </p>
     */
    public void destroy();

    /**
     * Sets this process's configuration. A process usually shares its
     * configuration object with the device that owns it.
     * 
     * @see org.cip4.elk.DeviceConfig
     * @param config this process's configuration
     */
    public void setDeviceConfig(DeviceConfig config);

    /**
     * Returns this process's configuration.
     * 
     * @see org.cip4.elk.DeviceConfig
     * @return this process's configuration
     */
    public DeviceConfig getDeviceConfig();

    /**
     * Adds a listener that should receive events whenever this process's status
     * changes.
     * 
     * @param listener the object that wants to listen to this process
     */
    public void addProcessStatusListener(ProcessStatusListener listener);
    
    /**
     * Removes a listener.
     * 
     * @see #addProcessStatusListener(ProcessStatusListener)
     * @param listener the object that has listened to this process
     */
    public void removeProcessStatusListener(ProcessStatusListener listener);

    /**
     * Adds a listener that should receive events whenever a QueueEntry is run
     * on this process's and this process changes its status. changes.
     * 
     * @param listener the object that wants to listen to this process
     */
    public void addQueueEntryEventListener(
            ProcessQueueEntryEventListener listener);

    /**
     * Removes a ProcessQueueEntryEvent listener.
     * 
     * @see #addQueueEntryEventListener(ProcessQueueEntryEventListener)
     * @param listener the object that has listened to this process
     */
    public void removeQueueEntryEventListener(
            ProcessQueueEntryEventListener listener);

    /**
     * Runs a JDF job located at the specified URL. This process should send
     * {@link ProcessStatusEvent}s to its listeners reporting any status
     * changes that it goes through during the job run.
     * 
     * @param jdfUrl the URL to the JDF job to execute
     * @return the resulting JDF node after the job run has finish
     */
    public JDFNode runJob(String jdfUrl) throws Exception;

    /**
     * Suspends the job currently running. This process should send
     * {@link ProcessStatusEvent}s to its listeners reporting any status
     * changes it goes through as a result of this method being called.
     */
    public void suspendJob();

    /**
     * Resumes the job that has been suspended. This process should send
     * {@link ProcessStatusEvent}s to its listeners reporting any status
     * changes it goes through as a result of this method being called.
     */
    public void resumeJob();

    /**
     * Aborts the currently running job. This process should send
     * {@link ProcessStatusEvent}s to its listeners reporting any status
     * changes it goes through as a result of this method being called.
     */
    public void abortJob();

    /**
     * Test runs a job located at the specified URL. This process should send
     * {@link ProcessStatusEvent}s to its listeners reporting any status
     * changes it goes through as a result of this method being called.
     * 
     * @param jdfUrl the URL to the JDF job to test run
     */
    public void testRunJob(String jdfUrl);

    /**
     * Returns this process's current status.
     * 
     * @return the status of this process
     */
    public JDFDeviceInfo.EnumDeviceStatus getStatus();

    /**
     * Returns the JDF process type this <code>Process</code> implements. The
     * returned String can be any JDF process name, see <a
     * href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     * Specification Release 1.2, 6 Processes </a>.
     * 
     * @return a JDF process name
     */
    public String getProcessType();

    //    TODO Support a "Combined" Process
    //    /**
    //     * Returns an array of names of the JDF processes that this
    // <code>Process</code>
    //     * implements. The returned array is of length 1 if this
    // <code>Process</code>
    //     * only implements a single JDF process. A <code>Process</code> that
    // executes
    //     * more than one JDF process is called a <em>Combined Process</em>, see <a
    // href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
    // Specification Release 1.2, 3.1.5 Combined Process Nodes</a>.
    //     * The order of the array elements is should be the order in which this
    //     * <code>Process</code> supports executing the JDF processes.
    //     *
    //     * @return an array of the JDF process names this <code>Process</code>
    // implements
    //     */
    //    public String[] getProcessTypes();

    /**
     * Returns the phase of the job currently running on this process.
     * 
     * @return a JobPhase element describing the active job; <code>null</code>
     *         if no job is active
     */
    public JDFJobPhase getJobPhase();

    /**
     * Returns the <em>DeviceInfo</em> of the Device including the
     * <em>JobPhase</em> element in case the includeJobPhase parameter is set
     * to true and the device is currently processing a job.
     * 
     * @param includeJobPhase Indicates if the <em>JobPhase</em> element
     *            should be included in the <em>DeviceInfo</em> element.
     * @return The DeviceInfo element of the Device.
     */
    public JDFDeviceInfo getDeviceInfo(boolean includeJobPhase);

    /**
     * Flushes any temporary resources used by this process. Both general
     * temporary resources used by this process and any resources related to the
     * currently running job, if any, should be flushed.
     */
    public void flushResources();

    /**
     * Wakes up the device from standby. This methods maps to the JMF command
     * <em>WakeUp</em>.
     * 
     * @see #sleep()
     */
    public void wakeUp();

    /**
     * Sets the device in sleep mode. This methods maps to the JMF command
     * <em>Shutdown</em> <em>ShutDownCmdParams/@ShutdownType="StandBy"</em>.
     * 
     * @see #wakeUp()
     */
    public void sleep();

    /**
     * Returns this process's ID.
     * 
     * @return this process's ID
     */
    public String getProcessId();
    
    /**
     * set the local preprocessor to pre
     */
    public void setPreprocessor(JDFPreprocessor pre);
}
