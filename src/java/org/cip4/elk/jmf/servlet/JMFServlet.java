/*
 * Created on Sep 12, 2004
 */
package org.cip4.elk.jmf.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * A servlet that processes JMF messages.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 *
 * @web:servlet name="JMFServlet" 
 *              display-name="Elk JMF Servlet" 
 *              description="A servlet that processes JMF messages" 
 *              load-on-startup="1"
 *
 * @web:servlet-init-param  name="spring.config.file" 
 *                          value="spring-config.xml"
 *                          description="Classpath to springs configuration file"
 *
 * @web:servlet-mapping url-pattern="/jmf"
 */
public abstract class JMFServlet extends HttpServlet {

    public static final String JDF_CONTENT_TYPE = "application/vnd.cip4-jdf+xml";
    public static final String JMF_CONTENT_TYPE = "application/vnd.cip4-jmf+xml";
    public static final String MIME_CONTENT_TYPE = "multipart/related";
    public static final String JDF_EXTENSION = ".jdf";
    public static final String JMF_EXTENSION = ".jmf";
    public static final String JDF_MIME_EXTENSION = ".mjd";
    public static final String JMF_MIME_EXTENSION = ".mjm";
    public static final String SERVLET_NAME = "JMF Servlet";
    protected Logger log;
    
    public void init() throws ServletException {
        super.init();
        log = Logger.getLogger(this.getClass().getName());        
    }
    
    public String getServletName() {
        return SERVLET_NAME;
    }
    
    /**
     * Entry point that delegates to the process methods based on the 
     * request header <code>Content-type</code>.
     * @see #JDF_CONTENT_TYPE
     * @see #JMF_CONTENT_TYPE
     * @see #MIME_CONTENT_TYPE
     * @see #processJDF(HttpServletRequest, HttpServletResponse)
     * @see #processJMF(HttpServletRequest, HttpServletResponse)
     * @see #processMime(HttpServletRequest, HttpServletResponse)
     * @see #processOther(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest req,  HttpServletResponse res) throws IOException, ServletException {
        // Debug logging
        if (log.isDebugEnabled()) {
            StringBuffer debugInfo = new StringBuffer("Received request from ");
            debugInfo.append(req.getHeader("User-Agent")).append(" @ ");
            debugInfo.append(req.getRemoteHost()).append(" (");
            debugInfo.append(req.getRemoteAddr()).append("):\n");
            debugInfo.append("  Protocol: ");
            debugInfo.append(req.getProtocol()).append("\n");
            for(Enumeration e = req.getHeaderNames(); e.hasMoreElements(); ) {
                String headerName = (String) e.nextElement();
                debugInfo.append("  ").append(headerName).append(": ");
                debugInfo.append(req.getHeader(headerName)).append("\n");                
            }
            log.debug(debugInfo);            
        }
        // Delegate to methods
        try {
            if (req.getHeader("Content-type").startsWith(JMF_CONTENT_TYPE)) {
                processJMF(req, res);
            } else if (req.getHeader("Content-type").startsWith(JDF_CONTENT_TYPE)) {
                processJDF(req, res);
            } else if (req.getHeader("Content-type").startsWith(MIME_CONTENT_TYPE)) {
                processMime(req, res);
            } else {
                processOther(req, res);
            }
        } catch (Exception e) {
            String err = "The request body could not be processed. Maybe it did not contain JMF or JDF?" 
                + " [Java Exception: " + e + "]";
            log.error(err, e);
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }
    
    /**
     * HTTP GET is not implemented.
     */
    public void doGet(HttpServletRequest req,  HttpServletResponse res) throws IOException, ServletException {
        String msg = "Received HTTP GET request from " + req.getHeader("User-Agent") + " @ " + 
        req.getRemoteHost() + " (" + req.getRemoteAddr() + "). Request ignored.";
        log.warn(msg);
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "HTTP GET not implemented.");
    }
    
    /**
     * Processes JMF.
     * @param req
     * @param resp
     * @throws IOException
     */
    public void processJMF(HttpServletRequest req,  HttpServletResponse res) 
    throws IOException {
        String msg = "Received JMF from " + req.getHeader("User-Agent") + " @ " + 
        req.getRemoteHost() + " (" + req.getRemoteAddr() + "). Request ignored.";
        log.warn(msg);
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "JMF support not implemented.");
    }
    
    /**
     * Processes JDF.
     * @param req
     * @param resp
     * @throws IOException
     */
    public void processJDF(HttpServletRequest req,  HttpServletResponse res) 
    throws IOException {
        String msg = "Received JDF from " + req.getHeader("User-Agent") + " @ " + 
        req.getRemoteHost() + " (" + req.getRemoteAddr() + "). Request ignored.";
        log.warn(msg);
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "JDF support not implemented.");
    }

    /**
     * Processes MIME package.
     * @param req
     * @param resp
     * @throws IOException
     */
    public void processMime(HttpServletRequest req,  HttpServletResponse res) 
    throws IOException {
        String msg = "Received MIME from " + req.getHeader("User-Agent") + " @ " + 
        req.getRemoteHost() + " (" + req.getRemoteAddr() + "). Request ignored.";
        log.warn(msg);
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "MIME support not implemented.");
    }
    
    /**
     * Processes other.
     * @param req
     * @param resp
     * @throws IOException
     */
    public void processOther(HttpServletRequest req,  HttpServletResponse res) 
    throws IOException {
        String msg = "Received unknown content type (" + req.getHeader("Content-type")
            + " from " + req.getHeader("User-Agent") + " @ " + req.getRemoteHost() 
            + " (" + req.getRemoteAddr() + "). Request ignored.";
        log.warn(msg);
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Content-type '" + req.getHeader("Content-type") + "' is not supported.");
    }
}
