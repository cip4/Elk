/**
 * Created on Mar 22, 2006, 3:34:32 PM
 * org.cip4.elk.util.security.DefaultKeyManager.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * A Default implementation of the org.cip4.elk.util.security.KeyManager
 * interface.
 * 
 * This implementation provides a basic implementation for holding all 
 * the KeyStores with all keys and certificates. 
 *
 * @author Markus Nyman, (markus.cip4@myman.se)
 * 
 */
public class DefaultKeyManager implements KeyManager {

	protected Log log = LogFactory.getLog(this.getClass().getName());
	
	protected static final String BASE64_CERT_PREFIX = "-----BEGIN CERTIFICATE-----";
	protected static final String BASE64_CERT_POSTFIX = "-----END CERTIFICATE-----";
	
	protected static final String NEWLINE = System.getProperty("line.separator");

	public static final String DEFAULT_KEYSTORE_TYPE = "JKS";
	public static final String DEFAULT_CERT_TYPE = "X.509";

	/**
	 * A BASE64Encoder for encoding incoming String certificates
	 */
	protected BASE64Encoder _encoder;
	
	/**
	 * A BASE64Decoder to decode the Stored certs.
	 */
	protected BASE64Decoder _decoder;
	
	
	protected CertificateFactory _certFactory;

	
	private String userHome = System.getProperty("user.home");
	private String fileSeparator = System.getProperty("file.separator");


	private String serverAlias;
	private String clientAlias;
	
	// These strings are just referencing objects holding the KeyStores
	// one string can refer to the same KeyStore
	private String serverKS = "serverKS";
	private String clientKS = "clientKS";
	private String serverTS = "serverTS";
	private String clientTS = "clientTS";

	/**
	 * A prefix for the client certificate aliases in the KeyStore
	 */
	private String clientPrefix = "Client:";
	
	/**
	 * A prefix for the server certificate aliases in the KeyStore
	 */
	private String serverPrefix = "Server:";

	// A Vector holding all the KeyStoreInstances
	private Vector _keyStores;
	
