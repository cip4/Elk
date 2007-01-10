/*
 * Created on Jan 30, 2006
 */
package org.cip4.elk.impl.queue.jmf;

import org.cip4.elk.impl.jmf.AbstractJMFProcessor;
import org.cip4.elk.queue.Queue;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * A processor that handles AbortQueueEntry commands. Aborting Runnig or Suspended 
 * queue entries returns a ReturnCode 5 (Query/Command not implemented)
 * since the appropriate functionality is not yet implemented.
 * 
 * @author Markus Nyman (markus@myman.se)
 * @version $Id$
 */
public class AbortQueueEntryJMFProcessor extends AbstractJMFProcessor {

	// private Config _config;
	private Queue _queue;
    private static final String MESSAGE_TYPE = "AbortQueueEntry";

	public AbortQueueEntryJMFProcessor(Queue queue) {
		super();
        // _config = config; TODO Not used, should it be here?
		_queue = queue;
		setMessageType(MESSAGE_TYPE);
		setCommandProcessor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.elk.jmf.JMFProcessor#processJMF(com.heidelberg.JDFLib.jmf.JDFMessage,
	 *      com.heidelberg.JDFLib.jmf.JDFResponse)
	 */
	public int processMessage(JDFMessage input, JDFResponse output) {
		return processAbortQueueEntry((JDFCommand) input, output);
	}

	/**
	 * Processes AbortQueueEntry commands. Queue entries will not be
	 * aborted if entry has status <em>Completed</em>, <em>Removed</em> 
	 * or <em>Aborted</em>. returnCodes are defined in JDF1.3 Appendix D.
	 * 
	 * @param command
	 * @param response
	 * @return returnCode
	 */
	private synchronized int processAbortQueueEntry(JDFCommand command,
			JDFResponse response) {
		int returnCode = 0;
		String qeId = command.getQueueEntryDef(0).getQueueEntryID();
		JDFQueueEntry qe = _queue.getQueueEntry(qeId);
		if (qe == null) {
			// Queue entry does not exist
			returnCode = 105;
			String msg = "The queue entry '" + qeId
					+ "' could not be aborted because it does not exist.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
		} else if (qe.getQueueEntryStatus().equals(
				JDFQueueEntry.EnumQueueEntryStatus.Aborted)) {
			// Queue entry is aborted
			returnCode = 113;
			String msg = "The queue entry '" + qeId
					+ "' is already aborted.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
		} else if (qe.getQueueEntryStatus().equals(
				JDFQueueEntry.EnumQueueEntryStatus.Completed)) {
			// Queue entry is completed
			returnCode = 114;
			String msg = "The queue entry '" + qeId
					+ "' could not be aborted because it is completed.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
			/*
		} else if (qe.getQueueEntryStatus().equals(
				JDFQueueEntry.EnumQueueEntryStatus.Removed)) {
			// NOT SPECIFIED in JDF specification
			// Queue entry is removed
			returnCode = 105;
			String msg = "The queue entry '" + qeId
					+ "' could not be aborted because it is removed.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
			*/
		} else if (qe.getQueueEntryStatus().equals(
				JDFQueueEntry.EnumQueueEntryStatus.Running)) {
			// Queue entry is running. Abort the running process
			// TODO implement the appropriate functionality
			returnCode = 5;
			String msg = "The queue entry '" + qeId
					+ "' is Running and could not be aborted. "
					+ "Functionality not yet implemented.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
		} else if (qe.getQueueEntryStatus().equals(
				JDFQueueEntry.EnumQueueEntryStatus.Suspended)) {
			// Queue entry is suspended. Abort the suspended process
			// TODO implement the appropriate functionality
			returnCode = 5;
			String msg = "The queue entry '" + qeId
					+ "' is Suspended and could not be aborted. "
					+ "Functionality not yet implemented.";
			appendNotification(response, JDFNotification.EnumClass.Error,
					returnCode, msg);
			log.warn(msg);
		} else {
			// Queue entry can be aborted. Status Waiting or Held
			// See JDF1.3 Appendix D for supported error codes
			returnCode = 0;
			// set status aborted to queue element
			qe.setQueueEntryStatus(JDFQueueEntry.EnumQueueEntryStatus.Aborted);
			// put the updated queue element in queue
			_queue.putQueueEntry(qe);		
			log.info("Aborted the queue entry '" + qeId + "'.");
		}

		// Returns a filtered queue
		JDFQueue q = _queue.getQueue(command.getQueueFilter(0));
		response.copyElement(q, null);
		response.setReturnCode(returnCode);
		return returnCode;
	}

}
