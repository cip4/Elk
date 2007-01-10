/**
 * Created on Mar 22, 2006, 3:28:17 PM
 * org.cip4.elk.util.security.RFAJMFProcessor.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import org.cip4.jdflib.jmf.JDFJMF;

/**
 * This is the messaging interface to the AuthenticationHandler.
 * Implementing classes should hide the JMF syntax to the AuthenticationHandler.
 * This Processor processes all messages and handles all receiving and sending
 * of RFA messages.
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public interface RFAJMFProcessor {


	/**
	 * This method is the interface to the lower layer messaging classes.
	 * This is the only method a specific RFAJMFProcessor must implement in
	 * order to receive messages through the underlying messaging implementation
	 * 
	 * @param request request
	 * @param response response
	 * @param remoteHost remote host info 
	 * @return the response return code
	 */
	public int processJDF(JDFJMF request, JDFJMF response, RemoteHost remoteHost);
	
	
	/**
	 * Sends a query to the remote party associated
	 * with the TrustEntry.
	 * @param trustEntry
	 * @return true if the query is accepted
	 */
	public boolean sendQuery(TrustEntry trustEntry);
	

	/**
	 * Sends a InitiateConnection RFA Command to the remote party associated
	 * with the TrustEntry.
	 * @param trustEntry
	 * @return true if the the RFA is accepted or pending false otherwise
	 */
	public boolean sendInitiateConnectionCommand(TrustEntry trustEntry);
	
	
	/**
	 * Sends a CertificateExpired RFA Command to the remote party associated
	 * with the TrustEntry.
	 * 
	 * @param trustEntry
	 * @return true iff return code equals success
	 */
	public boolean sendCertificateExpiredCommand(TrustEntry trustEntry);
	
	
	/**
	 * Sends a HostnameMismatch RFA Command to the remote party associated
	 * with the TrustEntry.
	 * @param trustEntry
	 * @return true iff return code equals success
	 */
	public boolean sendHostnameMismatchCommand(TrustEntry trustEntry);
	
	
	/**
	 * Sends a CertificateRevoked RFA Command to the remote party associated
	 * with the TrustEntry.
	 * @param trustEntry
	 * @return true iff return code equals success
	 */
	public boolean sendCertificateRevokedCommand(TrustEntry trustEntry);
	
	
	/**
	 * Sends a Other RFA Command to the remote party associated
	 * with the TrustEntry.
	 * @param trustEntry
	 * @return true iff return code equals success
	 */
	public boolean sendOtherReasonCommand(TrustEntry trustEntry, String reasonDetails);
	
	
	
}