	/**
	 * Default and only constructor.
	 * Initializes the Encoders and Decoders and
	 * All neccessary fields and objects.
	 */
	public DefaultKeyManager() {

		_encoder = new BASE64Encoder();
		_decoder = new BASE64Decoder();
		_keyStores = new Vector();
		
		try {
			_certFactory = CertificateFactory.getInstance(DEFAULT_CERT_TYPE);
		} catch (Exception e) {
			log.fatal("Could not instanciate CertificateFactory");
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Initializes the KeyManager with the given properties. The UID
	 * should be the UID field in the generated X509 certs. If the KeyStores
	 * are created externally, the *.cert.* properties can be omitted as well
	 * as the UID. Just pass "" as an uid-argument in that case.  
	 * 
	 * The Properties object should hold the following fields and all
	 * the properties should have the prefix given in propertyPrefix.
	 * 
	 * [propertyPrefix].ssl.server.alias
	 * [propertyPrefix].ssl.client.alias
	 * [propertyPrefix].ssl.server.keystore
	 * [propertyPrefix].ssl.server.keystorepass
	 * [propertyPrefix].ssl.server.keypass
	 * [propertyPrefix].ssl.client.keystore
	 * [propertyPrefix].ssl.client.keystorepass
	 * [propertyPrefix].ssl.client.keypass
	 * [propertyPrefix].ssl.client.truststore
	 * [propertyPrefix].ssl.client.truststorepass
	 * [propertyPrefix].ssl.server.truststore
	 * [propertyPrefix].ssl.server.truststorepass
	 * 
	 * If keyStores doesn´t exist, set these properties for
	 * certificate generation
	 * 
	 * [propertyPrefix].ssl.server.cert.keyalg
	 * [propertyPrefix].ssl.server.cert.validity
	 * [propertyPrefix].ssl.server.cert.CN
	 * [propertyPrefix].ssl.server.cert.OU
	 * [propertyPrefix].ssl.server.cert.O
	 * [propertyPrefix].ssl.server.cert.C
	 * 
	 * [propertyPrefix].ssl.client.cert.keyalg
	 * [propertyPrefix].ssl.client.cert.validity
	 * [propertyPrefix].ssl.client.cert.CN
	 * [propertyPrefix].ssl.client.cert.OU
	 * [propertyPrefix].ssl.client.cert.O
	 * [propertyPrefix].ssl.client.cert.C
	 * 
	 * This method sets the following SystemProperties. The KeyStore is
	 * set to the KeyStore holding the Client Key. Used in HttpClient.
	 * 
	 * javax.net.ssl.trustStore
	 * javax.net.ssl.trustStorePassword
	 * javax.net.ssl.keyStore
	 * javax.net.ssl.keyStorePassword
	 * 
	 * @param properties The init properties.
	 * @param propertyPrefix A prefix valid for all properties.
	 * @param uid The UID of this machine.
	 */
	public void init(Properties properties, String propertyPrefix, String uid) {
		log.info("initiating KeyManager for " + uid + ".");
		
		serverAlias = properties.getProperty(propertyPrefix + ".ssl.server.alias", "server");
		clientAlias = properties.getProperty(propertyPrefix + ".ssl.client.alias", "client");

		// If the default is changed, it must be changed in HttpReceiver too.
		// HttpReceiver uses default .alcesserverks.jks with pw changeit
		// if properties are not set
		String serverKSFName = properties.getProperty(
				propertyPrefix + ".ssl.server.keystore",
				"." + propertyPrefix + "serverks.jks");
		String serverKSFile = userHome + fileSeparator + serverKSFName;
		
		String serverKSPass = properties.getProperty(
				propertyPrefix + ".ssl.server.keystorepass",
				propertyPrefix + "changeit");

		String serverKPass = properties.getProperty(
				propertyPrefix + ".ssl.server.keypass",
				propertyPrefix + "changeit");
		
		String clientKSFName = properties.getProperty(
				propertyPrefix + ".ssl.client.keystore",
				"." + propertyPrefix + "clientks.jks");
		String clientKSFile = userHome + fileSeparator + clientKSFName;
		
		String clientKSPass = properties.getProperty(
				propertyPrefix + ".ssl.client.keystorepass",
				propertyPrefix + "changeit");

		String clientKPass = properties.getProperty(
				propertyPrefix + ".ssl.client.keypass",
				propertyPrefix + "changeit");

		
		String clientTSFName = properties.getProperty(
				propertyPrefix + ".ssl.client.truststore",
				"." + propertyPrefix + "clientts.jks");
		String clientTSFile = userHome + fileSeparator + clientTSFName;
		
		String clientTSPass = properties.getProperty(
				propertyPrefix + ".ssl.client.truststorepass",
				propertyPrefix + "changeit");

		
		// add separate serverTrustStore iff .ssl.server.trustStore is specified
		String serverTSFName = properties.getProperty(
				propertyPrefix + ".ssl.server.truststore");
		if (serverTSFName != null && !serverTSFName.equalsIgnoreCase(clientTSFName)) {
			String serverTSFile = userHome + fileSeparator + serverTSFName;
			
			String serverTSPass = properties.getProperty(
					propertyPrefix + ".ssl.server.truststorepass",
					propertyPrefix + "changeit");
			addKeyStore(serverTS, serverTSFile, serverTSPass);
		} else {
			serverTS = clientTS;
		}
		
		if (!keystoreExists(serverKSFile, serverKSPass, serverAlias)) {
			
			String keyalg = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.keyalg", "RSA");
			String validity = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.validity", "14");
			String cn = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.CN",
					propertyPrefix + ".server.default");
			String ou = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.OU",
					"default " + propertyPrefix);
			String o = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.O",
					"default org " + propertyPrefix);
			String c = properties.getProperty(
					propertyPrefix + ".ssl.server.cert.C", "DE");
			generateCertificate(serverAlias, cn, ou, o, c,
					uid, serverKSFile, serverKPass, serverKSPass, validity, keyalg);
		} 
		
