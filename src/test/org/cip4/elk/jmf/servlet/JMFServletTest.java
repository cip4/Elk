/*
 * Created on Sep 28, 2004
 */
package org.cip4.elk.jmf.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletResponse;

import org.cip4.elk.ElkTestCase;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class JMFServletTest extends ElkTestCase {
    public static String JDF_FILE = "data/jdf/Approval.jdf";
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Posts a JDF document.
     * @throws Exception
     */
    public void testPostJDF() throws Exception {
        log.debug("About to test postJDF");
        // Create simulated container
        ServletRunner sr = new ServletRunner();
        // Register servlet
        sr.registerServlet("jmf", EmptyJMFServlet.class.getName());
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/jmf", 
                getResourceAsStream(JDF_FILE), 
                JMFServlet.JDF_CONTENT_TYPE);
        
        log.debug("Created request: " + request);
        // Post the request
        try {
            // WebResponse response = sc.getResponse(request);
            sc.getResponse(request);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
    
    /**
     * Posts a JMF message.
     * @throws Exception
     */
    public void testPostJMF() throws Exception {
        // Create simulated container
        ServletRunner sr = new ServletRunner();
        // Register servlet
        sr.registerServlet("jmf", EmptyJMFServlet.class.getName());
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/jmf", 
                getResourceAsStream("data/Status.jmf"), 
                JMFServlet.JMF_CONTENT_TYPE);
        // Post the request
        try {
            if(sc == null){
                log.error("Unable to create ServletUnitClient...");
            }
            // WebResponse response = sc.getResponse(request);
            sc.getResponse(request);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
    
    /**
     * Posts a MIME package containing JMF+JDF.
     * @throws Exception
     */
    public void testSendMime_JMFwithJDF() throws Exception {
        // Load JMF
        String jmf = getResourceAsJDF("data/SubmitQueueEntry.jmf").getOwnerDocument_KElement().write2String(0);
        assertNotNull(jmf);
//        String jdf = getResourceAsJDF("data/Approval.jdf").getOwnerDocument_KElement().write2String(0);
//        assertNotNull(jdf);
        
        // Create a MIME package
        // String body = "This is the body"; // TODO Replace with JDF
        Properties dummyProps = new Properties(); // Usually contains server, etc.
        Session mailSession = Session.getDefaultInstance(dummyProps);
        Message message = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart("related"); // JDF: multipart/related
        // Part 1 is JMF
        BodyPart messageBodyPart = new MimeBodyPart();
        // TODO Set URL to point to part 2
        messageBodyPart.setContent(jmf, "text/xml");
        // Override content type
        messageBodyPart.setHeader("Content-Type", JMFServlet.JMF_CONTENT_TYPE); // JDF: application/vnd.cip4-jmf+xml
        multipart.addBodyPart(messageBodyPart);

        // Part 2 is JDF
        messageBodyPart = new MimeBodyPart();
        DataSource source = new URLDataSource(getResourceAsURL(JDF_FILE));
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("Approval.jdf");
        messageBodyPart.setHeader("Content-Type", JMFServlet.JDF_CONTENT_TYPE); // JDF: application/vnd.cip4-jdf+xml
        messageBodyPart.setHeader("Content-ID", "<1234>"); // JDF: ID within < >; case insensitive; escape with %hh
        multipart.addBodyPart(messageBodyPart);
        
        // Put parts in message
        message.setContent(multipart);
        // Writes message to string
        OutputStream outStream = new ByteArrayOutputStream();
        message.writeTo(outStream);
        String msgString = outStream.toString();
        log.info(msgString);
        // Input stream from string
        InputStream inStream = new ByteArrayInputStream(msgString.getBytes());
        
        // Create simulated container
        ServletRunner sr = new ServletRunner();
        // Register servlet
        sr.registerServlet("jmf", EmptyJMFServlet.class.getName());
        //sr.registerServlet("jmf", LoggingJMFServlet.class.getName());
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/jmf", 
                inStream,
                JMFServlet.MIME_CONTENT_TYPE);
        // Post the request
        try {
            // WebResponse response = sc.getResponse(request);
            sc.getResponse(request);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
    
    /**
     * A concrete but empty implementation of JMFServlet.
     * @author Claes Buckwalter (clabu@itn.liu.se)
     */
    public static class EmptyJMFServlet extends JMFServlet {
        // Nothing. Super class's implementation is used instead.
    }
    
}
