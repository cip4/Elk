/*
 * Created on Aug 31, 2004
 */
package org.cip4.elk.queue.util;

import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueFilter;

/**
 * An interface for filtering queue entries. 
 *   
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface QueueFilter
{
    /**
     * Filters all queue entries in the specified queue using the specified 
     * filter and returns a new queue that only contains queue entries that 
     * match the filter. The new queue has the same owner Document as the original
     * queue.
     * <p>
     * If input queue does not contain any queue entries then a new queue
     * should be created that is an exact copy of the input queue. 
     * </p><p>
     * If the filter is <code>null</code> then a new queue should be created
     * that is an exact copy of the input queue.
     * </p>
     * @todo Should a new owner Document be created for the new queue?
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, 5.6.5 Queue-Handling Elements</a>
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-102 Queue</a>
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-103 QueueEntry</a>
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-105 QueueFilter</a>
     * @param queue     the queue to filter
     * @param filter    the filter to apply to the queue
     * @return a new queue containing the queue entries that match the filter
     */
    public JDFQueue filterQueue(JDFQueue queue, JDFQueueFilter filter);
}
