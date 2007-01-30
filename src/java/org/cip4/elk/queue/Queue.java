package org.cip4.elk.queue;

import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;

/**
 * An interface for a JDF queue. This interface provides methods for controlling
 * the queue:
 * <ul>
 * <li>{@link #holdQueue()}</li>
 * <li>{@link #resumeQueue()}</li>
 * <li>{@link #closeQueue()}</li>
 * <li>{@link #openQueue()}</li>
 * </ul>
 * methods for manipulating the queue's data structures:
 * <ul>
 * <li>{@link #addQueueEntry(JDFQueueSubmissionParams)}</li>
 * <li>{@link #putQueueEntry(JDFQueueEntry)}</li>
 * <li>{@link #removeQueueEntry(String)}</li>
 * <li>{@link #setMaxQueueSize(int)}</li>
 * </ul>
 * and methods for retrieving the queue's contents and state:
 * <ul>
 * <li>{@link #getQueue()}</li>
 * <li>{@link #getQueue(JDFQueueFilter)}</li>
 * <li>{@link #getQueueEntry(String)}</li>
 * <li>{@link #getFirstRunnableQueueEntry()}</li>
 * <li>{@link #getQueueSubmissionParams(String)}</li>
 * <li>{@link #getQueueStatus()}</li>
 * <li>{@link #getQueueSize()}</li>
 * <li>{@link #getTotalQueueSize()}</li>
 * </ul>
 * <strong>Note: </strong> The JMF messages <em>FlushQueue</em> and
 * <em>SubmissionMethods</em> to not have methods in the <code>Queue</code>
 * interface. Instead, it is recommended that this functionality be implemented
 * in {@link org.cip4.elk.jmf.JMFProcessor JMFProcessor}s that handle these
 * message types. For example, a <code>JMFProcessor</code> handling
 * <em>FlushQueue</em> messages could interpret the <em>FlushQueueParams</em>
 * and delete the corresponding queue entries using
 * {@link #removeQueueEntry(String)}. However, although not recommended, there
 * is nothing that prevents a <code>Queue</code> implementation from also
 * being a <code>JMFProcessor</code> that handles one or more message types.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2, 5.6 Queue Support </a>
 * @see <a
 *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
 *      Specification Release 1.2, 5.6.5 Queue-Handling Elements </a>
 * @version $Id: Queue.java 468 2005-06-07 18:47:14Z ola.stering $
 */
public interface Queue {

    /**
     * Sets this maximum number of queue entries permitted in the queue that do
     * not have status <em>Aborted</em> or <em>Completed</em>.
     * 
     * @param size the queue's maximum size; use <code>-1</code> for an
     *            unrestricted queue size
     * @see #getQueueSize()
     * @see #getTotalQueueSize()
     * @throws IllegalStateException if this queue does not permit its size to
     *             be changed
     */
    public void setMaxQueueSize(int size);

    /**
     * Returns this queue's maximum size.
     * 
     * @return the queue's maximum size; <code>-1</code> if the queue's size
     *         is unrestricted
     */
    public int getMaxQueueSize();

    /**
     * Returns the number of queue entries in this queue. This does not include
     * the queue entries that have status <em>Aborted</em> or
     * <em>Completed</em>.
     * 
     * @return the number of queue entries in this queue that do not have status
     *         <em>Aborted</em> or <em>Completed</em>
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, Table 5-102: Contents of the Queue
     *      element </a>
     */
    public int getQueueSize();

    /**
     * Returns the total number of queue entries in this queue.
     * 
     * @return the total number of queue entries in this queue
     */
    public int getTotalQueueSize();

    /**
     * Adds a queue entry to this queue.
     * 
     * @param params the <em>QueueSubmissionParams</em> element specifying the
     *            queue entry to put on this queue
     * @return the <em>QueueEntry</em> element representing the queue entry
     *         that was put on this queue; <code>null</code> if the queue was
     *         full
     */
    public JDFQueueEntry addQueueEntry(JDFQueueSubmissionParams params);

    /**
     * Returns the queue entry with the specified
     * <em>QueueEntry/@QueueEntryID</em>.
     * 
     * @param queueEntryId the ID of the queue entry to return
     * @return the queue entry; <code>null</code> if there was no queue entry
     *         with the specified ID
     */
    public JDFQueueEntry getQueueEntry(String queueEntryId);

