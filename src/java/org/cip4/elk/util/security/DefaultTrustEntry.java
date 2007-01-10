/**
 * Created on Mar 22, 2006, 3:43:35 PM
 * org.cip4.elk.util.security.DefaultTrustEntry.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO JAVADOC
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public class DefaultTrustEntry implements TrustEntry, Runnable, Serializable {

	private transient Log log = LogFactory.getLog(this.getClass().getName());
	
    private static final long serialVersionUID = -15876023230782128L;

	

    /**
     * The authenticationhandler that handles this TrustEntry
     */
	private transient AuthenticationHandler _authHandler;
	
	/**
	 * boolean for the local role of this TrustEntry
	 */
	private boolean _localRoleIsServer;

	private String _localID = "NULL";
	private String _remoteID = "NULL";
	private String _remoteUrl = "NULL";
	private String _remoteCertificate = "NULL";
	private String _localCertificate = "NULL";
	
	private RemoteHost _hostInfo = null;
	
	private String _localSecureUrl = null;
	private String _remoteSecureUrl = "NULL";
	
	
	private int remoteStatus = -1;
	private int localStatus = -1;
	
	private long queryInterval = 13000; // querying remote status every 13 sec
	private int nrOfQueries = 20;
	private int queriesMade = 0; 
	
	//private Thread process = null;
	
	public DefaultTrustEntry(boolean localRoleIsServer) {
		remoteStatus = UNKNOWN;
		localStatus = UNKNOWN;
		_localRoleIsServer = localRoleIsServer;
	}
	
	public void init() {
		log = LogFactory.getLog(this.getClass().getName());
	}
	
	public void setAuthenticationHandler(AuthenticationHandler authHandler) {
		_authHandler = authHandler;
	}
	
	public synchronized void setRemoteStatus(int status) {
		_authHandler.notifyUser("Remote status changed from " + 
				getStatusString(remoteStatus) + " to " + getStatusString(status));
		remoteStatus = status;
		switch (status) {
		case TrustEntry.UNKNOWN: break;
		case TrustEntry.PENDING: break;
		case TrustEntry.CERT_ACCEPTED: break;
		case TrustEntry.CERT_EXPIRED: 
			deleteTrust();
			break;
		case TrustEntry.CERT_HOSTNAME_MISMATCH:
			deleteTrust();
			break;
		case TrustEntry.CERT_INVALID:
			deleteTrust();
			break;
		case TrustEntry.CERT_REVOKED:
			deleteTrust();
			break;
		case TrustEntry.ERROR: 
			break;
		}
		checkStatus();
	}
	
	public synchronized void setLocalStatus(int status) {
		_authHandler.notifyUser("Remote status changed from " + 
				getStatusString(remoteStatus) + " to " + getStatusString(status));
		localStatus = status;
		switch (status) {
		case TrustEntry.UNKNOWN: break;
		case TrustEntry.PENDING: break;
		case TrustEntry.CERT_ACCEPTED: break;
		case TrustEntry.CERT_EXPIRED: 
			_authHandler.getRFAJMFProcessor().sendCertificateExpiredCommand(this);
			deleteTrust();
			break;
		case TrustEntry.CERT_HOSTNAME_MISMATCH:
			_authHandler.getRFAJMFProcessor().sendHostnameMismatchCommand(this);
			deleteTrust();
			break;
		case TrustEntry.CERT_INVALID:
			_authHandler.getRFAJMFProcessor().sendCertificateRevokedCommand(this);
			deleteTrust();
			break;
		case TrustEntry.CERT_REVOKED:
			_authHandler.getRFAJMFProcessor().sendCertificateRevokedCommand(this);
			deleteTrust();
			break;
		case TrustEntry.ERROR: break;
		default:
			log.debug("Unhandled status code in DefaultTrustEntry.");
		}
		checkStatus();
		
	}
	
	
	// Called after a status change
	private synchronized void checkStatus() {
		if (getLocalStatus() == TrustEntry.CERT_ACCEPTED &&
				getRemoteStatus() == TrustEntry.CERT_ACCEPTED)
			addTrust();
			
		
	}
	
	public synchronized int getRemoteStatus() {
		return remoteStatus;
	}
	
	public synchronized int getLocalStatus() {
		return localStatus;
	}
	
	public void setRemoteHostInfo(RemoteHost hostInfo) {
		_hostInfo = hostInfo;
	}
	
	public RemoteHost getRemoteHostInfo() {
		return _hostInfo;
	}


	
	public boolean isLocalRoleServer() {
		return _localRoleIsServer;
	}
	
	public String getStatusString(int status) {
		switch (status) {
		case UNKNOWN: return "Unknown";
		case PENDING: return "Pending";
		case CERT_ACCEPTED: return "Certificate accepted";
		case CERT_INVALID: return "Certificate invalid";
		case CERT_EXPIRED: return "Certificate expired";
		case CERT_REVOKED: return "Certificate revoked";
		case CERT_HOSTNAME_MISMATCH: return "Hostname mismatch";
		case ERROR: return "Error";
		case INITIALIZED: return "Initialized";
		}
		return "Status code unknown";
	}
	
	public String getRemoteStatusString() {
		return getStatusString(getRemoteStatus());
	}
	
	public String getLocalStatusString() {
		return getStatusString(getLocalStatus());
	}
	

	// the id of the local party TODO is this needed? no...
	public String getLocalID() {
		return _localID;
	}
	public void setLocalID(String localID) { //authhandler
		_localID = localID;
	}
	

	
	// set and get local certificate
	public String getLocalCertificate() { // processor
		return _localCertificate;
	}
	public void setLocalCertificate(String certificate) { //authhandler
		_localCertificate = certificate;
	}
	
	
	

	
	private String localInfo() {
		if (isLocalRoleServer())
			return ("LocalServer " + getLocalID() + "@" + getLocalSecureUrl());
		else
			return ("LocalClient " + getLocalID());			
	}
	
	private String remoteInfo() {
		if (isLocalRoleServer())
			return ("RemoteClient " + getRemoteID());
		else
			return ("RemoteServer " + getRemoteID() + "@" + getRemoteUrl());			
	}
	
	public String getLocalInfo() {
		return localInfo();
	}
	
	public String getRemoteInfo() {
		return remoteInfo();
	}
	
	
	
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while(getRemoteStatus() == PENDING && queriesMade < nrOfQueries) {
			try {
				Thread.sleep(queryInterval);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// return code handling?
			_authHandler.getRFAJMFProcessor().sendQuery(this);
			queriesMade++;
			log.debug("Remotely pending. queries sent: " + queriesMade);
		}
		
		queriesMade = 0;
		
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (this.isLocalRoleServer())
			buf.append("-:CONNECTION TO REMOTE CLIENT:-");
		else
			buf.append("-:CONNECTION TO REMOTE SERVER:-");
		buf.append("\nLocal party ID:           " + getLocalID());
		buf.append("\nRemote party ID:          " + getRemoteID());
		buf.append("\nRemote host info:         " + getRemoteHostInfo());
		buf.append("\nRemote party server url:  " + getRemoteUrl());
		buf.append("\nRemote party secureUrl:   " + _remoteSecureUrl);
		buf.append("\nLocal party SecureUrl:    " + getLocalSecureUrl());	
		//buf.append("\nLocal party certificate:  " + getLocalCertificate());
		buf.append("\nRemote party certificate: " + certificateInfo());
		buf.append("\nLocal status:             " + getStatusString(getLocalStatus()));
		buf.append("\nRemote status:            " + getStatusString(getRemoteStatus()));
		return buf.toString();
	}
	
	
	
	public void startProcess() {
		setRemoteStatus(PENDING);
		setLocalStatus(PENDING);
		Thread process = new Thread(this, getLocalID() + " querying " + getRemoteID());
		process.start();
	}
	
	
	private void deleteTrust() {
		if (isLocalRoleServer()) {
			log.debug("TRUSTED CLIENT DELETED: " + getRemoteID());
			_authHandler.deleteClientTrust(getRemoteID());
		}
		else {
			log.debug("TRUSTED SERVER DELETED: " + getRemoteID());
			_authHandler.deleteServerTrust(getRemoteID());
		}
		
		
	}
	
	private void addTrust() {
		if (isLocalRoleServer()) {
			_authHandler.addClientTrust(getRemoteID());
			log.debug("TRUSTED CLIENT ADDED: " + getRemoteID());
		}
		else {
			_authHandler.addServerTrust(getRemoteID());
			log.debug("TRUSTED SERVER ADDED: " + getRemoteID());
		}
	}
	
	
	
	
	
	private String certificateInfo() {
		StringBuffer buf = new StringBuffer();
		try {
		X509Certificate cert = _authHandler.getKeyManager()
			.generateX509Certificate(_remoteCertificate);
		buf.append("DN: " + cert.getSubjectX500Principal());
		} catch (Exception e) {
			log.debug("Could not extract certificate info.");
		}
		return buf.toString();
	}
	
	
	public void sendRemoteQuery() {
		_authHandler.getRFAJMFProcessor().sendQuery(this);
	}
	
	
	
	
	
	
	
	public String getRemoteCertificate() {
		return _remoteCertificate;
	}
	
	
	public void setRemoteCertificate(String certificate) {
		_remoteCertificate = certificate;
	}
	
	public String getRemoteID() {
		return _remoteID;
	}
	
	public void setRemoteID(String remoteID) {
		_remoteID = remoteID;
	}
	
	public String getRemoteUrl() {
		return _remoteUrl;
	}
	
	public String getRemoteSecureUrl() {
		return _remoteSecureUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		_remoteUrl = remoteUrl;
	}
	
	public void setRemoteSecureUrl(String remoteSecureUrl) {
		if (!isLocalRoleServer())
			_remoteSecureUrl = remoteSecureUrl;
		else throw new IllegalArgumentException("Can´t set secure url for a remote client");
	}
	
	public String getLocalSecureUrl() {
		if (isLocalRoleServer()) {
			if (_localSecureUrl == null) {
				_localSecureUrl = _authHandler.getSecureUrl();
			}
			return _localSecureUrl;
		} else return null;
	}
	
	public String getRole() {
		if (isLocalRoleServer())
			return "server";
		else return "client";
	}
	
	public String getRemoteRole() {
		if (isLocalRoleServer())
			return "client";
		else return "server";
	}
	
	public String getCertInfo() {
		return toString();
	}
	
	
	
	
	

}
