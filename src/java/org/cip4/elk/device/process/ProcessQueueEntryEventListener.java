/*
 * Created on 2005-jun-06 
 */
package org.cip4.elk.device.process;

import java.util.EventListener;

/**
 * A listener that listens for <code>QueueEntryStatusEvents</code>.
 * 
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id: ProcessQueueEntryEventListener.java,v 1.1 2005/06/24 07:32:19 ola.stering Exp $
 */
public interface ProcessQueueEntryEventListener extends EventListener {
    /**
     * Invoked every time a QueueEntrty's status is changed.
     * 
     * @param event The event that has occurred.
     */
    public void queueEntryStatusChanged(ProcessQueueEntryEvent event);

}