    /**
     * Returns the submission parameters that were used when submitting the
     * specified queue entry.
     * 
     * @param queueEntryId the ID of the queue entry
     * @return the queue entry's submission parameters
     */
    public JDFQueueSubmissionParams getQueueSubmissionParams(String queueEntryId);

    /**
     * Removes a queue entry from this queue.
     * 
     * @param queueEntryId the ID of the queue entry to remove
     * @return the removed queue entry
     */
    public JDFQueueEntry removeQueueEntry(String queueEntryId);

    /**
     * Puts a queue entry on this queue. If there already exists a queue entry
     * with this specified ID it is replaced with the new queue entry.
     * <p>
     * <strong>Note: </strong> When adding new queue entries to the queue,
     * {@link #addQueueEntry(JDFQueueSubmissionParams) addQueueEntry}should be
     * used.
     * 
     * @param queueEntry the queue entry to put on the queue
     * @return the queue entry that was replaced by the new queue entry;
     *         <code>null</code> if no queue entry was replaced
     */
    public JDFQueueEntry putQueueEntry(JDFQueueEntry queueEntry);

    /**
     * Returns all a JDF representation of the queue and all its queue entries.
     * 
     * @return a <em>Queue</em> element representing this queue
     */
    public JDFQueue getQueue();

    /**
     * Returns this queue and a filtered subset of its queue entries.
     * 
     * @param filter the <em>JDFQueueFilter</em> to filter this queue with
     * @return the filtered queue
     */
    public JDFQueue getQueue(JDFQueueFilter filter);

    /**
     * Returns the status of this queue.
     * 
     * @return this queue's status
     * @see <a
     *      href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF
     *      Specification Release 1.2, Table 5-102: Contents of the Queue
     *      element </a>
     */
    public JDFQueue.EnumQueueStatus getQueueStatus();

    /**
     * Returns the first runnable queue entry in this queue.
     * 
     * @return the first runnable job in this queue; <code>null</code> if
     *         there is no runnable queue entry, for example if the queue is
     *         held
     */
    public JDFQueueEntry getFirstRunnableQueueEntry();

    /**
     * Registers a listener that should receive an event whenever this queue's
     * status changes.
     * 
     * @param listener
     */
    public void addQueueStatusListener(QueueStatusListener listener);

    /**
     * Unregisters a listener that previously received an event whenever this
     * queue's status changes.
     * 
     * @param listener
     */
    public void removeQueueStatusListener(QueueStatusListener listener);

    /**
     * Closes this queue. This method maps to the JMF command
     * <em>CloseQueue</em>.
     * 
     * @see #openQueue()
     */
    public void closeQueue();

    /**
     * Opens this queue. This method maps to the JMF command <em>OpenQueue</em>.
     * 
     * @see #closeQueue()
     */
    public void openQueue();

    /**
     * Holds this queue. This method maps to the JMF command <em>HoldQueue</em>.
     * 
     * @see #resumeQueue()
     */
    public void holdQueue();

    /**
     * Resumes this queue. This method maps to the JMF command
     * <em>ResumeQueue</em>.
     * 
     * @see #holdQueue()
     */
    public void resumeQueue();

    /**
     * Returns the <em>JobPhase</em> for the <em>QueueEntry</em> with
     * <code>queueEntryId</code> and includes the belonging <em>JDF</em> if
     * the <code>includeJDF</code> parameter is set to <code>true</code>.
     * 
     * @param queueEntryId The id of the <em>QueueEntry</em> which
     *            <em>JobPhase</em> is returned.
     * @param includeJDF <code>true</code> if the JobPhase should include the
     *            JDF Node, <code>false</code> otherwise.
     * @return The <em>JobPhase</em> element of the <em>QueueEntry</em>
     *         specified in the parameter. <code>null</code> if the
     *         queueEntryID does not exists in the Queue or if the QueueEntry is
     *         not running.
     * @throws NullPointerExcption if queueEntryID is <code>null</code>
     */
    public JDFJobPhase getJobPhase(String queueEntryId, boolean includeJDF);
}