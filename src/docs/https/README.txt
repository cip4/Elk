This version of ELK supports the Request for authentication (RFA) 
JMF messaging specification. It has been tested running on tomcat 
interacting with an https/RFA supporting version of alces.


------------
Requirements
------------

In order to make this implementation work, tomcat must be set up for https
communication. A Https-Connector must be specified in the <TOMCAT_HOME>/conf/server.xml
file (SERVER.XML). The same settings must be used in elk and this is done manually in
the rfa.properties file. The location of the rfa.properties file is by default
in the org.cip4.elk.impl.util.security package.

All settings relevant to the certificates and keystores used are set here.
Default files that can be used should be supplied in this folder

Tomcat also needs a cip4 JSSE implementation in order to
allow runtime changes in the truststore. (You don�t need to 
restart tomcat after adding a trusted certificate into the
specified truststore file). If this JSSEImplementation isn�t
specified, tomcat has to be restarted in order
to handle new certificates stored in the truststore file.

The JSSE Implementation is defined cip4JSSEImpl-<yyyymmdd>.jar




----------------------------------
Quick Start Guide for Tomcat 5.5.x
----------------------------------

 1. Set up the HTTPS Connector in tomcats server.xml file
 2. If you have the cip4JSSEImpl.jar, add the tag in the Connector
 3. Build ELK and copy the .war file to the webapps folder in tomcat
 4. Start Tomcat
 5. Verify that the default keystores are created in your user.home directory.
 	There should be 4 .jks files with the prefix RFA. (i.e RFAelkts.jks)
 	If not, the keytool command is probably not in the system path
 	You can check that by simply typing "keytool" in a shell or command prompt
 6. Verify that the Elk JDF Device is running by pointing your web browser to the URL http://localhost:8080/elk-printing/jmf
 7. Connect your RFA implementing JDF Controller (for example Alces) to the Elk JDF Device using the URL http://localhost:8080/elk-printing/jmf
 8. You can check the contents of the keystores with the "keytool -list -keystore <ks-file.jks>" command
 	The passwords are specified in the rfa.properties file	
 9. If this doesn�t work, send an email to markus.cip4<at>myman.se

--------------------------
Default settings in tomcat
--------------------------

This is the default Connector used in the implementation:
Insert this Connector into existing <tomcat home>/conf/server.xml
or overwrite the existing server.xml with the one supplied.
Remember to set the <user home> folder in the file.

If you don�t have the cip4JSSEImplementation you should ommit the
line 
    		   SSLImplementation="org.cip4.ssl.JSSEImplementation"

Default Connector:

    <Connector port="8443" maxHttpHeaderSize="8192"
    		   SSLImplementation="org.cip4.ssl.JSSEImplementation"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" scheme="https" secure="true"
               clientAuth="true" sslProtocol="TLS" 
               keystoreFile="<user home>/RFAtomcatks.jks" 
               keystoreType="JKS"
               keystorePass="tomcatks" 
               truststoreFile="<user home>/RFAtomcatts.jks"
               truststoreType="JKS"
               truststorePass="tomcatts" />
               

If you want to change these properties you have to change the
org/cip4/elk/impl/util/security/rfa.properties file accordingly

If you have the cip4JSSEImpl.jar you must copy it into tomcats classpath;
preferrably in <TOMCAT_HOME>/server/lib

The implementation is supplied in cip4JSSEImpl-<yyyymmdd>.jar

copy the jar file into folder: <tomcat home>/server/lib
and restart the server.

-----------------------
Remote host information
-----------------------

If you want remote host information to be accessed from the authHandler class
you must make two changes:

	1. 	change the jmf servlet class in web.xml to the one supplied in
		the org.cip4.elk.impl.util.security package.
	2.  change the incomingDispatcher in elk-spring-config.xml to
		the one in the org.cip4.elk.impl.util.security package.

Change the DispatcingJMFServlet to SecureDispatchingJMFServlet in web.xml

	<servlet>
		<description>A servlet that dispatches JMF messages to an
			IncomingJMFDispatcher.</description>
		<display-name>Dispatching JMF servlet </display-name>
		<servlet-name>DispatchingJMFServlet</servlet-name>
		<servlet-class>org.cip4.elk.impl.util.security.SecureDispatchingJMFServlet</servlet-class>
		<load-on-startup>20</load-on-startup>
	</servlet>

Change the bean reference in elk-spring-config to:

	<bean id="incomingDispatcher" class="org.cip4.elk.impl.util.security.SubscribingIncomingRFAJMFDispatcher" singleton="true">


That should be it!

Markus Nyman



If you have any questions please send an email to markus.cip4<at>myman.se
