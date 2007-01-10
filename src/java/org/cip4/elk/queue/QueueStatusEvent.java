package org.cip4.elk.queue;

import java.util.List;

import org.cip4.elk.ElkEvent;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * An event that is generated when a <code>Queue</code> goes through a status
 * change. This event may be triggered by a queue entry (
 * <code>JDFQueueEntry</code>) in the queue, but not necessarily.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class QueueStatusEvent extends ElkEvent {
    private JDFQueue.EnumQueueStatus _status;
    private List /* JDFQueueEntry */_queueEntries;

    /**
     * Creates a new event.
     * 
     * @param eventClass the class of this event
     * @param status the Queue's status
     * @param source the <code>Queue</code> that was the source of the event
     * @param queueEntries the <code>JDFQueueEntry</code> s that triggered the
     *            Queue to generate this event
     * @param description a description of this event
     */
    public QueueStatusEvent(JDFNotification.EnumClass eventClass,
            JDFQueue.EnumQueueStatus status, Queue source,
            List /* JDFQueueEntry */queueEntries, String description) {
        super(eventClass, source, description);
        _status = status;
        _queueEntries = queueEntries;
    }

    public QueueStatusEvent(JDFNotification.EnumClass eventClass,
            JDFQueue.EnumQueueStatus status, Queue source, String description) {
        this(eventClass, status, source, null, description);
    }

    /**
     * Returns the queue that generated this event.
     * 
     * @return the queue that generated this event
     */
    public Queue getQueue() {
        return (Queue) getSource();
    }

    /**
     * Returns the status of the queue.
     * 
     * @return the queue status
     */
    public JDFQueue.EnumQueueStatus getQueueStatus() {
        return _status;
    }

    /**
     * Returns a list of the <code>JDFQueueEntry</code> s that triggered the
     * Queue to generate this event.
     * 
     * @return a List of <code>JDFQueueEntry</code> objects; <code>null</code>
     *         if it was not a <code>JDFQueueEntry</code> that triggered this
     *         event
     */
    public List /* JDFQueueEntry */getQueueEntries() {
        return _queueEntries;
    }

    public String toString() {
        return "QueueStatusEvent[Status: " + _status.getName() + ";  Class: "
                + getEventClass() + ";  Source: " + getSource()
                + ";  Time stamp: " + getTimestamp() + "]";
    }

}
