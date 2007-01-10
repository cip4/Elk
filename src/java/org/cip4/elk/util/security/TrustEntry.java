/**
 * Created on Mar 22, 2006, 3:42:45 PM
 * org.cip4.elk.util.security.TrustEntry.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;


/**
 * TODO JAVADOC
 * represents a relation to a remote entry.
 * The relation is representing a Client-Server relation or
 * a Server-Client relation so each remote party consists of
 * two TrustEntries.
 * 
 * Two TrustEntries representing a trustRelation has a difference
 * in the attribute localRole wich can be checked with
 * boolean isLocalRoleServer()
 * 
 * This implentation is something im not very proud of.
 * 
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public interface TrustEntry {

	/**
	 * Status enumerations for a trust entry
	 *
	 * TODO change to safe Enumeration class
	 */
	public static final int UNKNOWN = 0;
	public static final int PENDING = 1;
	public static final int CERT_ACCEPTED = 2;
	public static final int CERT_INVALID = 3;
	public static final int CERT_EXPIRED = 4;
	public static final int CERT_REVOKED = 5;
	public static final int CERT_HOSTNAME_MISMATCH = 6;
	public static final int ERROR = 7;
	public static final int INITIALIZED = 8;
	
	
	
	/**
	 * Sets the remote status of this TrustEntry
	 * @param status status
	 */
	public void setRemoteStatus(int status);
	
	/**
	 * Sets the local status of this TrustEntry
	 * @param status status
	 */
	public void setLocalStatus(int status);
	
	/**
	 * Getter for the remote status
	 * @return status
	 */
	public int getRemoteStatus();
	
	/**
	 * getter for the local status
	 * @return status
	 */
	public int getLocalStatus();
	
	
	
	//public String getLocalInfo();
	
	/**
	 * returns a short info string
	 */
	public String getRemoteInfo();
	
	// set the remote info of the host
	// ONLY when initiating connection. after that resolve the info
	// to the sending host
	int i = 0;
	
	/**
	 * Sets the RemoteHost object for this TrustEntry
	 * @param hostInfo host info
	 */
	public void setRemoteHostInfo(RemoteHost hostInfo);
	
	/**
	 * getter for the RemoteHost
	 * @return remote host info
	 */
	public RemoteHost getRemoteHostInfo();
	
	/**
	 * checks if this TrustEntry has a server role
	 * @return true if local role is server
	 */
	// DefaultKeyManager, AbstractRFAJMFProcessor
	public boolean isLocalRoleServer();
	

	/**
	 * Returns the remote id of this TrustEntry
	 * @return
	 */
	// DefaultKeyManager, AbstractAuthenticationHandler
	public String getRemoteID();
	
	/**
	 * Sets the remote id of this trust entry
	 * @param remoteID
	 */
	// AbstractRFAJMFProcessor
	public void setRemoteID(String remoteID);
	
	/**
	 * Getter for the remote certificate
	 * @return PEM string
	 */
	// DefaultKeyManager
	public String getRemoteCertificate();
	
	/**
	 * Setter for the certificate field
	 * @param certificate PEM cert
	 */
	// AbstractRFAJMFProcessor
	public void setRemoteCertificate(String certificate);
	
	/**
	 * Setter for the secure url of this TrustEntry
	 * @param remoteSecUrl
	 */
	// AbstractRFAJMFProcessor
	public void setRemoteSecureUrl(String remoteSecUrl);

	/**
	 * Getter for the local secure url
	 * @return local secure url or null if this is a client TrustEntry
	 */
	// AbstractRFAJMFProcessor
	public String getLocalSecureUrl();
	
	/**
	 * Getter for the remote url for this TrustEntry
	 * @return
	 */
	// AbstractRFAJMFProcessor
	public String getRemoteUrl();
	
	/**
	 * Getter for the remote secure url
	 * @return secure url
	 */
	// testrunner needs this to know if server is trusted.
	// check authHandler.isTrustEstablished()
	public String getRemoteSecureUrl();
	
	/**
	 * Getter for the local certificate
	 * @return
	 */
	// AbstractRFAJMFProcessor
	public String getLocalCertificate();
	
	/**
	 * Getter for the local ID
	 * @return
	 */
	// AbstractRFAJMFProcessor
	public String getLocalID();

	///////////////////////////////
	// methods for the webapp jsp:s
	//////////////////////////////
	
	public String getRole();
	public String getRemoteRole();
	public String getCertInfo();
	public String getRemoteStatusString();
	public String getLocalStatusString();
	
	/**
	 * init method to init after a deserialization
	 */
	// needed for deserialization AbstrAuthHandler
	public void init();
	

}
