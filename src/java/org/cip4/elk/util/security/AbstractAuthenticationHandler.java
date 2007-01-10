/**
 * Created on Mar 22, 2006, 3:30:37 PM
 * org.cip4.elk.util.security.AbstractAuthenticationHandler.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An abstract class that provides basic methods for an 
 * <code>AuthenticationHandler</code>.
 * This class implements basic storage functionality
 * where <code>DefaultTrustEntry</code> objects are stored.
 * 
 * Extending classes can overide the 
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 * @see DefaultTrustEntry
 * @see TrustEntry
 * @see AuthenticationHandler
 */
public abstract class AbstractAuthenticationHandler implements
		AuthenticationHandler {
	
	private Log log = LogFactory.getLog(this.getClass().getName());

	private Vector _trustEntries = null;
	
	/**
	 * Only constructor. Instanciates the default Maps used for
	 * storing TrusEntries
	 */
	public AbstractAuthenticationHandler() {
		_trustEntries = new Vector();
	}

	/**
	 * Abstract method for getting the ID of the local device or controller
	 * that implements RFA commands. This is used to set the senderID field
	 * in JMF messages. In some implementations this is done in underlying
	 * classes.
	 */
	public abstract String getLocalID();


	/**
	 * A getter for the <code>KeyManager</code> used in this 
	 * <code>AuthenticationHandler</code>
	 * @return The KeyManager used in this AuthenticationHandler
	 */
	public abstract KeyManager getKeyManager();


	/**
	 * A getter for the <code>RFAJMFProcessor used in this
	 * <code>AuthenticationHandler</code>
	 * @return The RFAJMFProcessor used in this AuthenticationHandler
	 */
	public abstract RFAJMFProcessor getRFAProcessor();

	/**
	 * TODO
	 * This method starts a process of authenticating a remote CLIENT.
	 * When this method is called all the fields of the TrustEntry must
	 * be initiated. This implies that the initial handshake and certificate
	 * exchange has been made.
	 * @param trustEntry
	 */
	public abstract void startClientRFAProcess(TrustEntry trustEntry);

	/**
	 * TODO
	 * This method starts a process of authenticating a remote SERVER.
	 * When this method is called all the fields of the TrustEntry must
	 * be initiated. This implies that the initial handshake and certificate
	 * exchange has been made.
	 * @param trustEntry
	 */
	public abstract void startServerRFAProcess(TrustEntry trustEntry);

	////////////////////////////////////////////////////////
	// Implemented AuthenticationHandler methods          //
	////////////////////////////////////////////////////////
	
	/**
	 * Initiates a incoming Request for authentication request.
	 * Creates a trust entry and starts the querying process
	 */
	public TrustEntry initIncomingServerConnection(String remoteID,
			String certificate, String remoteUrl, RemoteHost hostInfo) {
		// TODO Auto-generated method stub
		TrustEntry trustEntry = createDefaultTrustEntry(remoteUrl, false);
		trustEntry.setRemoteID(remoteID);
		trustEntry.setRemoteCertificate(certificate);
		trustEntry.setRemoteHostInfo(hostInfo);

		// TODO is this optimal?
		startServerRFAProcess(trustEntry);
		return trustEntry;
	}

	/**
	 * Initiates a incoming Request for authentication request.
	 * Creates a trust entry and starts the querying process
	 */
	public TrustEntry initIncomingClientConnection(String remoteID,
			String certificate, String remoteUrl, RemoteHost hostInfo) {
		// TODO Auto-generated method stub
		TrustEntry trustEntry = createDefaultTrustEntry(remoteUrl, true);
		trustEntry.setRemoteID(remoteID);
		trustEntry.setRemoteCertificate(certificate);
		trustEntry.setRemoteHostInfo(hostInfo);
		// TODO is this optimal?
		startClientRFAProcess(trustEntry);
		return trustEntry;
	}

	/**
	 * This party initiates a RFA command by sending a RFA command with
	 * the role AsServer.
	 * 
	 * Creates a TrustEntry and prepares and sends the init commands.
	 */
	public TrustEntry initOutgoingServerConnection(String remoteUrl) {
		
		TrustEntry trustEntry = getClientTrustEntryURL(remoteUrl);
		if (trustEntry == null)
			trustEntry = createDefaultTrustEntry(remoteUrl, true);
		if (!getRFAProcessor().sendInitiateConnectionCommand(trustEntry)) {
			return null;
		} else {
			startClientRFAProcess(trustEntry);
			return trustEntry;
		}
	}

	/**
	 * This party initiates a RFA command by sending a RFA command with
	 * the role AsClient.
	 * 
	 * Creates a TrustEntry and prepares and sends the init commands.
	 */
	public TrustEntry initOutgoingClientConnection(String remoteUrl) {
		TrustEntry trustEntry = getServerTrustEntryURL(remoteUrl);
		if (trustEntry == null)
			trustEntry = createDefaultTrustEntry(remoteUrl, false);
		if (!getRFAProcessor().sendInitiateConnectionCommand(trustEntry)) {
			return null;
		} else {
			startServerRFAProcess(trustEntry);
			return trustEntry;
		}
	}
	
	
	/**
	 * Creates the init TrustEntry so that an incoming RFA commands
	 * can be accepted. This is needed since the URL wich is queried
	 * for the RFA status must be known prior to an incoming RFA command.
	 * The non-initiating party needs to know where to send the queries.
	 * Only used for passive parties, that is parties who doesnt send init
	 * commands but still handles incoming requests.
	 */
	public void addAllowedTrustRelation(String remoteID, String remoteURL) {
		if (getTrustEntry(remoteID, false) == null) {
			TrustEntry serverTE = createDefaultTrustEntry(remoteURL, false);
			serverTE.setRemoteID(remoteID);
			addServerTrustEntry(serverTE);
		}
		if (getTrustEntry(remoteID, true) == null) {
			TrustEntry clientTE = createDefaultTrustEntry(remoteURL, true);
			clientTE.setRemoteID(remoteID);
			addClientTrustEntry(clientTE);
		}
	}
	

	
	/**
	 * returns the TrustEntries in a Vector
	 */
	public Vector getTrustEntries() {		
		return _trustEntries;
	}
	

	/**
	 * Serializes the current trust relations to the file
	 * given. The Objects are serialized with the
	 * ObjectOutputStream.
	 * 
	 * TODO load and store in XML format
	 * 
	 * @param fname the filename to load objects from
	 */
	public void serializeTrustEntries(String fname) {
		try {
			FileOutputStream out = new FileOutputStream(fname);
			ObjectOutputStream s = new ObjectOutputStream(out);
			s.writeObject(_trustEntries);
			s.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Loads the stored trust relations from the file
	 * given. The Objects are de-serialized with the
	 * ObjectInputStream.
	 * 
	 * TODO load and store in XML format
	 * 
	 * @param fname the filename to load objects from
	 */
	public void loadTrustEntries(String fname) {
	
		try {
			FileInputStream in = new FileInputStream(fname);
			ObjectInputStream s = new ObjectInputStream(in);

			_trustEntries = (Vector)s.readObject();
			Iterator i = _trustEntries.iterator();
			while (i.hasNext()) {
				TrustEntry trustEntry = (TrustEntry)i.next();
				trustEntry.init();
				((DefaultTrustEntry)trustEntry).setAuthenticationHandler(this);
				
			}
		} catch (FileNotFoundException e) {
			log.debug("No trust entries file found. Initializing new Vector.");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	
	}
	
	
	/**
	 * TODO OPTIMIZE ASAP XXX
	 * 
	 * Implements the check for a trusted remote part.
	 * Currently this only checks wether a trust relation
	 * is present with a remote url.
	 * 
	 * This method compares the getRemoteSecureUrl() and remoteUrl() 
	 * with the input parameter from all trusted remote servers
	 * and returns true if a match is found and false otherwise.
	 */
	public boolean isClientTrustEstablished(String remoteUrl) {
		TrustEntry tmp = null;
		Iterator it = _trustEntries.iterator();
		while (it.hasNext()) {
			tmp = (TrustEntry) it.next();
			// if there is a trusted client with the connected url
			if (tmp.isLocalRoleServer() == true &&
					(tmp.getRemoteSecureUrl().equals(remoteUrl) ||
							tmp.getRemoteUrl().equals(remoteUrl))) 
				return (tmp.getRemoteStatus() == TrustEntry.CERT_ACCEPTED &&
						tmp.getLocalStatus() == TrustEntry.CERT_ACCEPTED);
			// if not we have to get the servers ID and look up the client trust status
			// NASTY SHIT
			if (tmp.isLocalRoleServer() == false &&
					(tmp.getRemoteSecureUrl().equals(remoteUrl) ||
							tmp.getRemoteUrl().equals(remoteUrl))) {
				TrustEntry tmp2 = getClientTrustEntry(tmp.getRemoteID());
				return (tmp2 != null && 
						tmp2.getRemoteStatus() == TrustEntry.CERT_ACCEPTED &&
						tmp2.getLocalStatus() == TrustEntry.CERT_ACCEPTED);
			}
								
		}
		
		return false;
	}
	
	
	/**
	 * Checks if a server with the specified url (secure or unsecure)
	 * is trusted and returns the Secure URL. null otherwise
	 */
	public String getTrustedServerSecureUrl(String unsecureRemoteUrl) {
		for (Iterator i = _trustEntries.iterator();i.hasNext();) {
			TrustEntry tmp = (TrustEntry) i.next();
			if (!tmp.isLocalRoleServer() &&
					(tmp.getRemoteUrl().equals(unsecureRemoteUrl) ||
					 tmp.getRemoteSecureUrl().equals(unsecureRemoteUrl))) {
				if (tmp.getRemoteStatus() == TrustEntry.CERT_ACCEPTED &&
						tmp.getLocalStatus() == TrustEntry.CERT_ACCEPTED)
					return tmp.getRemoteSecureUrl();
			}
		}
		return null;
	}
	
	
	
	
	/**
	 * Returns a TrustEntry from the local Collection of
	 * TrustEntries.
	 * @param senderID The senderID of the searched TrustEntry.
	 * @param serverRole The local role of the trustEntry.
	 * True for a local server, false for a local client.
	 * @return The TrustEntry
	 */
	private TrustEntry getTrustEntry(String senderID, boolean serverRole) {
		TrustEntry tmp = null;
		Iterator it = _trustEntries.iterator();
		while (it.hasNext()) {
			tmp = (TrustEntry) it.next();
			if (tmp.isLocalRoleServer() == serverRole && tmp.getRemoteID().equals(senderID))
				return tmp;
		}
		return null;
	}
	
	
	/**
	 * Removes a TrustEntry from the local Collection of
	 * TrustEntries. The removed instance is returned
	 * @param senderID The senderID to remove
	 * @param serverRole The local role of the trustEntry.
	 * True for a local server, false for a local client.
	 * @return The TrustEntry
	 */
	private TrustEntry removeTrustEntry(String senderID, boolean serverRole) {
		TrustEntry tmp = getTrustEntry(senderID, serverRole);
		_trustEntries.remove(tmp);
		return tmp;
		
	}
	
	
	/**
	 * Deletes the given TrustEntry from the local
	 * Collection of TrustEntries.
	 */
	public void deleteTrustEntry(TrustEntry trustEntry) {
		_trustEntries.remove(trustEntry);
	}

	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries.
	 * No error handling is performed. This should be done by
	 * implementing classes
	 * @throws NullPointerException if any of the parameters are null
	 * @see AuthenticationHandler
	 */
	public void addClientTrustEntry(TrustEntry trustEntry) {
		_trustEntries.add(trustEntry);
	}
	
	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries
	 * No error handling is performed. This should be done by
	 * implementing classes.
	 * @throws NullPointerException if any of the parameters are null
	 * @see AuthenticationHandler
	 */
	public void addServerTrustEntry(TrustEntry trustEntry) {
		_trustEntries.add(trustEntry);
	}

	
	/**
	 * Returns the client TrustEntry that listens to requests on 
	 * the given URL. The TrustEntry represents a remote client.
	 *  
	 * @param url The remote servers URL.
	 * @return the TrustEntry or null if no such TrustEntry exist.
	 */
	public TrustEntry getClientTrustEntryURL(String url) {
		TrustEntry tmp = null;
		Iterator it = _trustEntries.iterator();
		while (it.hasNext()) {
			tmp = (TrustEntry) it.next();
			// Check that it´s not a server TE and that URL is correct
			if (tmp.isLocalRoleServer() == true && tmp.getRemoteUrl().equals(url))
				return tmp;
		}
		return null;

	}
	
	
	/**
	 * Returns the server TrustEntry that listens to requests on 
	 * the given URL. TrustEntry represents a remote server.
	 *  
	 * @param url The remote servers URL.
	 * @return the TrustEntry or null if no such TrustEntry exist.
	 */
	public TrustEntry getServerTrustEntryURL(String url) {
		TrustEntry tmp = null;
		Iterator it = _trustEntries.iterator();
		while (it.hasNext()) {
			tmp = (TrustEntry) it.next();
			// Check if TrustEntry is server and has given URL
			if (tmp.isLocalRoleServer() == false && tmp.getRemoteUrl().equals(url))
				return tmp;
		}
		return null;
	}
	
	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries
	 * No error handling is performed. This should be done by
	 * implementing classes. Returns the TrustEntry or null.
	 * @throws NullPointerException if senderID is null
	 * @see AuthenticationHandler
	 */
	public TrustEntry getClientTrustEntry(String senderID) {
		return getTrustEntry(senderID, true);
	}
	
	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries
	 * No error handling is performed. This should be done by
	 * implementing classes
	 * @see AuthenticationHandler
	 */
	public TrustEntry getServerTrustEntry(String senderID) {
		return getTrustEntry(senderID, false);
	}
	
	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries
	 * No error handling is performed. This should be done by
	 * implementing and overriding classes
	 * @see AuthenticationHandler
	 */
	public TrustEntry deleteClientTrustEntry(String senderID) {
		return removeTrustEntry(senderID, true);
	}
	
	
	/**
	 * Implements the method in interface Authenticationhandler
	 * and uses the default Storage class for TrustEntries
	 * No error handling is performed. This should be done by
	 * implementing and overriding classes
	 * @see AuthenticationHandler
	 */
	public TrustEntry deleteServerTrustEntry(String senderID) {
		return removeTrustEntry(senderID, false);
	}

	
	/**
	 * Creates a template <code>DefaultTrustEntry</code> for use in other methods
	 * Parameters are the remote URL this TrustEntry represents and if the local partys
	 * role is server or client. 
	 * @see TrustEntry
	 * @see DefaultTrustEntry
	 * @param remoteUrl The remote URL.
	 * @param localRoleIsServer The role of this (the local) party
	 * @return An instance of <code>DefaultTrustEntry</code>
	 */
	protected DefaultTrustEntry createDefaultTrustEntry(String remoteUrl, boolean localRoleIsServer){
		DefaultTrustEntry trustEntry = new DefaultTrustEntry(localRoleIsServer);
		// set relevant fields in the TrustEntry
		trustEntry.setAuthenticationHandler(this);
		// get the entity name (elk, alces) and set it in TrustEntry
		trustEntry.setLocalID(getLocalID());
		// set the NON-SECURE URL of the remote party
		trustEntry.setRemoteUrl(remoteUrl);
		// Is this TrustEntry referencing the local Server or Client certificate
		if (localRoleIsServer)
			trustEntry.setLocalCertificate(getKeyManager().getServerCertificate());
		else
			trustEntry.setLocalCertificate(getKeyManager().getClientCertificate());
		
		return trustEntry;
	}
	
}
