/*
 * Created on Sep 28, 2004
 */
package org.cip4.elk.testtools.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

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

import org.apache.commons.io.IOUtils;
import org.cip4.elk.ElkTestCase;
import org.cip4.elk.impl.testtools.servlet.MimeTestJMFServlet;
import org.cip4.elk.jmf.servlet.JMFServlet;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * This test requires that MimeTestJMFServlet is running at 
 * http://localhost:8080/elk/mime.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class MimeTestJMFServletTest extends ElkTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * A hack. This test case is used to create a MIME package for Alces to send
     * to Elk.
     * @throws Exception
     */
    public void testCreateMimeFile() throws Exception {
        String jmfResource = "data/MimeJMFServletTest/Alces-SubmitQueueEntry.jmf";
        String[] fileResources = {"data/MimeJMFServletTest/Alces-Approval.jdf",
                "data/MimeJMFServletTest/Alces-Figures.pdf"};
        String mimePackage = "src/test/data/MimeJMFServletTest/alces-approval.mjm";
        // Build MIME pacakge
        String mime = buildMimePackage(jmfResource, fileResources);
        PrintStream out = new PrintStream(new FileOutputStream(mimePackage));
        out.print(mime);
    }
    
    /**
     * Posts a MIME package containing JMF+JDF.
     * @throws Exception
     */
    public void testSendMime_JMFwithJDF() throws Exception {
        String jmfResource = "data/MimeJMFServletTest/SubmitQueueEntry_cid.jmf";
        String[] fileResources = {"data/MimeJMFServletTest/Approval_http.jdf"};
        // Build MIME pacakge
        String mime = buildMimePackage(jmfResource, fileResources);
        // Input stream from string
        InputStream inStream = new ByteArrayInputStream(mime.getBytes());
        
        // Create simulated container
        ServletRunner sr = new ServletRunner();        
        // Register servlet
        sr.registerServlet("mime", MimeTestJMFServlet.class.getName());        
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/mime", 
                inStream,
                JMFServlet.MIME_CONTENT_TYPE);
        // Post the request
        try {
            WebResponse response = sc.getResponse(request);
            assertEquals(response.getContentType(), JMFServlet.JMF_CONTENT_TYPE);
            String responseBody = IOUtils.toString(response.getInputStream());
            log.debug(responseBody);
            assertTrue(responseBody.indexOf("ReturnCode=\"0\"") != -1);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() != HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
    
    
    private String buildMimePackage(String jdfResource, String[] fileResources)
    throws Exception {
        return buildMimePackage(jdfResource, Arrays.asList(fileResources));
    }
    
    /**
     * Builds a MIME package.
     * @param jdfResource   the JMF or JDF to be stored as the first part
     *                      of the package
     * @param fileResources the other parts
     * @return a string containing the resulting MIME package
     * @throws Exception
     */
    private String buildMimePackage(String jdfResource, List fileResources) 
    throws Exception {
        log.debug("Building MIME package from: " + jdfResource + ";  " + fileResources);
        
        String jdf = getResourceAsJDF(jdfResource).getOwnerDocument_KElement().write2String(0);
        assertNotNull(jdf);
      
        // Create a MIME package
        Properties dummyProps = new Properties(); // Usually contains server, etc.
        Session mailSession = Session.getDefaultInstance(dummyProps);
        Message message = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart("related"); // JDF: multipart/related
        // Part 1 is JMF
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(jdf, "text/xml");
        messageBodyPart.setHeader("Content-Type", JMFServlet.JMF_CONTENT_TYPE); // JDF: application/vnd.cip4-jmf+xml
        multipart.addBodyPart(messageBodyPart);
        // Add other body parts
        for(int i=0, imax=fileResources.size(); i<imax; i++) {
            String fileResource = (String) fileResources.get(i);
            String fileName = new File(fileResource).getName();
            // Part 2 is JDF
            messageBodyPart = new MimeBodyPart();
            DataSource source = new URLDataSource(getResourceAsURL(fileResource));
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            //messageBodyPart.setHeader("Content-Type", JMFServlet.JDF_CONTENT_TYPE); // JDF: application/vnd.cip4-jdf+xml
            messageBodyPart.setHeader("Content-ID", "<" + fileName + ">"); // JDF: ID within < >; case insensitive; escape with %hh
            multipart.addBodyPart(messageBodyPart);
        }
        // Put parts in message
        message.setContent(multipart);
        // Writes message to string
        OutputStream outStream = new ByteArrayOutputStream();
        message.writeTo(outStream);
        String msgString = outStream.toString();
        log.info("MIME package:\n" + msgString);
        return msgString;
    }
    
    
    /**
     * Posts MIME package containing a JDF and three binary PDF files.
     * @throws Exception
     */
    public void testSendMime_JDFwithBinary() throws Exception {
        // Test files       
        String jdfResource = "data/MimeJMFServletTest/Approval_cid.jdf";
        String[] fileResources = {"data/MimeJMFServletTest/file1.pdf",
                                    "data/MimeJMFServletTest/file2.pdf",
                                    "data/MimeJMFServletTest/file3.pdf"};
        String mime = buildMimePackage(jdfResource, fileResources);

        // Input stream from string
        InputStream inStream = new ByteArrayInputStream(mime.getBytes());
        
        // Create simulated container
        ServletRunner sr = new ServletRunner();        
        // Register servlet
        sr.registerServlet("mime", MimeTestJMFServlet.class.getName());        
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/mime", 
                inStream,
                JMFServlet.MIME_CONTENT_TYPE);
        // Post the request
        try {
            WebResponse response = sc.getResponse(request);
            assertEquals(response.getContentType(), JMFServlet.JMF_CONTENT_TYPE);
            String responseBody = IOUtils.toString(response.getInputStream());
            log.debug(responseBody);
            assertTrue(responseBody.indexOf("ReturnCode=\"0\"") != -1); 
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }

    /**
     * Posts a corrupt MIME package.
     * @throws Exception
     */
    public void testCorruptMime() throws Exception {
        // Create simulated container
        ServletRunner sr = new ServletRunner();        
        // Register servlet
        sr.registerServlet("mime", MimeTestJMFServlet.class.getName());        
        // Random bytes for request body
        Random random = new Random();
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/mime", 
                new ByteArrayInputStream(randomBytes),
                JMFServlet.MIME_CONTENT_TYPE);
        // Post the request
        try {
            WebResponse response = sc.getResponse(request);
            assertEquals(response.getContentType(), JMFServlet.JMF_CONTENT_TYPE);
            String responseBody = IOUtils.toString(response.getInputStream());
            log.debug(responseBody);
            assertTrue(responseBody.indexOf("ReturnCode=\"0\"") == -1);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
    
    /**
     * Posts a random stream an uses content-type text/xml.
     * @throws Exception
     */
    public void testNoMime() throws Exception {
        // Create simulated container
        ServletRunner sr = new ServletRunner();        
        // Register servlet
        sr.registerServlet("mime", MimeTestJMFServlet.class.getName());        
        // Random bytes for request body
        Random random = new Random();
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        // Create a client for posting to the servlet
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new PostMethodWebRequest("http://blabla/mime", 
                new ByteArrayInputStream(randomBytes),
                "text/xml");
        // Post the request
        try {
            WebResponse response = sc.getResponse(request);
            assertNotSame(response.getContentType(), JMFServlet.JMF_CONTENT_TYPE);
            String responseBody = IOUtils.toString(response.getInputStream());
            log.debug(responseBody);
            assertTrue(responseBody.indexOf("ReturnCode=\"0\"") == -1);
        } catch(HttpException he) {
            assertTrue(he.getResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);    
        }
    }
}
