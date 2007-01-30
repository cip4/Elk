=======================
Elk @build.timestamp@ (build @build.number@)
=======================

The main goal of the Elk Framework project is to create an application framework that provides the services needed by a JDF Device or Controller. A sub-goal of the project is to create a reference implementation of the Elk Framework. The hope is that the reference implementation will be able to serve as a test tool and a starting point for others wishing to implement a JDF enabled system.


------------
Requirements
------------

- Java 1.4.2 or later
- A servlet container with support for Servlet 2.4 API and JSP 2.0, for example Apache Tomcat 5.5.x.

--------
Features
--------

- CheckJDF-J validation of received JDF
- Implements the JDF 1.2 Base ICS 1.0 level 3 Worker Interface (hot folders excluded)
- Device capabilities checking
- Two simulated process implementations: Approval and ConventionalPrinting
- Web-based user interface for monitoring
- And more...

------------
Known Issues
------------

See http://www.cip4.org/jira/browse/ELK

-------------
Documentation
-------------

For general help and API documentation see the project web site http://elk.itn.liu.se

-----------------
Quick Start Guide
-----------------

The Elk reference devices come in the form of J2EE web applications bundled as WAR files. The Elk reference devices are deployable in a servlet container without any additional configuration. In theory, Elk should run in any servlet container but has currently only been tested with Apache Tomcat. Installation:

 1. Install Java JDK 5.0
 2. Install Apache Tomcat 5.5.x
 3. Download and unpack the latest Elk binary distribution (elk-DATE-bin.zip)
 4. Install the Elk ConventionalPrinting JDF Device by copying elk-printing.war to Tomcat's webapps folder
 5. Start Tomcat
 6. Verify that the Elk JDF Device is running by pointing your web browser to the URL http://localhost:8080/elk-printing/jmf
 7. Connect your JDF Controller (for example Alces) to the Elk JDF Device using the URL http://localhost:8080/elk-printing/jmf

--------
Building
--------

For documentation on how to build Elk, see the developer's tutorial at the Elk web site http://elk.itn.liu.se.
