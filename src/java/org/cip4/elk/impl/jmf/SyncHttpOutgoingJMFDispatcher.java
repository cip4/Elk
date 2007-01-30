/*
 * Created on Sep 13, 2004
 */
package org.cip4.elk.impl.jmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.cip4.elk.Config;
import org.cip4.elk.jmf.OutgoingJMFDispatcher;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;

/**
 * An outgoing JMF dispatcher that sends JMF over HTTP synchronously. A call to
 * {@link dispatchJMF(JDFJMF, URL) dispatchJMF}does not return until the
 * message has been delivered or an exception is thrown.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: SyncHttpOutgoingJMFDispatcher.java 1512 2006-08-15 08:53:16Z prosi $
 */
public class SyncHttpOutgoingJMFDispatcher implements OutgoingJMFDispatcher {

    protected static Logger log;
    private HttpClient _httpClient;
    private Config _config;

    /**
     * Constructs a new outgoing dispatcher with the specified configuration.
     * This dispatcher gets its proxy settings from the <code>Config</code>
     * object. If no proxy settings are configured in the <code>Config</code>
     * object no proxy will be used.
     * 
     * @param config a configuration containing optional proxy settings
     * @see org.cip4.elk.Config
     */
    public SyncHttpOutgoingJMFDispatcher(Config config) {
        log = Logger.getLogger(this.getClass().getName());
        _config = config;
        _httpClient = new HttpClient();
        HostConfiguration hostConfig = _httpClient.getHostConfiguration();
        int proxyPort = _config.getProxyPort();
        String proxyHost = _config.getProxyHost();
        if (proxyPort != -1 && proxyHost != null) {
            hostConfig.setProxy(proxyHost, proxyPort);
            log.debug("Configured outgoing JMF dispatcher with proxy: "
                    + proxyHost + ":" + proxyPort);
        }
    }

    /**
     * Sends a HTTP POST request containing the specified JMF message to the
     * specified URL.
     */
    public JDFResponse dispatchJMF(JDFJMF jmf, String url) {
        log.debug("Dispatching JMF (" + jmf.getMessageElement(null,0)  + ") to " + url + "...");
        JDFResponse jmfResponse = null;
        try {
            PostMethod post = postData(jmf, url);
            log.debug("Dispatched JMF message (" + jmf.getMessageElement(null,0) + ") to "
                + url);

            // Parse response body
            InputStream bodyStream = null;
            try {
                bodyStream = post.getResponseBodyAsStream();
                if (bodyStream.available() != 0) {
                    jmf = (JDFJMF) new JDFParser().parseStream(bodyStream).getRoot();
                    jmfResponse = jmf.getResponse();
                } else {
                    log.error("Recieved empty response");
                }
            } catch (Exception e) {
                log.error("Could not parse response to dispatched JMF (" + jmf.getMessageElement(null,0) + 
                        "). Maybe the HTTP response body was empty?", e);
                e.printStackTrace();
            } finally {
                closeQuietly(bodyStream);
            }
        } catch (Exception e) {
            log.error("An error occurred while dispatching JMF (" + jmf.getMessageElement(null,0)
                    + ") to " + url, e);
            e.printStackTrace();
        }
        return jmfResponse;
    }

    /**
     * Sends a HTTP POST request containing the specified JDF document to the
     * specified URL.
     */
    public void dispatchJDF(JDFNode jdf, String url) {
        log.debug("Dispatching JDF (" + jdf.getID() + ") to " + url + "...");
        try {
            postData(jdf, url);
            log.debug("Dispatched JDF (" + jdf.getID() + ") to " + url);
        } catch (IOException ioe) {
            log.error("An error occurred while dispatching JDF (" + jdf.getID()
                    + ") to " + url, ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * Sends a HTTP POST request containing the specified JMF Signal message.
     */
    public void dispatchSignal(JDFJMF jmfSignal, String url) {
        log.debug("Dispatching JMF Signal (" + jmfSignal.getMessage(0).getID() + ") to "
                + url + "...");
        try {
            postData(jmfSignal, url);
            log.debug("Dispatched JMF Signal (" + jmfSignal.getMessage(0).getID() + ") to "
                + url);
        } catch (IOException ioe) {
            log.error("An error occurred while dispatching JMF Signal ("
                            + jmfSignal.getMessage(0).getID() + ") to " + url, ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * Sends a HTTP POST with the request-body containing the specified JDF
     * element. If the JDF element is a JMF message the request-header
     * <code>Content-type</code> is set to
     * <code>application/vnd.cip4-jmf+xml; charset=UTF-8</code>; otherwise
     * <code>application/vnd.cip4-jdf+xml; charset=UTF-8</code> is used.
     * 
     * @todo Extract encoding from XML and use the specified encoding instead of
     *       always using the default UTF-8.
     * @param jdf the JDF/JMF to send in request body
     * @param url the URL to post to
     * @return The object representing the HTTP POST used. This can be used to
     *         retrieve the responses headers and body.
     * @throws IOException if an IO error occurred
     * @see org.apache.commons.httpclient.methods.PostMethod
     */
    public PostMethod postData(JDFElement jdf, String url) throws IOException {
        log.debug("Posting data to " + url + "...");
        PostMethod post = new PostMethod(url);
        if (jdf instanceof JDFJMF) {
            // TODO Set custom character encoding
            post.setRequestHeader("Content-type",
                "application/vnd.cip4-jmf+xml; charset=UTF-8");
        } else {
            // TODO Set custom character encoding
            post.setRequestHeader("Content-type",
                "application/vnd.cip4-jdf+xml; charset=UTF-8");
        }
        String data = jdf.getOwnerDocument_KElement().write2String(0);
        byte[] jmfBytes = data.getBytes();
        post.setRequestBody(data);
        if (jmfBytes.length < Integer.MAX_VALUE) {
            post.setRequestContentLength(jmfBytes.length);
        } else {
            post
                    .setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        }
        int result = _httpClient.executeMethod(post);
        // Debug logging
        if (log.isDebugEnabled()) {
            log.debug("Response from posting to " + url + ":\nHeaders:\n"
                    + Arrays.asList(post.getResponseHeaders()) + "\nBody:\n"
                    + post.getResponseBodyAsString());
        }
        return post;
    }

    /**
     * Utility method that quietly closes an input stream.
     * 
     * @param stream
     */
    private void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ioe) {
            }
        }
    }

}
