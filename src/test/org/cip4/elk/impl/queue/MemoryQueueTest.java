/*
 * Created on Sep 19, 2004
 */
package org.cip4.elk.impl.queue;

import java.io.InputStream;
import java.util.Vector;

import org.cip4.elk.Config;
import org.cip4.elk.DefaultConfig;
import org.cip4.elk.ElkTestCase;
import org.cip4.elk.impl.util.URLAccessTool;
import org.cip4.elk.queue.Queue;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.pool.JDFAncestorPool;

/**
 * NOTE: To run this test, a folder named data containing the file:
 * SubmitQueueEntry.jmf must exists.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id$
 */
public class MemoryQueueTest extends ElkTestCase {

    String fileAncectorPool0parts = _jdfFilesPath + "AncestorPoolTest0.jdf";
    String fileAncectorPool1parts = _jdfFilesPath + "AncestorPoolTest.jdf";
    String fileAncectorPool2parts = _jdfFilesPath + "AncestorPoolTest2.jdf";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testInitialValues() {        
        int maxQueueSize = 10;
        Queue q = createQueue(maxQueueSize);
        assertTrue(q.getMaxQueueSize() == maxQueueSize);
        assertTrue(q.getQueueSize() == 0);
        assertEquals(q.getQueueStatus(), JDFQueue.EnumQueueStatus.Waiting);
        assertTrue(q.getTotalQueueSize() == 0);
        assertNull(q.getFirstRunnableQueueEntry());
        assertNull(q.getQueueEntry("100"));
    }

    public void testAddQueueEntry() {
        // Loads a JMF that references a JDF file /tmp/Approval.jdf
        JDFQueueSubmissionParams qsp = loadQueueSubmissionParams();
        assertTrue(qsp.isValid());
        Queue q = createQueue(10);
        // Adds a queue entry
         JDFQueueEntry qe1 = q.addQueueEntry(qsp);
        assertTrue(qe1.isValid());
        assertTrue(q.getQueueSize() == 1);
        // Tests that the queue entry is copied before it is returned
        JDFQueueEntry qe2 = q.getQueueEntry(qe1.getQueueEntryID());
        assertNotSame(qe1, qe2);
        assertNotSame(qe1.getOwnerDocument(), qe2.getOwnerDocument());
        assertEquals(qe1.toString(), qe2.toString());
        log.info(qe1);
        // Tests that the queue is copied before it is returned
        JDFQueue q1 = q.getQueue();
        JDFQueue q2 = q.getQueue();
        assertNotSame(q1, q2);
        assertNotSame(q1.getOwnerDocument(), q2.getOwnerDocument());
        assertEquals(q1.toString(), q2.toString());
        log.info(q1);
    }

    public void testPutQueueEntry() {
        JDFQueueSubmissionParams qsp = loadQueueSubmissionParams();
        Queue q = createQueue(10);
        // Adds a queue entry
        JDFQueueEntry qe = q.addQueueEntry(qsp);
        assertTrue(qe.isValid());
        qe.setQueueEntryID("QueueEntryID");
        qe.setDescriptiveName("This queue entry was put");
        // Puts a new queue entry
        q.putQueueEntry(qe);
        assertTrue(q.getQueueSize() == 2);
        // Replaces an old queue entry
        q.putQueueEntry(qe);
        assertTrue(q.getQueueSize() == 2);
        log.info(q.getQueue());
    }

    public void testRemoveQueueEntry() {
        JDFQueueSubmissionParams qsp = loadQueueSubmissionParams();
        Queue q = createQueue(3);
        // Adds 3 queue entries
        JDFQueueEntry qe = q.addQueueEntry(qsp);
        q.addQueueEntry(qsp);
        q.addQueueEntry(qsp);
        assertEquals(q.getQueueStatus(), JDFQueue.EnumQueueStatus.Full);
        // Remove 1 queue entry
        q.removeQueueEntry(qe.getQueueEntryID());
        assertEquals(q.getQueueStatus(), JDFQueue.EnumQueueStatus.Waiting);
    }

