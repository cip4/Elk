/*
 * Created on Sep 13, 2004
 */
package org.cip4.elk.jmf;

import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;

/**
 * A dispatcher that sends JMF messages and JDF docments.
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public interface OutgoingJMFDispatcher {
    
    /**
     * Dispatches a JMF message to the specified URL and returns the response
     * from the receiving controller/device.
     * @param jmf   the JMF message to dispatch
     * @param url   the URL to dispatch the JMF to
     * @return  The JMF response from the receiving controller/device. Is
     *          <code>null</code> if the response was empty.
     */
    public JDFResponse dispatchJMF(JDFJMF jmf, String url);
    
    /**
     * Dispatches a JDF document to the specified URL and returns the response 
     * from the receiving controller/device.
     * @param jdf   the JDF message to dispatch
     * @param url   the URL to dispatch the JMF to 
     */    
    public void dispatchJDF(JDFNode jdf, String url);
    
    /**
     * Dispatches a JMF message containing a Signal. An implementation of this 
     * method may chose to do this synchronously or asynchronously. 
     * @param jmf   the JMF message to dispatch
     * @param url   the URL to dispatch the JMF to
     * @throws  java.lang.IllegalArgumentException if the JMF message did not
     *          contain a <em>Signal</em> element
     */
    public void dispatchSignal(JDFJMF jmf, String url);
}
