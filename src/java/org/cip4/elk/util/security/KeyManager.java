/**
 * Created on Mar 22, 2006, 3:27:48 PM
 * org.cip4.elk.util.security.KeyManager.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.security.cert.X509Certificate;

/**
 * The KeyManager handles all KeyStores and Certificates.
 * 
 * A KeyManager has references to the KeyStores holding the
 * client and server PrivateKeyEntries as well as the trusted
 * entries certificates
 *
 * @author Markus Nyman, (markus.cip4@myman.se)
 * 
 */
public interface KeyManager {

	
	/** 
	 * Returns a String representation of the client certificate for
	 * the local workflow entity. This is the method to call to get
	 * the client Certificate to send to a RFA-server entity.
	 * @return Certificate in some String representation
	 */
	public String getClientCertificate();
	
	
	/** 
	 * Returns a String representation of the server certificate for
	 * the local workflow entity. This is the method to call to get
	 * the server Certificate to send to a RFA querying client.
	 * @return Certificate in some String representation
	 */
	public String getServerCertificate();

	
	/**
	 * Adds the certificate of the TrustEntry to the trustStore.
	 * Implementing classes must distinguish servers from clients
	 * since an entity´s client and server certificate usually
	 * has the same alias.
	 * 
	 * The remoteCertificate for the trustEntry is stored in 
	 * the trustStore.
	 * 
	 * @param trustEntry the TrustEntry to add trust for.
	 * @return True if addition is successful
	 */
	public boolean addTrust(TrustEntry trustEntry); // DefaultTrustEntry

	
	/**
	 * Deletes the certificate for the given trustEntry from 
	 * the trustStore.
	 * @see addTrust(TrustEntry)
	 * @param trustEntry TrustEntry to delete trust for.
	 * @return true if deletion is successful
	 */
	public boolean deleteTrust(TrustEntry trustEntry);
 
	
	/**
	 * Converts a PEM-encoded Certificate to a X509Certificate.
	 * This method is only here to allow other objects to access
	 * X509 fields of a certificate. 
	 * 
	 * Since the certificates outside the scope of the KeyManager 
	 * handles all certificates in a PEM String format this method
	 * is added to be able to extract neccessary X509 fields for
	 * host name verification and -dname information
	 * 
	 * @param PEMCertificate PEM encoded Certificate String
	 * @return Proper certificate object
	 */
	public X509Certificate generateX509Certificate(String PEMCertificate);
	
}
