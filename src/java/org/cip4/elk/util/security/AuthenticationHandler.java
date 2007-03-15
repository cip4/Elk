/**
 * Created on Mar 22, 2006, 3:27:35 PM
 * org.cip4.elk.util.security.AuthenticationHandler.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.util.Vector;


/**
 * TODO JAVADOC
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public interface AuthenticationHandler {

	/**
	 * Handles a incoming RequestForAuthentication Command with reason
	 * InitiateConnection and type AsServer.
	 * 
	 * @param senderID The requesting sender
	 * @param certificate the requesters certificate
	 * @param remoteUrl the requesters url
	 * @param hostInfo information of the remote host
	 * @return the corresponding trustentry
	 */
	public TrustEntry initIncomingServerConnection(String senderID,
			String certificate, String remoteUrl, RemoteHost hostInfo);

	/**
	 * Handles a incoming RequestForAuthentication Command with reason
	 * InitiateConnection and type AsClient.
	 * 
	 * @param senderID The requesting sender
	 * @param certificate the requesters certificate
	 * @param remoteUrl the requesters url
	 * @param hostInfo information of the remote host
	 * @return the corresponding trustentry
	 */
	public TrustEntry initIncomingClientConnection(String senderID,
			String certificate, String remoteUrl, RemoteHost hostInfo);


	/**
	 * Initiates a connection by sending a RFA command to the URL
	 * Returns null if the RFA request couldn�t be handled. 
	 * 
	 * @param remoteUrl the remote URL to initiate a cert exchange with
	 * @return 
	 */
	public TrustEntry initOutgoingServerConnection(String remoteUrl);

	/**
	 * Initiates a connection by sending a RFA command to the URL
	 * Returns null if the RFA request couldn�t be handled. 
	 * 
	 * @param remoteUrl the remote URL to initiate a cert exchange with
	 * @return 
	 */
	public TrustEntry initOutgoingClientConnection(String remoteUrl);

	
	/**
	 * Returns a TrustEntry with the localRole AsServer or null if
	 * it doesn�t exist. 
	 * 
	 * Returns a TrustEntry to a remote CLIENT wich is
	 * the same as a local SERVER
	 * 
	 * @param senderID  the ID of the TrustEntry
	 * @return the TrustEntry or null
	 */
	public TrustEntry getServerTrustEntry(String senderID);


	/**
	 * Returns a TrustEntry with the role AsClient or null if
	 * it doesn�t exist.
	 * 
	 * Returns a TrustEntry to a remote SERVER wich is
	 * the same as a local CLIENT
	 * 
	 * @param senderID  the ID of the TrustEntry
	 * @return the TrustEntry or null
	 */
	public TrustEntry getClientTrustEntry(String senderID);


	
	/**
	 * Adds a TrustEntry with the initial data ID and URL.
	 * This is needed by passive parties in order to know the
	 * query URL when a init RFA command is received
	 * 
	 * @param remoteID
	 * @param remoteURL
	 */
	public void addAllowedTrustRelation(String remoteID, String remoteURL);
	
	
	/**
	 * Deletes the trustEntry
	 * @param trustEntry
	 */
	public void deleteTrustEntry(TrustEntry trustEntry);
	
	/**
	 * Adds a currently pending server trust relation
	 * to the trusted entries.
	 * 
	 * @param remoteServerID The remote server ID
	 * @return true if successful
	 */
	public boolean addServerTrust(String remoteServerID);
	
	
	/**
	 * Adds a currently pending client trust relation to
	 * the trusted entries
	 * 
	 * @param remoteClientID The remote client to grant trust
	 * @return true if successful
	 */
	public boolean addClientTrust(String remoteClientID);
	
	
	
	/**
	 * Deletes all trust information of a remote server
	 * 
	 * @param remoteServerID The id of the remote server
	 * @return True if deletion is successful
	 */
	public boolean deleteServerTrust(String remoteServerID);
	
	
	/**
	 * Deletes all trust information of a remote client
	 * 
	 * @param remoteClientID The id of the remote client
	 * @return True if deletion is successful
	 */
	public boolean deleteClientTrust(String remoteClientID);

	
	/**
	 * Returns the RFAJMFProcessor that processes all incoming and
	 * outgoing RFA messages.
	 * 
	 * The RFAJMFProcessor should be the abstraction layer between
	 * JMF messages and Trust relations.
	 * 
	 * @return an implementation of RFAJMFProcessor
	 */
	public RFAJMFProcessor getRFAJMFProcessor();
	
	
	/**
	 * Returns the KeyManager holding all the local certificates and
	 * trusted certificates.
	 * @return
	 */
	public KeyManager getKeyManager();
	
	
	/**
	 * A generic method to notify the AuthenticationHandler or the
	 * User using the workflow entity.
	 * 
	 * @param notification A notification in some form.
	 */
	public void notifyUser(Object notification);
	
	
	/**
	 * Returns all the TrustEntries in a Vector.
	 * 
	 * @return Vector<TrustEntry>
	 */
	public Vector getTrustEntries(); 

	
	/**
	 * Checks if a client trust relation is established to the server
	 * with the secure URL specified.
	 * 
	 * @param secureRemoteUrl the remote https url.
	 * @return true if a trust relation is established.
	 */
	public boolean isClientTrustEstablished(String secureRemoteUrl); // Alces needs to know what to set as returnUrl when preprocessing
	
	
	/**
	 * gets the secureUrl of a connected device with the unsecure Url
	 * as the method argument
	 * @param unsecureRemoteUrl non https url
	 * @return the secure url if there is a trustrelation. null otherwise
	 */
	public String getTrustedServerSecureUrl(String unsecureRemoteUrl);
	
	/**
	 * Returns the secure url to this server.
	 * 
	 * This is sent to Clients requesting authentication and
	 * used to get the full URL of the listening server using this
	 * AuthenticationHandler
	 * 
	 * @return A URL to the JMF/JDF listening server/servlet
	 */
	public String getSecureUrl();
	
	
	/**
	 * This method allows the AuthenticationHandler to do any
	 * necessary post-processing.
	 */
	public void shutDown();
	
	
	/**
	 * Called at startup to initiate the AuthenticationHandler.
	 */
	public void init();
	
}