    public void testAncestorPool() {
        JDFQueueSubmissionParams qsp = loadQueueSubmissionParams();
        Queue q = createQueue(3);
        JDFQueueEntry qe0 = q.addQueueEntry(qsp);
        JDFQueueEntry qe1 = q.addQueueEntry(qsp);
        JDFQueueEntry qe2 = q.addQueueEntry(qsp);

        log.info("Tests the AncestorPool/Part...");

        try {
            JDFNode jdf0 = (JDFNode) getResourceAsJDF(fileAncectorPool0parts);
            JDFNode jdf1 = (JDFNode) getResourceAsJDF(fileAncectorPool1parts);
            JDFNode jdf2 = (JDFNode) getResourceAsJDF(fileAncectorPool2parts);

            log.info("Testing with 0 Part elements.");
            assertEquals(testParts(jdf0, qe0), 0);
            log.info("Testing with 1 Part element.");
            assertEquals(testParts(jdf1, qe1), 1);
            log.info("Testing with 2 Part elements.");
            assertEquals(testParts(jdf2, qe2), 2);

        } catch (Exception e) {
            log.error("One of the files '" + fileAncectorPool0parts + "', '"
                    + fileAncectorPool1parts + "' and '"
                    + fileAncectorPool2parts + "' is not on the classpath");
            assertTrue(false);
        }

    }

    /**
     * @param jdf1
     * @param q
     */
    private int testParts(JDFNode jdf1, JDFQueueEntry qe) {

        JDFAncestorPool ancestorPool = jdf1.getAncestorPool();

        if (ancestorPool != null) {
            Vector partVector = ancestorPool.getPartMapVector().getVector();
            int vsize = partVector.size();
            log.debug("The JDFNode contained an AncestorPool with " + vsize
                    + " Part elements.");
            for (int i = 0; i < vsize; i++) {
                qe.appendPart().setAttributes(
                    (JDFAttributeMap) partVector.get(i));
            }
        }

        return qe.getPartMapVector().size();

    }

    public void testAddMaxEntries() {
        JDFQueueSubmissionParams qsp = loadQueueSubmissionParams();
        Queue q = createQueue(3);
        // Adds 3 queue entries
        assertNotNull(q.addQueueEntry(qsp));
        assertNotNull(q.addQueueEntry(qsp));
        assertNotNull(q.addQueueEntry(qsp));
        // Tests that the queue's size is exhausted
        assertTrue(q.getQueueSize() == q.getTotalQueueSize());
        assertTrue(q.getMaxQueueSize() == q.getQueueSize());
        // XXXq.setQueueStatus(JDFQueue.EnumQueueStatus.Full);
        assertEquals(q.getQueueStatus(), JDFQueue.EnumQueueStatus.Full);
        // Tests that another queue entry cannot be added
        assertNull(q.addQueueEntry(qsp));
        log.info("Max size: " + q.getMaxQueueSize());
        log.info("Current size: " + q.getQueueSize());
        assertTrue(q.getMaxQueueSize() == q.getQueueSize());
        assertEquals(q.getQueueStatus(), JDFQueue.EnumQueueStatus.Full);
    }

    private JDFQueueSubmissionParams loadQueueSubmissionParams() {
        InputStream in = getResourceAsStream(_testDataPath + "SubmitQueueEntry.jmf");
        return new JDFParser().parseStream(in).getJMFRoot().getCommand()
                .getQueueSubmissionParams(0);
    }

    private Queue createQueue(int size) {
        return createQueue(null, size, null);
    }

    private Queue createQueue(Config config, int maxSize, URLAccessTool fileUtil) {
        if (config == null) {
            config = new DefaultConfig();
        }
        if (fileUtil == null) {
            fileUtil = new URLAccessTool(getResourceAsURL(".").toString());
        }
        return new MemoryQueue(config, maxSize, fileUtil);
    }
}
