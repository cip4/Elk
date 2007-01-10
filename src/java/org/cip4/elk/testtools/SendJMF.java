package org.cip4.elk.testtools;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 *
 * This is a sample application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * This application sends an XML document
 * to a remote web server using HTTP POST
 *
 * @author Sean C. Sullivan
 * @author Ortwin Glück
 * @author Oleg Kalnichevski
 * @author Claes Buckwalter
 */
public class SendJMF {
    public static final String JDF_CONTENT_TYPE = "application/vnd.cip4-jdf+xml";
    public static final String JMF_CONTENT_TYPE = "application/vnd.cip4-jmf+xml";
    public static final String MIME_CONTENT_TYPE = "multipart/related";
    public static final String JDF_EXTENSION = ".jdf";
    public static final String JMF_EXTENSION = ".jmf";
    public static final String JDF_MIME_EXTENSION = ".mjd";
    public static final String JMF_MIME_EXTENSION = ".mjm";    
    
    /**
     *
     * Usage:
     *          java PostXML http://mywebserver:80/ c:\foo.xml
     *
     *  @param args command line arguments
     *                 Argument 0 is a URL to a web server
     *                 Argument 1 is a local filename
     *                 Argument 2 is number of times to post (optional, default 1)
     *                 Argument 3 is delay between posts (optional, default 1000ms)
     *                 Argument 4 is proxy host (optional)
     *                 Argument 5 is proxy port (optional)
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 6) {
            System.out.println("Usage: url filename [repeat [delay [proxyhost proxport]]]");           
            System.out.println("  url         URL to post the file to");
            System.out.println("  filename    File to post to the URL");
            System.out.println("  repeat      Number of times to repeat the post; default is 1");
            System.out.println("  delay       Delay between repeated posts; default is 500ms");
            System.out.println("  proxyhost   Proxy hostname");
            System.out.println("  proxyport   Proxy port");
            System.out.println();
            System.exit(1);
        }
        
        /*
        //XXX
        // Accept all self-signed certificates
        Protocol easyhttps = new Protocol("https", 
                new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
        //XXX         
        */
        
        // HTTP client
        HttpClient httpClient = new HttpClient();
        
        // Get target URL
        String strURL = args[0];
        // Get file to be posted
        String strXMLFilename = args[1];
        File input = new File(strXMLFilename);
        // Get number of posts
        int repeat = 1;        
        if (args.length >= 3) {
            repeat = Integer.parseInt(args[2]);
        }
        // Get delay between posts
        int delay = 500;
        if (args.length >= 4) {
            delay = Integer.parseInt(args[3]);
        }
        // Configure proxy
        String proxyHost = null;
        int proxyPort = -1;
        if (args.length == 6) {
            proxyHost = args[4];
            proxyPort = Integer.parseInt(args[5]);
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }
        
        System.out.println("Posting " + repeat + " times to '" + strURL + "'...");
        // Do the post
        long t0 = System.currentTimeMillis();
        for(int i=0; i<repeat; i++) {
           System.out.println("========== Post " + (i+1) + " ==========");
           try {
               postXML(httpClient, strURL, input);
               if (i != (repeat-1))
                   Thread.sleep(delay);
           } catch(Exception e) {
               System.out.println("== Post " + (i+1) + " failed: " + e + " ==");
           }
       }
       long t1 = System.currentTimeMillis();      
       System.out.println("========== Posted " + repeat + " time(s) in " + (t1-t0) + "ms ==========");
       System.out.println();
    }
    
    public static void postXML(HttpClient httpclient, String strURL, File input) 
        throws Exception {
        // Prepare HTTP post
        PostMethod post = new PostMethod(strURL);
        // Request content will be retrieved directly 
        // from the input stream
        post.setRequestBody(new FileInputStream(input));
        // Per default, the request content needs to be buffered
        // in order to determine its length.
        // Request body buffering can be avoided when
        // = content length is explicitly specified
        // = chunk-encoding is used
        if (input.length() < Integer.MAX_VALUE) {
            post.setRequestContentLength((int)input.length());
        } else {
            post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        }
        // Content-Type
        String contentType; 
        if (input.getName().endsWith(JDF_EXTENSION)) {
            contentType = JDF_CONTENT_TYPE;
        } else if (input.getName().endsWith(JMF_EXTENSION)) {
            contentType = JMF_CONTENT_TYPE;
        } else if (input.getName().endsWith(JDF_MIME_EXTENSION)) {
            contentType = MIME_CONTENT_TYPE;
        } else if (input.getName().endsWith(JMF_MIME_EXTENSION)) {
            contentType = MIME_CONTENT_TYPE;
        } else {
            contentType = "text/xml; charset=ISO-8859-1";
        }
        post.setRequestHeader("Content-Type", contentType);
        //post.setHttp11(false);
        // TODO Specify encoding
        
        // Print request headers
        System.out.println("===== Request Headers =====");
        printHeaders(post.getRequestHeaders());
        
        // Server certificates
        System.out.println("===== Server Certificate =====");
        
        // Execute request
        int result = httpclient.executeMethod(post);
        
        // Print response headers
        System.out.println("===== Response Headers =====");
        printHeaders(post.getResponseHeaders());
        System.out.println("Response Status Code: " + result);
        printHeaders(post.getResponseFooters());

        // Check response content-type
        Header responseContentType = post.getResponseHeader("Content-Type");
        if (responseContentType != null &&  
                responseContentType.getValue().indexOf(JMF_CONTENT_TYPE) != -1) {
            System.out.println("== WARNING: The response's Content-Type may be" +
                    " incorrect. JMF messages should have Content-Type " 
                    + JMF_CONTENT_TYPE + " ==");
        }
        // Display response
        System.out.println("===== Response Body =====");
        System.out.println(post.getResponseBodyAsString());
        // Release current connection to the connection pool once you are done
        post.releaseConnection();
    }
    
    
    private static void printHeaders(Header[] headers) {
        for(int i=0; i<headers.length; i++) {
            System.out.println(headers[i].getName() + ": " + headers[i].getValue());
        }
    }   
}