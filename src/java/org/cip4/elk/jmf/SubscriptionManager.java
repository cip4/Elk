/*
 * Created on Sep 14, 2004
 */
package org.cip4.elk.jmf;

import java.util.Collection;

import org.cip4.elk.ElkEvent;
import org.cip4.jdflib.jmf.JDFNotificationDef;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.resource.process.JDFNotificationFilter;

/**
 * An interface for managing subscriptions, <em>persistent channels</em>, of JMF
 * messages. Subscriptions are registered with this class, which broadcasts JMF 
 * Signal messages to all subscribers.  
 * 
 * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, 5.2.1.1 Query</a>
 * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, 5.2.2.3 Persistent Channels</a>
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: SubscriptionManager.java,v 1.4 2005/11/08 15:59:11 buckwalter Exp $
 */
public interface SubscriptionManager {

    /**
     * Registers a new subscription (persistent channel) for the specified
     * query. Returns <code>true</code> if the subscription was successfully
     * registered. Returns <code>false</code> if the <em>Query</em> element
     * does not contain a <em>Subscription</em> child element or if the 
     * subscription could not be registered for some other reason.
     * <p>
     * It is up to the client of this interface to send a response back to the 
     * controller that issued the subscription command. If the subscription
     * was successfully registered this method returns <code>true</code>, which
     * means <em>Response/@Subscribed</em> should be set to <code>true</code>.
     * </p>
     * <p>
     * If the subscription could not be registered this method returns 
     * <code>false</code>, which means <em>Response/@Subscribed</em> 
     * should be set to <code>false</code> and <em>Response/@ReturnCode</em>
     * should be set to <code>111</code>.
     * </p>  
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-4 Contents of the Repsonce messge element</a> 
     * @param subscription  the subscription parameters
     * @return <code>true</code> if the subscription was accepted and regitered;
     *         <code>false</code> otherwise
     */
    public boolean registerSubscription(JDFQuery subscription);
    
    /**
     * Unregisters a subscription (persistent channel).
     * 
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, 5.5.1.6 StopPersistentChannel</a>
     * @param stopParams    the parameters specifying the subscription to 
     *                      unregister/the persistent channel to stop
     * @return 0 on success. Otherwise matching error code
     * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Appendix I, p. 619</a>                
     */
    public int unregisterSubscription(JDFStopPersChParams stopParams);
    
    /**
     * Returns a list of the <em>Notification</em> types this 
     * <code>SubscriptionMananger</code> supports that match the the specified 
     * <em>NotificationFilter</em>. If the filter is <code>null</code> then all
     * <em>Notification</em> types supported by this 
     * <code>SubscriptionManager</code> should be returned. 
     * @param filter    the filter
     * @return  an array containing <code>JDFNotificationDef</code> elements 
     *          that specify the supported <em>Notification</em> types
     */
    public JDFNotificationDef[] getNotificationDefs(JDFNotificationFilter filter);
    
    /**
     * Broadcasts the event to all subscribers whose subscriptions match the
     * event. Events are delivered to subscribers synchronously or
     * asynchronously, depending on how this <code>SubscriptionManager</code>
     * has been configured.
     * 
     * @param event the <code>ElkEvent</code> to broadcast
     */
    public void broadcastEvent(ElkEvent event);
    
    /**
     * Returns a collection of all subscriptions registered with this subscription
     * manager.
     * @return a <code>Collection</code> of <code>{@link Subscription}</code>s
     */
    public Collection/*Subscription*/ getSubscriptions();
}
