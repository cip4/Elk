/**
 * Created on Mar 22, 2006, 3:31:25 PM
 * org.cip4.elk.util.security.AbstractRFAJMFProcessor.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import org.apache.commons.logging.Log;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * TODO JAVADOC and string format
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public abstract class AbstractRFAJMFProcessor implements RFAJMFProcessor {
	
	protected static final String MESSAGE_TYPE = "RequestForAuthentication"; 
	
	/**
	 * Valid Elements in a RFA Command.
	 * Variable names are the same as the element id but with a lower case first letter.
	 */
	public static final String AUTH_CMD_PARAMS_TAG = "AuthenticationCmdParams";
	public static final String AUTH_RESP_TAG = "AuthenticationResp";
	public static final String CERTIFICATE_TAG = "Certificate";
	public static final String AUTH_QU_PARAMS_TAG = "AuthenticationQuParams";
	
	/**
	 * Parameter AuthenticationType and its possible enumeration values. 
	 * Variable names are the same as the element id but with a lower case first letter.
	 */
	public static final String AUTH_TYPE_TAG = "AuthenticationType";
	public static final String AS_CLIENT_TAG = "AsClient";
	public static final String AS_SERVER_TAG = "AsServer";
	
	/**
	 * Parameter Reason and its possible enumeration values
	 * Variable names are the same as the element id but with a lower case first letter.
	 */
	public static final String REASON_STR = "Reason";
	public static final String INITIATE_CONNECTION_STR = "InitiateConnection";
	public static final String CLIENT_CERT_EXPIRED_TAG = "ClientCertificateExpired";
	public static final String SERVER_CERT_EXPIRED_TAG = "ServerCertificateExpired";
	public static final String CLIENT_HOSTNAME_MISMATCH_TAG = "ClientHostnameMismatch";
	public static final String SERVER_HOSTNAME_MISMATCH_TAG = "ServerHostnameMismatch";
	public static final String CLIENT_CERT_REVOKED_TAG = "ClientCertificateRevoked";
	public static final String SERVER_CERT_REVOKED_TAG = "ServerCertificateRevoked";
	public static final String OTHER_TAG = "Other";
	
	/**
	 * Parameter ReasonDetails and its possible enumeration values
	 */	
	public static final String REASON_DETAILS_TAG = "ReasonDetails";
	
	/**
	 * Parameter SecureURL and its possible enumeration values
	 */	
	public static final String SECURE_URL_TAG = "SecureURL";
	
	/**
	 * JDFJMF error codes. should be in JDFJMF
	 */
	public static final int SUCCESS = 0; 						// q c 
	public static final int INTERNAL_ERROR = 2; 				// q c
	public static final int QUERYCOMMAND_NOT_IMPLEMENTED = 5; 	// q c lower layers
	public static final int INVALID_PARAMETERS = 6; 			// q c lower layers
	public static final int INSUFFICIENT_PARAMETERS = 7; 		// q c lower layers
	public static final int AUTHENTICATION_DENIED = 300; 		// q
	public static final int SECURE_CHANNEL_NOT_SUPPORTED = 301; //   c lower layers
	public static final int SECURE_CHANNEL_REQUIRED = 302; 		//   c lower layers
	public static final int CERTIFICATE_EXPIRED = 303; 			// q c
	public static final int AUTH_PENDING = 304; 				// q c
	public static final int AUTH_ALREADY_ESTABLISHED = 305; 	//   c this layer
	public static final int NO_AUTH_REQUEST_IN_PROCESS = 306; 	// q
	public static final int CERTIFICATE_INVALID = 307; 			// q c
	
	
	/**
	 * The owner of this RFAJMFProcessor
	 */
	protected AuthenticationHandler _authHandler;
	
	protected Log log;
	
	/**
	 * Setter for this RFAJMFProcessors AuthenticationHandler
	 * @param authHandler
	 */
	public void setAuthenticationHandler(AuthenticationHandler authHandler) {
		_authHandler = authHandler;
	}
	
	/**
	 * Getter for this RFAJMFProcessors AuthenticationHandler
	 * @return
	 */
	public AuthenticationHandler getAuthenticationHandler() {
		return _authHandler;
	}
	
	
	public abstract JDFJMF dispatchJDF(JDFJMF output, String url);
	
	/**
	 * Checks if there is a trust established with this client and
	 * a remote server
	 * 
	 * @param senderID the remote server
	 * @return true iff TrustEntry exists and the certs are accepted by
	 * both parties (remotely and locally)
	 */
	private boolean isClientTrustEstablished(String senderID) {
		TrustEntry trustEntry = _authHandler.getClientTrustEntry(senderID);
		if (trustEntry == null)
			return false;
		else
			return (TrustEntry.CERT_ACCEPTED == trustEntry.getLocalStatus() &&
					TrustEntry.CERT_ACCEPTED == trustEntry.getRemoteStatus());
	}
	
	
	/**
	 * Checks if there is a trust established with this server and
	 * a remote client
	 * 
	 * @param senderID the remote client
	 * @return true iff TrustEntry exists and the certs are accepted by
	 * both parties (remotely and locally)
	 */
	private boolean isServerTrustEstablished(String senderID) {
		TrustEntry trustEntry = _authHandler.getServerTrustEntry(senderID);
		if (trustEntry == null)
			return false;
		else
			return (TrustEntry.CERT_ACCEPTED == trustEntry.getLocalStatus() &&
					TrustEntry.CERT_ACCEPTED == trustEntry.getRemoteStatus());
	}

	
	/**
	 * Sends an RFA query to the remote party associated
	 * with the TrustEntry and sets the status of the trustEntry
	 * according to the response.
	 */
	public boolean sendQuery(TrustEntry trustEntry) {
		// get all info from trustEntry.. url, role, senderID
		JDFJMF query = null;
		if (trustEntry.isLocalRoleServer())
			query = createRFAQuery(AS_SERVER_TAG);
		else
			query = createRFAQuery(AS_CLIENT_TAG);
		
		// TODO this might aswell be sent to the queryconstructor
		query.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(query, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA query. Query might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1;
		try {			
			returnCode = response.getResponse().getReturnCode();	
		} catch (NullPointerException e) {
			log.debug("Unexpected or invalid response to RFA query.");
			return false;
		}
		
		
		switch (returnCode) {
		case AUTH_PENDING: 
			trustEntry.setRemoteStatus(TrustEntry.PENDING);
			break;
		case SUCCESS:
			if(!trustEntry.isLocalRoleServer()) {
				String secUrl = response.getResponse()
				.getElement(AUTH_RESP_TAG).getAttribute(SECURE_URL_TAG);
				trustEntry.setRemoteSecureUrl(secUrl);
			}
			trustEntry.setRemoteStatus(TrustEntry.CERT_ACCEPTED); 
			return true;
		case AUTHENTICATION_DENIED:
			trustEntry.setRemoteStatus(TrustEntry.CERT_REVOKED);
			break;
		case CERTIFICATE_EXPIRED:
			trustEntry.setRemoteStatus(TrustEntry.CERT_EXPIRED);
			break;
		case CERTIFICATE_INVALID:
			trustEntry.setRemoteStatus(TrustEntry.CERT_INVALID);
			break;
		default:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
		log.debug("Response to RFA query handled in processQueryResponse().");	
		break;
		
		}		
		return processQueryResponse(response, trustEntry);
		
	}
	
	/**
	 * Further processing of a RFA query response.
	 * Override this method if new returnCodes are added.
	 * @param response the response to RFA query
	 * @param trustEntry the corresponding TrustEntry
	 * @return false
	 * 
	 * @see TrustEntry
	 * @see sendQuery(TrustEntry trustEntry)
	 */
	protected boolean processQueryResponse(JDFJMF response, TrustEntry trustEntry) {
		log.debug("Processing query response with code: " 
				+ response.getResponse().getReturnCode()
				+ " queried by " + trustEntry.getRemoteInfo());
		return false;
	}
	
	
	
	/**
	 * Returns true if the response returnCode is either SUCCESS (0) or
	 * AUTH_PENDING (304) and false if AUTHENTICATION_DENIED (300) or if any
	 * response parameters are invalid or insufficient. Does not check
	 * the validity of the senderID or the certificate. That should be done
	 * in <code>AuthenticationHandler</code>.
	 * 
	 * Otherwise this method returns what method 
	 * processCommandResponse returns.
	 * 
	 * Sets the TrustEntry.remoteStatus to ERROR if the initiation failed 
	 * or some error occured.
	 * 
	 * @see processCommandResponse(JDFJMF response, TrustEntry trustEntry)
	 */
	public boolean sendInitiateConnectionCommand(TrustEntry trustEntry) {
		
		JDFJMF command = null;
		if (trustEntry.isLocalRoleServer())
			command = createInitiateConnectionCmd(
					AS_SERVER_TAG, trustEntry.getLocalCertificate());
		else
			command = createInitiateConnectionCmd(
					AS_CLIENT_TAG, trustEntry.getLocalCertificate());
		// TODO this might aswell be sent to the command constructor
		command.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(command, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA command. Command might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1; 
		String certificate = null;
		String senderID = null;
		
		try {
			senderID = response.getJMFRoot().getSenderID();
			returnCode = response.getResponse().getReturnCode();
		} catch (NullPointerException e) {
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.error("Invalid response to RFA InitiateConnection command.");
			return false;
		}
		if (senderID == null) {
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.error("Insufficient response parameters to RFA InitiateConnection command.");
			return false;
		}
		
		trustEntry.setRemoteID(senderID);
		
		switch (returnCode) {
		case AUTH_PENDING:
			try {
				certificate = response.getResponse().
				getElement(AUTH_RESP_TAG).getElement(CERTIFICATE_TAG).getText();
			} catch (NullPointerException e) {
				trustEntry.setRemoteStatus(TrustEntry.ERROR);
				log.error("Invalid response to RFA InitiateConnection command. No certificate received.");
				return false;
			}
			trustEntry.setRemoteCertificate(certificate);
			trustEntry.setRemoteStatus(TrustEntry.PENDING);
			log.debug("Authentication pending at remote party: " + trustEntry.getRemoteInfo() + ".");
			return true;
		case SUCCESS:
			try {
				certificate = response.getResponse().
				getElement(AUTH_RESP_TAG).getElement(CERTIFICATE_TAG).getText();
			} catch (NullPointerException e) {
				trustEntry.setRemoteStatus(TrustEntry.ERROR);
				log.error("Invalid response to RFA InitiateConnection command. No certificate received.");
				return false;
			}
			trustEntry.setRemoteCertificate(certificate);
			trustEntry.setRemoteStatus(TrustEntry.CERT_ACCEPTED);
			log.debug("Certificate accepted at remote party: " + trustEntry.getRemoteInfo() + ".");
			return true;
			// The followig cases all return false.
		case AUTHENTICATION_DENIED:
			log.debug("Certificate revoked at remote party: " + trustEntry.getRemoteInfo() + ".");
			trustEntry.setRemoteStatus(TrustEntry.CERT_REVOKED);
			return false;
		case INTERNAL_ERROR:
			trustEntry.setRemoteStatus(TrustEntry.ERROR); 
			log.debug("Internal error at remote party: " + trustEntry.getRemoteInfo() + ".");
			break;
		case QUERYCOMMAND_NOT_IMPLEMENTED:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("RFA InitiateConnection command not implemented by remote party: " 
					+ trustEntry.getRemoteInfo() + ".");
			break;
		case INVALID_PARAMETERS:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("Invalid request parameters sent to " 
					+ trustEntry.getRemoteInfo() + ".");
			break;
		case INSUFFICIENT_PARAMETERS:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("Insufficient request parameters sent to " 
					+ trustEntry.getRemoteInfo() + ".");
			break;
		case SECURE_CHANNEL_NOT_SUPPORTED:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("Secure channel not supported by " 
					+ trustEntry.getRemoteInfo() + ".");
			break;
		case SECURE_CHANNEL_REQUIRED:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("Secure channel required by " + trustEntry.getRemoteInfo() + ".");
			break;
		case CERTIFICATE_EXPIRED:
			trustEntry.setRemoteStatus(TrustEntry.CERT_EXPIRED);
			log.debug("Certificate sent to " + trustEntry.getRemoteInfo() + " has expired.");
			break;
		case AUTH_ALREADY_ESTABLISHED:
			//trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("Authentication already established with " + trustEntry.getRemoteInfo() + ".");
			break;
		case NO_AUTH_REQUEST_IN_PROCESS:
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
			log.debug("No authentication request in process for " + trustEntry.getRemoteInfo() + ".");
			break;
		case CERTIFICATE_INVALID:
			trustEntry.setRemoteStatus(TrustEntry.CERT_INVALID);
			log.debug("Certificate sent to " + trustEntry.getRemoteInfo() + " is invalid.");
			break;
		default:
			log.debug("Response to RFA InitiateConnection command handled in processCommandResponse().");	
		}
		return processCommandResponse(response, trustEntry);
	}
	
	/**
	 * Further processing of a InitiateConnection response.
	 * Override this method if new returnCodes are added.
	 * @param response the response to RFA InitiateConnection command
	 * @param trustEntry the corresponding TrustEntry
	 * @return false
	 * 
	 * @see TrustEntry
	 * @see sendInitiateConnectionCommand(TrustEntry trustEntry)
	 */
	protected boolean processCommandResponse(JDFJMF response, TrustEntry trustEntry) {
		return false;
	}
	
	
	
	
	
	/**
	 * Returns true if response returnCode is SUCCESS (0), false otherwise.
	 */
	public boolean sendCertificateExpiredCommand(TrustEntry trustEntry) {
		JDFJMF command = null;
		if (trustEntry.isLocalRoleServer())
			command = createCertificateExpiredCmd(AS_SERVER_TAG);
		else
			command = createCertificateExpiredCmd(AS_CLIENT_TAG);
		// TODO this might aswell be sent to the command constructor
		command.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(command, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA CertificateExpired command." 
					+ " Command might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1;
		try {			
			returnCode = response.getResponse().getReturnCode();	
		} catch (NullPointerException e) {
			log.debug("Unexpected or invalid response to RFA CertificateExpired command.");
			return false;
		}
		if (returnCode != SUCCESS)
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
		return (returnCode == SUCCESS);
	}
	
	/**
	 * Returns true if response returnCode is SUCCESS (0), false otherwise.
	 */
	public boolean sendHostnameMismatchCommand(TrustEntry trustEntry) {
		JDFJMF command = null;
		if (trustEntry.isLocalRoleServer())
			command = createHostnameMismatchCmd(AS_SERVER_TAG);
		else
			command = createHostnameMismatchCmd(AS_CLIENT_TAG);
		// TODO this might aswell be sent to the command constructor
		command.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(command, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA command. Command might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1;
		try {			
			returnCode = response.getResponse().getReturnCode();	
		} catch (NullPointerException e) {
			log.debug("Unexpected or invalid response to RFA HostnameMismatch command.");
			return false;
		}
		if (returnCode != SUCCESS)
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
		
		return (returnCode == SUCCESS);
	}
	
	/**
	 * Returns true if response returnCode is SUCCESS (0), false otherwise.
	 */
	public boolean sendCertificateRevokedCommand(TrustEntry trustEntry) {
		JDFJMF command = null;
		if (trustEntry.isLocalRoleServer())
			command = createCertificateRevokedCmd(AS_SERVER_TAG);
		else
			command = createCertificateRevokedCmd(AS_CLIENT_TAG);
		// TODO this might aswell be sent to the command constructor
		command.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(command, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA CertificateRevoked command." 
					+ " Command might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1;
		try {			
			returnCode = response.getResponse().getReturnCode();	
		} catch (NullPointerException e) {
			log.debug("Unexpected or invalid response to RFA CertificateRevoked command.");
			return false;
		}
		if (returnCode != SUCCESS)
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
		
		return (returnCode == SUCCESS);
	}
	
	/**
	 * Returns true if response returnCode is SUCCESS (0), false otherwise.
	 */
	public boolean sendOtherReasonCommand(TrustEntry trustEntry, String reasonDetails) {
		JDFJMF command = null;
		if (trustEntry.isLocalRoleServer())
			command = createOtherReasonCmd(AS_SERVER_TAG, reasonDetails);
		else
			command = createOtherReasonCmd(AS_CLIENT_TAG, reasonDetails);
		// TODO this might aswell be sent to the command constructor
		command.setSenderID(trustEntry.getLocalID());
		
		JDFJMF response = dispatchJDF(command, trustEntry.getRemoteUrl());
		
		if (response == null) {
			log.debug("Invalid response to RFA Other command." 
					+ " Command might not have been dispatched correctly.");
			return false;			
		}
		
		int returnCode = -1;
		try {			
			returnCode = response.getResponse().getReturnCode();	
		} catch (NullPointerException e) {
			log.debug("Unexpected or invalid response to RFA Other command.");
			return false;
		}
		if (returnCode != SUCCESS)
			trustEntry.setRemoteStatus(TrustEntry.ERROR);
		
		return (returnCode == SUCCESS);
	}
	
	
	
	
	
	
	
	////////////////////////////////////////////////////////
	// Incoming messages processing methods               //
	////////////////////////////////////////////////////////
	
	
	
	/**
	 * Processes an RFA message.
	 * 
	 * TODO change this to (JDFMessage, JDFResponse, RemoteHost)
	 */
	public int processJDF(JDFJMF input, JDFJMF output, RemoteHost hostInfo) {
		if (input.getCommand() != null) {
			processJDFCommand(input.getCommand(), output.getResponse(), hostInfo);
			return output.getResponse().getReturnCode();
		}
		else if (input.getQuery() != null){
			processJDFQuery(input.getQuery(), output.getResponse(), hostInfo);
			return output.getResponse().getReturnCode();
		}
		else {
			JDFResponse response = output.getResponse();
			response.setReturnCode(INVALID_PARAMETERS);
			appendNotification(response, JDFNotification.EnumClass.Error, 
			"Request could not be resolved. Neither a RFA Command nor RFA Query.");
			return INVALID_PARAMETERS; // TODO get this from JDFJMF.INVALID_PARAMETERS
		}
	}
	
	
	/**
	 * Processes an RFA Command.
	 * @param command command
	 * @param response response
	 * @param hostInfo remote host info
	 */
	public void processJDFCommand(JDFCommand command, JDFResponse response, RemoteHost hostInfo) {
		
		String commandReason = null;
		String commandType = null;
		String senderID = null;
		KElement elem = null;
		
		// Validate that all necessary fields are set
		// TODO validate this in processJDF()
		try {
			elem = command.getElement(AUTH_CMD_PARAMS_TAG);
			
			// Extract elements and check they are not empty. 
			// TODO is all checks neccessary?
			commandReason = elem.getAttribute(REASON_STR);			
			commandType = elem.getAttribute(AUTH_TYPE_TAG);
			senderID = command.getJMFRoot().getSenderID();
		} catch (NullPointerException e) {
			log.debug("Invalid JDF command parameters.");
			response.setReturnCode(INVALID_PARAMETERS);
			return;
		}
		
		if (commandReason == null || commandType == null 
				|| senderID == null || commandReason.equals("")
				|| commandType.equals("") || senderID.equals("")) {
			response.setReturnCode(INSUFFICIENT_PARAMETERS);
		}
		
		else if (commandType.equals(AS_CLIENT_TAG)) {
			if (commandReason.equals(INITIATE_CONNECTION_STR)) {
				// start initiate connection process
				if (isClientTrustEstablished(senderID)) {
					// check if trust is already established and if so
					// respond with AUTH_ALREADY_ESTABLISHED to all RFA commands
					response.setReturnCode(AUTH_ALREADY_ESTABLISHED);
				}
				else {
					String certificate = elem.getElement(CERTIFICATE_TAG).getText();
					processClientInitiateCmd(senderID, certificate, response, hostInfo);	
				}
			}
			else if (commandReason.equals(SERVER_CERT_EXPIRED_TAG)) {	
				processServerCertificateExpiredCmd(senderID, response, hostInfo);				
			}
			else if (commandReason.equals(SERVER_HOSTNAME_MISMATCH_TAG)) {
				processServerHostnameMismatchCmd(senderID, response, hostInfo);
			}
			else if (commandReason.equals(SERVER_CERT_REVOKED_TAG)) {
				processServerCertificateRevokedCmd(senderID, response, hostInfo);
			}
			else if (commandReason.equals(OTHER_TAG)) {
				String details = elem.getAttribute(REASON_DETAILS_TAG);
				processServerOtherCmd(senderID, details, response, hostInfo);
			} else {
				response.setReturnCode(INVALID_PARAMETERS);
			}
			
			
		}
		else if (commandType.equals(AS_SERVER_TAG)) {
			if (commandReason.equals(INITIATE_CONNECTION_STR)) {
				if (isServerTrustEstablished(senderID)) {
					// check if trust is already established
					response.setReturnCode(AUTH_ALREADY_ESTABLISHED);
				}
				else {
					String certificate = elem.getElement(CERTIFICATE_TAG).getText();
					processServerInitiateCmd(senderID, certificate, response, hostInfo);
				}
			}
			else if (commandReason.equals(CLIENT_CERT_EXPIRED_TAG)) {
				processClientCertificateExpiredCmd(senderID, response, hostInfo);
			}
			else if (commandReason.equals(CLIENT_HOSTNAME_MISMATCH_TAG)) {
				processClientHostnameMismatchCmd(senderID, response, hostInfo);
			}
			else if (commandReason.equals(CLIENT_CERT_REVOKED_TAG)) {
				processClientCertificateRevokedCmd(senderID, response, hostInfo);
			}
			else if (commandReason.equals(OTHER_TAG)) {
				String details = elem.getAttribute(REASON_DETAILS_TAG);
				processClientOtherCmd(senderID, details, response, hostInfo);
			} else {
				response.setReturnCode(INVALID_PARAMETERS);
			}
			
		}
		else response.setReturnCode(INVALID_PARAMETERS);
		
		
	}
	
	
	/**
	 * Processes an RFA query.
	 * 
	 * @param query query
	 * @param response response
	 * @param hostInfo info abt the remote host
	 */
	public void processJDFQuery(JDFQuery query, JDFResponse response, RemoteHost hostInfo) {
		
		KElement elem = null;
		String queryType = null;
		String senderID = null;
		
		// Validate that all necessary fields are set
		// TODO validate this in processJDF()
		try {
			elem = query.getElement(AUTH_QU_PARAMS_TAG);		
			
			// Extract elements and check they are not empty. 
			// TODO is all checks neccessary?
			queryType = elem.getAttribute(AUTH_TYPE_TAG);
			senderID = query.getJMFRoot().getSenderID();
		} catch (NullPointerException e) {
			log.error("Invalid JDF query parameters.");
			response.setReturnCode(INVALID_PARAMETERS);
			return;
		}
		
		if (queryType == null || senderID == null || 
				queryType.equals("") || senderID.equals("")) {
			//log.error("Insufficient parameters in RFA query.");
			// TODO get this from JDFJMF class
			response.setReturnCode(INSUFFICIENT_PARAMETERS);
		} else {
			if (queryType.equals(AS_CLIENT_TAG))
				processClientQuery(senderID, response, hostInfo);
			else if (queryType.equals(AS_SERVER_TAG))
				processServerQuery(senderID, response, hostInfo);
			else
				response.setReturnCode(INVALID_PARAMETERS);
		}
		
	}
	
	
	public void processClientQuery(String senderID, JDFResponse response,
			RemoteHost hostInfo) {
		processQuery(senderID, AS_CLIENT_TAG, response, hostInfo);
	}
	
	public void processServerQuery(String senderID, JDFResponse response,
			RemoteHost hostInfo) {
		processQuery(senderID, AS_SERVER_TAG, response, hostInfo);
	}
	
	/**
	 * Processes a Query.
	 * @param senderID sender id
	 * @param senderRole sender role, "AsServer" or "AsClient"
	 * @param response response
	 * @param hostInfo remote host information
	 */
	protected final void processQuery(String senderID, String senderRole,
			JDFResponse response, RemoteHost hostInfo) {
		TrustEntry trustEntry = null;
		// get server or client TrustEntry depending on serverRole
		if (senderRole.equals(AS_CLIENT_TAG))
			trustEntry = _authHandler.getClientTrustEntry(senderID);
		else
			trustEntry = _authHandler.getServerTrustEntry(senderID);
		// if no TrustEntry matches senderID, assume there is no authProcess
		if (trustEntry == null) {
			log.info("No authentication request in process for " + senderID + " " + senderRole);
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
		} else {
			// read the local status of the TrustEntry once 
			// since it can change while processing.
			int localStatus = trustEntry.getLocalStatus();
			
			switch (localStatus) {
			
			case TrustEntry.PENDING:
				response.setReturnCode(AUTH_PENDING); break;
			case TrustEntry.CERT_ACCEPTED:
				// set the secure URL in the response iff local role is server
				if (trustEntry.isLocalRoleServer())
					response.appendElement(AUTH_RESP_TAG).setAttribute(SECURE_URL_TAG, trustEntry.getLocalSecureUrl());
				response.setReturnCode(SUCCESS); break;
			case TrustEntry.CERT_REVOKED:
				// certificate revoked by local party
				response.setReturnCode(AUTHENTICATION_DENIED); break;
			case TrustEntry.CERT_EXPIRED:
				// certificate expired
				response.setReturnCode(CERTIFICATE_EXPIRED); break;
			case TrustEntry.CERT_INVALID:
				// certificate invalid.
				response.setReturnCode(CERTIFICATE_INVALID); break;
			case TrustEntry.CERT_HOSTNAME_MISMATCH:
				// cert hostname doesn�t match the hostInfo
				response.setReturnCode(AUTHENTICATION_DENIED); break;
			default:
				log.error("Unhandled TrustEntry.STATUS: " + localStatus 
						+ " while processing Query."); 
			response.setReturnCode(INTERNAL_ERROR); break;
			
			}
			
		}
	}
		
	
	/**
	 * Processes an incoming RFA Command with type AsServer
	 * TODO check other things as Already initiated, cert invalid aso
	 * @param senderID sender id
	 * @param certificate cert
	 * @param response response
	 * @param hostInfo remote host info
	 */
	public void processClientInitiateCmd(String senderID,
			String certificate, JDFResponse response, RemoteHost hostInfo) {
		// FIXME The query url must be present at this stage
		
		TrustEntry trustEntry = getAuthenticationHandler().getClientTrustEntry(senderID);
		if (trustEntry != null) {
			((DefaultTrustEntry)trustEntry).startProcess();
			response.setReturnCode(AUTH_PENDING);
			response.appendElement(AUTH_RESP_TAG).appendElement(CERTIFICATE_TAG).setText(trustEntry.getLocalCertificate());			
		} else {
			trustEntry = getAuthenticationHandler().initIncomingClientConnection(senderID, certificate, null, hostInfo);
			if (trustEntry == null) {
				response.setReturnCode(INTERNAL_ERROR);
				log.error("Could not initatate RFA process as client.");
			} else {
				response.setReturnCode(AUTH_PENDING);
				response.appendElement(AUTH_RESP_TAG).appendElement(CERTIFICATE_TAG).setText(trustEntry.getLocalCertificate());
			}
		}
	}
	
	
	/**
	 * Processes an incoming RFA Command with type AsClient
	 * TODO check other things as Already initiated, cert invalid aso
	 * @param senderID sender id
	 * @param certificate cert
	 * @param response response
	 * @param hostInfo remote host info
	 */
	public void processServerInitiateCmd(String senderID,
			String certificate, JDFResponse response, RemoteHost hostInfo) {
		// FIXME The query url must be present at this stage
		TrustEntry trustEntry = getAuthenticationHandler().getServerTrustEntry(senderID);
		if (trustEntry != null) {
			((DefaultTrustEntry)trustEntry).startProcess();
			response.setReturnCode(AUTH_PENDING);
			response.appendElement(AUTH_RESP_TAG).appendElement(CERTIFICATE_TAG).setText(trustEntry.getLocalCertificate());
		} else {
			trustEntry = getAuthenticationHandler().initIncomingServerConnection(senderID, certificate, null, hostInfo);
			if (trustEntry == null) {
				response.setReturnCode(INTERNAL_ERROR);
				log.info("Could not process RFA as server.");
			} else {
				response.setReturnCode(AUTH_PENDING);
				response.appendElement(AUTH_RESP_TAG).appendElement(CERTIFICATE_TAG).setText(trustEntry.getLocalCertificate());
			}	
		}
	}
	
	
	
	////////////////////////////////////////////////////////////////////////
	//
	//    Processors for other incoming commands.
	//    These all responds NO_AUTH_REQUEST_IN_PROCESS if thats the case
	//
	////////////////////////////////////////////////////////////////////////

	
	protected void processClientCertificateExpiredCmd(String sendingServerID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ClientCertificateExpired received from " + sendingServerID);
		TrustEntry trustEntry = getAuthenticationHandler().getServerTrustEntry(sendingServerID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_EXPIRED);
			response.setReturnCode(SUCCESS);
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processServerCertificateExpiredCmd(String sendingClientID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ServerCertificateExpired received from " + sendingClientID);
		TrustEntry trustEntry = getAuthenticationHandler().getClientTrustEntry(sendingClientID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_EXPIRED);
			response.setReturnCode(SUCCESS);	
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processClientHostnameMismatchCmd(String sendingServerID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ClientHostnameMismatch received from " + sendingServerID);
		TrustEntry trustEntry = getAuthenticationHandler().getServerTrustEntry(sendingServerID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_HOSTNAME_MISMATCH);
			response.setReturnCode(SUCCESS);	
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processServerHostnameMismatchCmd(String sendingClientID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ServerHostnameMismatch received from " + sendingClientID);
		TrustEntry trustEntry = getAuthenticationHandler().getClientTrustEntry(sendingClientID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_HOSTNAME_MISMATCH);
			response.setReturnCode(SUCCESS);	
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processClientCertificateRevokedCmd(String sendingServerID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ClientCertificateRevoked received from " + sendingServerID);
		TrustEntry trustEntry = getAuthenticationHandler().getServerTrustEntry(sendingServerID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_REVOKED);
			response.setReturnCode(SUCCESS);	
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processServerCertificateRevokedCmd(String sendingClientID, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("ServerCertificateRevoked received from " + sendingClientID);
		TrustEntry trustEntry = getAuthenticationHandler().getClientTrustEntry(sendingClientID);
		if (trustEntry != null) {
			trustEntry.setRemoteStatus(TrustEntry.CERT_REVOKED);
			response.setReturnCode(SUCCESS);	
		} else
			response.setReturnCode(NO_AUTH_REQUEST_IN_PROCESS);
	}
	protected void processClientOtherCmd(String sendingServerID, String message, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("Other received from server " + sendingServerID + " MESSAGE: " + message);
		response.setReturnCode(SUCCESS);	
	}
	protected void processServerOtherCmd(String sendingClientID, String message, JDFResponse response, RemoteHost hostInfo) {
		getAuthenticationHandler().notifyUser("Other received from client " + sendingClientID + " MESSAGE: " + message);
		response.setReturnCode(SUCCESS);	
	}
	
	
	
	
	
	
	/**
	 * Creates a notification.
	 * 
	 * @param response
	 *            the response to append the notification to
	 * @param notClass
	 *            the class of the notification
	 * @param msg
	 *            a message that will be appended as a comment to the
	 *            notification
	 * @see com.heidelberg.JDFLib.Auto.JDFAutoNotification
	 */
	protected void appendNotification(JDFResponse response,
			JDFNotification.EnumClass notificationClass, String message) {
		JDFNotification notification = response.appendNotification();
		notification.setClass(notificationClass);
		JDFComment comment = notification.appendComment();
		comment.appendText(message);
	}
		
	
	/**
	 * Creates and returns a <em>ClientCertificateExpired</em> or a
	 * <em>ServerCertificateExpired</em> RFA command depending on the
	 * input parameter <code>myRole</code>.
	 * <br><br>
	 * If <code>myRole</code> is <code>"AsClient"</code> the returned RFA command
	 * will be a <em>ServerCertificateExpired</em> <code>JDFJMF</code> and vice versa.
	 * <em>AuthenticationType</em> is set to <code>myRole</code> and
	 * the JMF:s <em>SenderID</em> is set to whatever <code>getSenderID()</code> returns.
	 * 
	 * @param myRole The value for the <em>AuthenticationType</em> attribute
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> command. 
	 */
	protected JDFJMF createCertificateExpiredCmd(String myRole) {
		return createCmd(myRole, "CertificateExpired");
	}
	
	
	/**
	 * Creates and returns a <em>ClientHostnameMismatch</em> or a
	 * <em>ServerHostnameMismatch</em> RFA command depending on the
	 * input parameter <code>myRole</code>.
	 * <br><br>
	 * If <code>myRole</code> is <code>"AsClient"</code> the returned RFA command
	 * will be a <em>ServerHostnameMismatch</em> <code>JDFJMF</code> and vice versa.
	 * <em>AuthenticationType</em> is set to <code>myRole</code> and
	 * the JMF:s <em>SenderID</em> is set to whatever <code>getSenderID()</code> returns.
	 * 
	 * @param myRole The value for the <em>AuthenticationType</em> attribute
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> command. 
	 */
	protected JDFJMF createHostnameMismatchCmd(String myRole) {
		return createCmd(myRole, "HostnameMismatch");
	}
	
	
	/**
	 * Creates and returns a <em>ClientCertificateRevoked</em> or a
	 * <em>ServerCertificateRevoked</em> RFA command depending on the
	 * input parameter <code>myRole</code>.
	 * <br><br>
	 * If <code>myRole</code> is <code>"AsClient"</code> the returned RFA command
	 * will be a <em>ServerCertificateRevoked</em> <code>JDFJMF</code> and vice versa.
	 * <em>AuthenticationType</em> is set to <code>myRole</code> and
	 * the JMF:s <em>SenderID</em> is set to whatever <code>getSenderID()</code> returns.
	 * 
	 * @param myRole The value for the <em>AuthenticationType</em> attribute.
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> command. 
	 */
	protected JDFJMF createCertificateRevokedCmd(String myRole) {
		return createCmd(myRole, "CertificateRevoked");
	}
	
	
	/**
	 * Creates and returns a RFA command with <em>Reason</em>
	 * set to <em>Other</em> and a <code>String</code> with reason details.
	 * <br><br>
	 * <em>AuthenticationType</em> is set to <code>myRole</code> and
	 * the JMF:s <em>SenderID</em> is set to whatever <code>getSenderID()</code> returns.
	 * <em>ReasonDetails</em> are set to the input <code>reasonMessage</code>.
	 * 
	 * @param myRole The value for the <em>AuthenticationType</em> attribute.
	 * @param reasonMessage The value for the <em>ReasonDetails</em> element.
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> command. 
	 */
	protected JDFJMF createOtherReasonCmd(String myRole, String reasonMessage) {
		JDFDoc document = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = document.getJMFRoot();
		//TODO set sender last. jmf.setSenderID(getSenderID());
		JDFCommand command =(JDFCommand) jmf.appendMessageElement (EnumFamily.Command, null);
		command.setType(MESSAGE_TYPE);
		KElement elem = command.appendElement(AUTH_CMD_PARAMS_TAG);
		elem.setAttribute(AUTH_TYPE_TAG, myRole);        
		elem.setAttribute(REASON_STR, "Other");
		elem.appendElement(REASON_DETAILS_TAG).setText(reasonMessage);
		return jmf;
	}
	
	
	/**
	 * Creates and returns a RFA <em>InitiateConnection</em> command.
	 * <br><br>
	 * <em>AuthenticationType</em> is set to <code>myRole</code> and
	 * the JMF:s <em>SenderID</em> is set to whatever <code>getSenderID()</code> returns.
	 * <em>Certificate</em> is set to the input <code>certificate</code>. No certificate
	 * validation is performed.
	 * 
	 * @param myRole The value for the <em>AuthenticationType</em> attribute.
	 * @param certificate A PEM MD5 formatted <code>String</code> for the 
	 * <em>Certificate</em> element.
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> command.
	 */
	protected JDFJMF createInitiateConnectionCmd(String myRole, String certificate) {
		JDFDoc document = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = document.getJMFRoot();
		//TODO set sender last. jmf.setSenderID(getSenderID());
		JDFCommand command =(JDFCommand) jmf.appendMessageElement (EnumFamily.Command, null);
		command.setType(MESSAGE_TYPE);
		KElement elem = command.appendElement(AUTH_CMD_PARAMS_TAG);
		elem.setAttribute(AUTH_TYPE_TAG, myRole);        
		elem.setAttribute(REASON_STR, INITIATE_CONNECTION_STR);
		elem.appendElement(CERTIFICATE_TAG).setText(certificate);
		// TODO what doc
		return jmf;
	}
	
	
	/**
	 * Creates a <em>RFA Query</em> <code>JDFJMF</code>. The only 
	 * attribute set in this method is <em>AuthenticationType</em> 
	 * wich is set to <code>myRole</code>.
	 * <br><br>
	 * The JMF:s <em>SenderID</em> is set by calling <code>getSenderID()</code>
	 * <br><br>
	 * This returns a ready to send <code>JDFJMF</code>.
	 * 
	 * @param myRole The role to set in the <em>AuthenticationType</em> attribute.
	 * @return A new <code>JDFJMF</code> instance of a <em>RequestForAuthentication</em> query. 
	 */
	protected JDFJMF createRFAQuery(String myRole) {
		JDFDoc document = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = document.getJMFRoot();
		// TODO set sender last. jmf.setSenderID(getSenderID());
		JDFQuery query =(JDFQuery) jmf.appendMessageElement (EnumFamily.Query, null);
		query.setType(MESSAGE_TYPE);
		query.appendElement(AUTH_QU_PARAMS_TAG).setAttribute(AUTH_TYPE_TAG, myRole);
		return jmf;
	}
	
	
	
	/////////////////////
	// Private methods //
	/////////////////////
	
	/**
	 * Creates a <em>RFA Command</em> template and returns a reference to
	 * the <em>AuthenticationCmdParams</em> <code>KElement</code>. The only 
	 * attribute that is not set in this method is the <em>Reason</em> 
	 * attribute. Any additional elements must be set by the caller.
	 * <br><br>
	 * The JMF:s <em>SenderID</em> is set by calling <ref>getSenderID()</ref>
	 * @deprecated Create the KElement in calling function
	 * @param role The role to set in the <em>AuthenticationType</em> attribute.
	 * @return The <code>KElement</code> <em>AuthenticationCmdParams</em>. 
	 */
	protected KElement createJMFCommandTemplate(String role) {
		JDFDoc document = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = document.getJMFRoot();
		//TODO set sender last. jmf.setSenderID(getSenderID());
		JDFCommand command =(JDFCommand) jmf.appendMessageElement (EnumFamily.Command, null);
		command.setType(MESSAGE_TYPE);
		KElement elem = command.appendElement(AUTH_CMD_PARAMS_TAG);
		elem.setAttribute(AUTH_TYPE_TAG, role);        
		return elem;
	}
	
	/**
	 * Creates a <em>RFA command</em> and sets the attributes 
	 * <em>AuthenticationType</em> and <em>Reason</em> to 
	 * <code>myRole</code> and <code>reason</code> respectively.
	 * <br><br>
	 * The command attribute <em>Reason</em> is set to <code>reason</code>
	 * with the prefix <code>"Server"</code> or <code>"Client"</code>
	 * depending on <code>myRole</code>. If <code>myRole</code> is 
	 * <code>"AsClient"</code> the prefix is set to <code>"Server"</code> 
	 * and vice versa.
	 * <br><br>
	 * If myRole is not <code>"AsClient"</code> or <code>"AsServer"</code>
	 * this method returns returns null.
	 * 
	 * @param myRole The role of the sender of this command
	 * @param reason The command <em>Reason</em> attribute without 
	 * "Client" or "Server" prefix.
	 * @return A new instance of <code>JDFJMF</code> <em>RequestForAuthentication</em> 
	 * command  with the attributes <em>AuthenticationType</em> and <em>Reason</em> set.
	 */
	protected JDFJMF createCmd(String myRole, String reason) {
		JDFDoc document = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = document.getJMFRoot();
		//TODO set sender last. jmf.setSenderID(getSenderID());
		JDFCommand command =(JDFCommand) jmf.appendMessageElement (EnumFamily.Command, null);
		command.setType(MESSAGE_TYPE);
		KElement elem = command.appendElement(AUTH_CMD_PARAMS_TAG);
		elem.setAttribute(AUTH_TYPE_TAG, myRole);        
		if (myRole.equals(AS_CLIENT_TAG))
			elem.setAttribute(REASON_STR, "Server" + reason);
		else if (myRole.equals(AS_SERVER_TAG))
			elem.setAttribute(REASON_STR, "Client" + reason);
		else {
			return null;
		}
		return jmf;
		
	}
	
}
