/*
 * Created on Sep 2, 2004
 */
package org.cip4.elk.impl.device;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.cip4.elk.ElkTestCase;
import org.cip4.elk.device.DeviceConfig;
import org.cip4.elk.impl.device.process.ApprovalProcess;
import org.cip4.elk.impl.jmf.util.Messages;
import org.cip4.elk.impl.queue.MemoryQueue;
import org.cip4.elk.impl.util.FileRepository;
import org.cip4.elk.impl.util.Repository;
import org.cip4.elk.impl.util.URLAccessTool;
import org.cip4.elk.queue.Queue;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.node.JDFNode;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class ApprovalProcessTest extends ElkTestCase
{
    private static String _jdf;
    private static String _jdfInvalid;
    private static URLAccessTool _fileUtil;
    private static BeanFactory _factory;
    
    public void setUp() throws Exception
    {
        super.setUp();
        _fileUtil = new URLAccessTool(getResourceAsURL(".").toString());
        _jdf = _jdfFilesPath + "Approval.jdf";
        _jdfInvalid = _jdfFilesPath + "ApprovalInvalid.jdf";
        _factory = (BeanFactory) new ClassPathXmlApplicationContext(_testDataPath + "elk-spring-config.xml");
        
    }
    
    public ApprovalProcess createProcess() {
        DeviceConfig config = (DeviceConfig) _factory.getBean("deviceConfig");
        Queue queue = new MemoryQueue(config, 10, _fileUtil);
        
        Repository r = null;
        try {
            r = new FileRepository();
        } catch (IOException e) {
            assertTrue(false);
            e.printStackTrace();
        }
        return new ApprovalProcess(config, queue, _fileUtil, null,r);
    }

    /**
     * Tests to see that the that expected number of JMF messages are 
     * sent by the device under test.
     * 
     * Runs a job and the aborts the job. Then waits for a JMF Signal
     * to be delivered with device status "Idle". 
     * 
     * TODO Test that the messages are of the correct type and order. 
     */
    public void testRunJob() throws Exception
    {
        log.debug("Testing Approval process...");
        ApprovalProcess device = createProcess();
        // Add listener
        DeviceHandler dh = new DeviceHandler();
        device.addProcessStatusListener(dh);
        // Run job
        device.init();
        URL url = getResourceAsURL(_jdf);
        log.debug("Attempted to fetch resource with url: '" + url + "'");
        if(url == null ){
            log.error("Unable to fetch " + _jdf);
        }
        device.runJob(getResourceAsURL(_jdf).toString());
        // Wait for a message telling us that the device is idle
        dh.waitForDeviceToBecomeIdle(20000);
        
        // Log messages
        log.info("The device sent " + dh.getReceviedMessageCount() + " messages:");
        List events = dh.getReceivedMessages();
        for(int i=0, imax=events.size(); i<imax; i++) {
            log.info(events.get(i).toString());
        }
        // Test that the device change state for times:
        // Unknown > Idle > Setup > Running > Cleanup 
        // The Process is not set to IDLE again until the JDF is run in its whole, runJob(JDFQueueEntry) > Idle
        assertTrue(dh.getReceviedMessageCount() == 4);
        
        log.info("Testing an invalid Approval Node, should generate a Notification in the AuditPool");
        url = getResourceAsURL(_jdfInvalid);
        if(url == null ){
            log.error("Unable to fetch " + _jdfInvalid);
        }
        device.runJob(url.toString());
    }
    
    /**
     * This tests loads a JDF and adds a NodeInfo element with a SubmitQueueEntry
     * command. This test does not test any aspect of Elk, rather it tests JDFLib-J.
     * This test should probably be removed.
     *   
     * @throws Exception
     */
    public void testGetJdfReturnUrl() throws Exception {
        JDFNode n = (JDFNode) getResourceAsJDF(_jdf);
        log.info("Testing JDFNode.getSubmissionParams and JDFNode.getNodeInfo().getTargetRoute()");
        JDFCommand c = Messages.createCommand("SubmitQueueEntry");
        JDFQueueSubmissionParams p = c.appendQueueSubmissionParams();
        p.setReturnURL("http://elk.itn.liu.se/elk/jmf");
        assertNotNull(p.getReturnURL());
        log.info("The call submissionParams.getReturnURL() never returns Null and throws NullPointerException if submissionParams == null");
        log.info("Throws NullPointerException: n.getNodeInfo().getTargetRoute()");
        JDFNodeInfo nodeInfo = n.appendNodeInfo();
        nodeInfo.setTargetRoute("http://elk.itn.liu.se/elk/jmf");
        assertNotNull(nodeInfo.getTargetRoute());
        log.info("The call ni.getTargetRoute() never returns null and throws NullPointerException if nodeInfo == null.");        
    }
    
    
//    public void testSuspendJob()
//    {
//        ApprovalProcess device = new ApprovalProcess();
//        device.setFileUtil(_fileUtil);
//        // Init
//        device.init();
//        DeviceHandler dh = new DeviceHandler();
//        device.addDeviceListener(dh);
//        // Test the number of messages generated during a job run
//        device.runJob(getResourceAsURL(_jdf));
//        device.suspendJob();
//        // Wait for a message telling us that the device is idle
//        dh.waitForDeviceToBecomeIdle(10000);
//        // Log messages
//        log.info("The device sent " + dh.getReceviedMessageCount() + " messages:");
//        List events = dh.getReceivedMessages();
//        for(int i=0, imax=events.size(); i<imax; i++) {
//            log.info(events.get(i).toString());
//        }
//        // Test the number of messages during initialization
//        //assertTrue(dh.getReceviedMessageCount() == 2)
//    }

    
//    public void testTestRunJob() {
//    }

    /**
     * Tests to see that the that expected number of JMF messages are 
     * sent by the device under test.
     *
     * Runs a job and the aborts the job. Then waits for a JMF Signal
     * to be delivered with device status "Idle".
     * 
     * @see #statusChange(DeviceEvent)
     * @throws Exception
     */
//    public void testAbortJob() throws Exception
//    {
//        
//        ApprovalProcess device = createProcess();
//        // Init
////        device.init();
//        // Add listener
//        DeviceHandler dh = new DeviceHandler();
//        device.addDeviceListener(dh);
//        device.runJob(getResourceAsURL(_jdf));
//        // Abort job
//        device.abortJob();
//        // Wait for device
//        dh.waitForDeviceToBecomeIdle(10000);
//        // Log messages
//        log.info("The device sent " + dh.getReceviedMessageCount() + " messages:");
//        List events = dh.getReceivedMessages();
//        for(int i=0, imax=events.size(); i<imax; i++) {
//            log.info(events.get(i).toString());
//        }
//    }

//    public void testResumeJob() {   
//    }
    
}
