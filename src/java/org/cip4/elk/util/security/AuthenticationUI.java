/**
 * Created on Mar 22, 2006, 3:37:14 PM
 * org.cip4.elk.util.security.AuthenticationUI.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

/**
 * This is an interface for a standard UI to notify the user of
 * changes in TrustEntries and other messages to the user
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public interface AuthenticationUI {

	/**
	 * Notifies the UI when a new TrustEntry is created.
	 * @param trustEntry
	 */
	public void notifyTrustEntry(TrustEntry trustEntry);
	
	/**
	 * Notifies the UI with a String message
	 * @param message
	 */
	public void notifyMessage(String message);
	

}
