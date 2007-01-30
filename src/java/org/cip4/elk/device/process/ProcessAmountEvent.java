/*
 * Created on 2005-apr-27
 */
package org.cip4.elk.device.process;

import org.cip4.elk.ElkEvent;

import org.cip4.jdflib.resource.JDFNotification;

/**
 * An event that is generated each time a process' amount is changed.
 * 
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id: ProcessAmountEvent.java 545 2005-07-06 15:57:34Z ola.stering $
 */
public class ProcessAmountEvent extends ElkEvent {
    
    private int _amount;
    
    /**
	 * Creates a new process amount event.
	 * 
	 * @param amount
	 *            the process's amount
	 * @param source
	 *            the process that generated this event
	 */
	public ProcessAmountEvent(JDFNotification.EnumClass eventClass,
		int amount, Process source,
			String description) {
		super(eventClass, source, description);
		_amount = amount;
	}

	/**
	 * Returns the process that generated this event.
	 * 
	 * @return the process that generated this event
	 */
	public Process getProcess() {
		return (Process) getSource();
	}

	/**
	 * Returns the amount produced by the process.
	 * 
	 * @return the process's status
	 */
	public int getAmount() {
		return _amount;
	}

	public String toString() {
		return "ProcessAmountEvent[Amount: " + _amount + ";  Source: "
				+ getSource() + ";  Time stamp: " + getTimestamp() + "]";
	}
	
}
