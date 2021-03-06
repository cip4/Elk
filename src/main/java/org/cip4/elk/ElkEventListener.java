package org.cip4.elk;

import java.util.EventListener;

/**
 * A listener that listens for status changes in device components.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface ElkEventListener extends EventListener {
	/**
	 * Is called each time an event is generated by the object listened upon.
	 * @param event    the generated event
	 */
	public void eventGenerated(ElkEvent event);
}
