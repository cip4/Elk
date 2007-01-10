package org.cip4.elk.device.process;

import java.util.EventListener;

/**
 * A listener that listens for process status changes.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface ProcessStatusListener extends EventListener {

    /**
     * Invoked each time any of the processes that are listened to change 
     * status.
     * @param processStatusEvent
     */
    public void processStatusChanged(ProcessStatusEvent processStatusEvent);    
}