		if(!keystoreExists(clientKSFile, clientKSPass, clientAlias)) {

			String keyalg = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.keyalg", "RSA");
			String validity = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.validity", "14");
			String cn = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.CN",
					propertyPrefix + ".client.default");
			String ou = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.OU",
					"default " + propertyPrefix);
			String o = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.O",
					"default org " + propertyPrefix);
			String c = properties.getProperty(
					propertyPrefix + ".ssl.client.cert.C", "DE");
			generateCertificate(clientAlias, cn, ou, o, c,
					uid, clientKSFile, clientKPass, clientKSPass, validity, keyalg);
		}

		
		// Adds the KeyStores to the local Vector of KeyStoreInstances
		addKeyStore(serverKS, serverKSFile, serverKSPass);
		addKeyStore(clientKS, clientKSFile, clientKSPass);
		addKeyStore(clientTS, clientTSFile, clientTSPass);
		
		// We need to save the TrustStore
		saveKeyStores();
		log.debug("Saving key stores.");
		
		// set system properties needed for Apache HttpClient
    	System.setProperty("javax.net.ssl.trustStore", clientTSFile);
    	System.setProperty("javax.net.ssl.trustStorePassword", clientTSPass);
    	System.setProperty("javax.net.ssl.keyStore", clientKSFile);
    	System.setProperty("javax.net.ssl.keyStorePassword", clientKSPass);

	}
	
	/**
	 * Returns the server certificate in PEM formatted String
	 */
	public String getServerCertificate() {
		return getPEMCertificate(serverKS, serverAlias);
	}
	
	
	/**
	 * Returns the client certificate in PEM formatted String
	 */
	public String getClientCertificate() {
		return getPEMCertificate(clientKS, clientAlias);
	}
	
	
	/**
	 * Validates a String to see if it is a valid PEM certificate
	 * @param certificate
	 * @return
	 */
	public boolean isValidCertificateFormat(String certificate) {
		return (generateX509Certificate(certificate) != null);
	}
	
	
	/**
	 * Adds the certificate from the TrustEntry in the trust store.
	 */
	public boolean addTrust(TrustEntry trustEntry) {
		log.debug("Adding certificate alias: " + trustEntry.getRemoteID() + " to trust store.");
		if (trustEntry.isLocalRoleServer())
			addPEMCertificate(serverTS, clientPrefix + trustEntry.getRemoteID(), trustEntry.getRemoteCertificate());
		else
			addPEMCertificate(clientTS, serverPrefix + trustEntry.getRemoteID(), trustEntry.getRemoteCertificate());
		saveKeyStores();
		return true;
	}
	
	/**
	 * removes the certificate belonging to the given TrustEntry
	 */
	public boolean deleteTrust(TrustEntry trustEntry) {

		log.debug("Deleting entry from trust store");
		if (trustEntry.isLocalRoleServer())
			deleteEntry(clientPrefix + trustEntry.getRemoteID(), serverTS);
		else
			deleteEntry(serverPrefix + trustEntry.getRemoteID(), clientTS);
		saveKeyStores();
		return false;
	}
	
	
	/**
	 * Checks if the keystore specified in the Properties file
	 * already exists. Returns True iff the file is a keystore
	 * with the given password AND the alias exists in the KeyStore
	 * 
	 * @param filename An absolute file path
	 * @param keyStorePass The password for the KeyStore.
	 * @param alias The searched alias.
	 * @return true iff file, store and alias exists.
	 */
	private boolean keystoreExists(String filename, String keyStorePass, String alias) {
		FileInputStream file = null;	
		try {
			file = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			//String[] test = {properties.getProperty(propertyPrefix + ".ssl.server.test")};
			log.info("File not found: " + filename);
			return false;
		}
		boolean ret = false;		
		if (file != null) {
			KeyStore ks = loadKeyStore(filename, keyStorePass);
			try {
				ret = ks.containsAlias(alias);
			} catch (KeyStoreException e) {
				ret = false;
				log.debug("KeyStore could not be opened: " + filename);
			}

		}
		
		return ret;

	}
	
	
	/**
	 * Generates a Certificate in the given KeyStore. Generates a string array
	 * for command line System calls. All attributes are passed on to a method
	 * executing a Runtime.exec(cmd[]) command.
	 * 
	 * NO FIELDS ARE CHECKED HERE! INPUT ONLY CORRECT PARAMETERS.
	 * 
	 * The check is done in the init() method
	 * 
	 * @param alias The KeyStore alias
	 * @param CN X509v1 Cert field
	 * @param OU X509v1 Cert field
	 * @param O X509v1 Cert field
	 * @param C X509v1 Cert field
	 * @param UID X509v1 Cert field
	 * @param keyStoreFile
	 * @param keypass
	 * @param storepass
	 * @param validity
	 * @param keyalg
	 * @return Return code of the Runtime call. Normally 0.
	 */
	private boolean generateCertificate(String alias, String CN, String OU,
			String O, String C, String UID, String keyStoreFile,
			String keypass, String storepass, String validity, String keyalg) {
		String[] cmd = {"keytool", "-genkey", "-alias", alias, "-dname",
				("\"CN=" + CN + ", OU="+OU+", O="+O+", C="+C+", UID="+UID+"\""),
				"-keystore", keyStoreFile,
				"-keypass",	keypass, "-storepass", storepass,
				"-validity", validity, "-keyalg", keyalg};
		log.info("Generating key store and storing key for alias " + alias);
		int ret = executeCommand(cmd);
		log.info("Key store generated and stored in " + keyStoreFile);
		return (ret == 0);
	}
	
	
	/**
	 * Executes a Runtime.exec(cmd[]) command and waits for the
	 * Process to execute. Returns the command exit value
	 * 
	 * @param cmd A command line command.
	 * @return The exit value.
	 */
	private int executeCommand(String[] cmd) {
		Process p = null;
		// TODO ATT!!! prints passwords!
		if (log.isDebugEnabled() && cmd.length>0) {
			StringBuffer buf = new StringBuffer(cmd[0]);
			for (int i=1; i<cmd.length; i++)
				buf.append(" " + cmd[i]);
			log.debug("Executing system command: " + buf.toString());
		}	
		
		try {
			p = Runtime.getRuntime().exec(cmd);
			// wait for the process since the command may need to be
			// executed completely before continuing.
			p.waitFor();
			log.debug("Returned with exit code: " + p.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return p.exitValue();
		
	}
	


	
	
	
	
	/**
	 * Adds a KeyStore to the list of KeyStore references.
	 * The KeyStore is referred to as the attribute keyStoreFile
	 * @param keyStoreFile The location of the KeyStore and 
	 * the name of the String reference to this KeyStore
	 * @param keyStorePass the KeyStore password
	 * 
	 * @see getKeyStore(String id)
	 * @see KeyStoreInstance
	 */
	protected void addKeyStore(String keyStoreFile, String keyStorePass) {
		KeyStoreInstance ksi = new KeyStoreInstance(keyStoreFile, keyStoreFile, keyStorePass);
		_keyStores.add(ksi);
	}
	
	
	/**
	 * Adds a KeyStore with the reference id to the list of KeyStores.
	 *  
	 * @param id The reference String
	 * @param keyStoreFile the KeyStore file location
	 * @param keyStorePass the KeyStore pass
	 * 
	 * @see getKeyStore(String id)
	 * @see KeyStoreInstance
	 */
	private void addKeyStore(String id, String keyStoreFile, String keyStorePass) {
		KeyStoreInstance ksi = new KeyStoreInstance(id, keyStoreFile, keyStorePass);
		_keyStores.add(ksi);
	}
	
	
	/**
	 * Adds a certificate to the KeyStore referred as keyStoreId.
	 * The cert must be in PEM format. that is with the leading and tailing
	 * -----BEGIN CERTIFICATE----- and ----END CERTIFICATE----
	 * 
	 * @param keyStoreId The string reference to the KeyStore
	 * @param alias The alias to store the cert as
	 * @param pemCertificate the Certificate in PEM format
	 * 
	 * @see getKeyStore(String id)
	 * @see KeyStoreInstance
	 */
	private void addPEMCertificate(String keyStoreId, String alias, String pemCertificate){
		KeyStore keyStore = getKeyStoreInstance(keyStoreId).getKeyStore();
		storePEMCertificate(alias, pemCertificate, keyStore);		
	}
	
	
	/**
	 * Returns the Certificate belonging to alias from the KeyStore
	 * refered as keyStoreId. The cert has the start and end tags:
	 * -----BEGIN CERTIFICATE----- and ----END CERTIFICATE----
	 * 
	 * @param keyStoreId
	 * @param alias Certificate alias
	 * @return PEM certificate, 
	 */
	private String getPEMCertificate(String keyStoreId, String alias) {
		KeyStore keyStore = getKeyStoreInstance(keyStoreId).getKeyStore();
		return loadPEMCertificate(alias, keyStore);				
	}
	
	
	/**
	 * Saves all the KeyStores managed by this KeyManager
	 */
	public void saveKeyStores() {
		Iterator it = _keyStores.iterator();
		while (it.hasNext()) {
			((KeyStoreInstance) it.next()).save();
		}
	}
	
	
	/**
	 * Returns the local, private representation of the
	 * KeyStore referred by id. 
	 * 
	 * @param id the id of the KeyStoreInstance.
	 * @return in the local KeyStore representation.
	 * 
	 * @see KeyStoreInstance
	 */
	private KeyStoreInstance getKeyStoreInstance(String id) {
		KeyStoreInstance ksi = null;
		Iterator it = _keyStores.iterator();
		while(it.hasNext()) {
			ksi = (KeyStoreInstance) it.next();
			if (ksi.getId().equals(id))
				return ksi;
				
		}
		log.info("Unable to find keystore " + id);
		return ksi;
	}
	
	
	/**
	 * Loads a KeyStore and returns the initiated KeyStore reference.
	 * Calls loadKeyStore(String keyStoreFile, String keyStorePass, String keyStoreType)
	 * with the default KeyStore type
	 * 
	 * @param keyStoreFile file name
	 * @param keyStorePass KeyStore pass
	 * @return KeyStore loaded from the given file
	 */
	private KeyStore loadKeyStore(String keyStoreFile, String keyStorePass) {
		return loadKeyStore(keyStoreFile, keyStorePass, DEFAULT_KEYSTORE_TYPE);
	}
	
	private KeyStore getKeyStore(String id) {
		return getKeyStoreInstance(id).getKeyStore();
	}
	

	/**
	 * Loads a <code>KeyStore</code> from a file and returns it. 
	 * TODO add throw exception
	 * @param keyStoreFile the relative or absolute path to the KeyStore file
	 * @param keyStorePass password for the KeyStore
	 * @param keyStoreType the type of this KeyStore
	 * @return The KeyStore
	 */
	private KeyStore loadKeyStore(String keyStoreFile, String keyStorePass, String keyStoreType) {
		KeyStore keyStore = null;	
		FileInputStream file = null;	
		try {
			file = new FileInputStream(keyStoreFile);
		} catch (FileNotFoundException e) {
			log.info("File not found: " + keyStoreFile + ". New empty key store created.");
		}
		try {
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(file, (keyStorePass).toCharArray());
			if (file != null)
				file.close();
		} catch (IOException e) {
			log.error("I/O problem or incorrect password.");
			e.printStackTrace();
		} catch (KeyStoreException e) {
			log.error("Could not iniate KeyStore type: " + keyStoreType);
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			log.error("KeyStore integrity check failed.");
			e.printStackTrace();
		} catch (CertificateException e) {
			log.error("Certificates in KeyStore could not be loaded.");
			e.printStackTrace();
		}
		return keyStore;
	}
	
	
	/**
	 * Saves a <code>KeyStore</code> to the specified file and with the specified password
	 * TODO create keystores if they are not found
	 * @param keystore 
	 * @param keystoreFile
	 * @param keystorePass
	 */
	private void saveKeyStore(KeyStore keyStore, String keyStoreFile, String keyStorePass) {
		try {
			FileOutputStream fos = new FileOutputStream(keyStoreFile);
			keyStore.store(fos, (keyStorePass).toCharArray());
			fos.close();	
		} catch (FileNotFoundException e) {
			log.error("KeyStore file: " + keyStoreFile + " could not be saved.");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("I/O problem saving KeyStore.");
			e.printStackTrace();
		} catch (KeyStoreException e) {
			log.error("KeyStore is not initialized.");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			log.error("KeyStore integrity check failed.");
			e.printStackTrace();
		} catch (CertificateException e) {
			log.error("Certificates in KeyStore could not be stored.");
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Returns a Certificate in PEM String format.
	 * 
	 * @param alias The alias
	 * @param keyStore KeyStore to search.
	 * @return PEM certificate
	 */
	private String loadPEMCertificate(String alias, KeyStore keyStore) {
		return addPreAndPostfix(loadBASE64Certificate(alias, keyStore));
	}
	
	
	/**
	 * Stores a certificate to the given KeyStore.
	 * 
	 * @param alias Certificate is stored under this alias.
	 * @param pemCertificate The certificate.
	 * @param keyStore Where to store the certificate.
	 * @return true if successful
	 */
	private boolean storePEMCertificate(String alias, String pemCertificate, KeyStore keyStore) {
		return storeBASE64Certificate(alias, removePreAndPostfix(pemCertificate), keyStore);
	}

	
	/**
	 * Deletes an alias from a KeyStore.
	 * 
	 * @param alias Alias
	 * @param keyStoreID A locally represented KeyStore.
	 * @return true if successful
	 */
	private boolean deleteEntry(String alias, String keyStoreID) {
		KeyStore keyStore = getKeyStore(keyStoreID);
		try {
			keyStore.deleteEntry(alias);
		} catch (KeyStoreException e) {
			log.error("Unable to delete alias " + alias + " from trust store.");
			if (log.isDebugEnabled())
				e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	
	/**
	 * Returns a BASE64Encoded cerificate. A BASE64 Encoded cert is
	 * a PEM cert without the leading -----BEGIN CERTIFICATE-----
	 * and the tailing -----END CERTIFICATE-----.
	 * 
	 * Prints stacktraces if errors occur and log.isDebugEnabled().
	 *  
	 * @param alias Alias.
	 * @param keystore KeyStore to browse.
	 * @return Returns a BASE64Cert or null if errors occur. 
	 */
	private String loadBASE64Certificate(String alias, KeyStore keystore) {
		String retVal = null;
		try {
			retVal = _encoder.encode(keystore.getCertificate(alias).getEncoded());
		} catch (KeyStoreException e) {
			log.error("KeyStore is not properly initiated.");
			if (log.isDebugEnabled())
				e.printStackTrace();
		} catch (CertificateEncodingException e) {
			log.error("Error encoding certificate.");
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		return retVal;
	}
	
	
	/**
	 * Stores a BASE64 encoded string to a given KeyStore.
	 * 
	 * Prints stacktraces if errors occur and log.isDebugEnabled().
	 * 
	 * @param alias alias to store as
	 * @param base64Certificate Certificate in BASE64 format
	 * @param keyStore Target KeyStore
	 * @return true if no errors occur
	 */
	private boolean storeBASE64Certificate(String alias, String base64Certificate, KeyStore keyStore) {
		try {
			byte[] decodedCert = _decoder.decodeBuffer(base64Certificate); // ByteBuffer
			Certificate cert = _certFactory.generateCertificate(
					new ByteArrayInputStream(decodedCert));		
			keyStore.setCertificateEntry(alias, cert);		
		} catch (CertificateException e) {
			log.error("Error parsing BASE64 Certificate.");
			if (log.isDebugEnabled())
				e.printStackTrace();
			return false;
		} catch (IOException e) {
			log.error("I/O error parsing BASE64 Certificate.");
			if (log.isDebugEnabled())
				e.printStackTrace();
			return false;
		} catch (KeyStoreException e) {
			log.error("KeyStore is not properly initiated.");
			if (log.isDebugEnabled())
				e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Generates an X509Certificate from a valid PEM cert String.
	 * For outside classes who needs to access X509 cert fields.
	 */
	public X509Certificate generateX509Certificate(String pemCertificate) {
		String certificate = removePreAndPostfix(pemCertificate);
		X509Certificate returnCert = null;
		try {
			byte[] decodedCert = _decoder.decodeBuffer(certificate); // ByteBuffer
			returnCert = (X509Certificate)_certFactory.generateCertificate(
					new ByteArrayInputStream(decodedCert));				
		} catch (CertificateException e) {
			log.error("Error parsing BASE64 Certificate.");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("I/O error parsing BASE64 Certificate.");
			e.printStackTrace();
		}
		return returnCert;
	}

	
	/**
	 * Removes the PEM fields -----BEGIN CERTIFICATE-----
	 * and -----END CERTIFICATE----- from the PEM certificate.
	 * I.e This returns the BASE64 encoded cert.
	 * 
	 * @param pemCertificate Certificate in PEM String format.
	 * @return a BASE64 Encoded String.
	 */
	private String removePreAndPostfix(String pemCertificate) {
		return pemCertificate.substring(
				BASE64_CERT_PREFIX.length(), 
				pemCertificate.length()-BASE64_CERT_POSTFIX.length());
	}
	
	
	/**
	 * Adds the PEM fields to the BASE64 certificate
	 * @see removePreAndPostfix(String)
	 * 
	 * @param base64Certificate BASE64 String
	 * @return PEM String
	 */
	private String addPreAndPostfix(String base64Certificate) {
		return (BASE64_CERT_PREFIX + NEWLINE + base64Certificate + NEWLINE + BASE64_CERT_POSTFIX);
	}
	
	
	//////////////////////////////////////////////////////////////////
	// KeyStoreInstance. A KeyStore Wrapper class.                  //
	//////////////////////////////////////////////////////////////////
	
	
	/**
	 * A private class for representing KeyStores.
	 * These objects holds, apart from an ID, 
	 * the KeyStore object, the file where the
	 * KeyStore is saved and the KeyStore password.
	 * 
	 * These objects are just KeyStore wrappers with some
	 * additional information.
	 *
	 * @author Markus Nyman, (markus.cip4@myman.se)
	 *
	 */
	private class KeyStoreInstance {
		
		// The wrapper class fields
		private String _id = null;
		private String _fileName = null;
		private String _password = null;
		private KeyStore _keyStore = null;

		
		/**
		 * This is the main Constructor.
		 * 
		 * @param id
		 * @param keyStoreFile
		 * @param keyStorePass
		 */
		public KeyStoreInstance(String id, String keyStoreFile,
				String keyStorePass) {
			_id = id;
			_fileName = keyStoreFile;
			_password = keyStorePass;
			_keyStore = loadKeyStore(keyStoreFile, keyStorePass);
		}
		
		
		/**
		 * Creates an instance from an already initiated KeyStore
		 * 
		 * @param id
		 * @param keyStoreFile
		 * @param keyStorePass
		 * @param keyStore
		 */
		public KeyStoreInstance(String id, String keyStoreFile,
				String keyStorePass, KeyStore keyStore) {
			_id = id;
			_fileName = keyStoreFile;
			_password = keyStorePass;
			_keyStore = keyStore;
		}
		
		
		/**
		 * Allows different KeyStore type
		 * 
		 * @param id
		 * @param keyStoreFile
		 * @param keyStorePass
		 * @param keyStoreType
		 */
		public KeyStoreInstance(String id, String keyStoreFile,
				String keyStorePass, String keyStoreType) {
			_id = id;
			_fileName = keyStoreFile;
			_password = keyStorePass;
			_keyStore = loadKeyStore(keyStoreFile, keyStorePass, keyStoreType);		
		}
		
				
		/**
		 * @return Returns the fileName.
		 */
		public String getFileName() {
			return _fileName;
		}

		
		/**
		 * @return Returns the keyStore.
		 */
		public KeyStore getKeyStore() {
			return _keyStore;
		}


		/**
		 * @return Returns the name.
		 */
		public String getId() {
			return _id;
		}
		
		
		/**
		 * Saves the KeyStore object to the specified file
		 */
		public void save() {
			saveKeyStore(_keyStore, _fileName, _password);
		}
	
	}

}
