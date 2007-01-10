package org.cip4.elk.queue;

import java.util.EventListener;

/**
 * A listener that listens for queue status changes.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface QueueStatusListener extends EventListener {

    /**
     * Invoked each time any of the queues that are listened to change status.
     * @param queueStatusEvent
     */
    public void queueStatusChanged(QueueStatusEvent queueStatusEvent);
    
}
