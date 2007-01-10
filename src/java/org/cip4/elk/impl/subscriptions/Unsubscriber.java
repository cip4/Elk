/*
 * Created on 2005-apr-27 
 */
package org.cip4.elk.impl.subscriptions;

import org.cip4.jdflib.jmf.JDFStopPersChParams;

/**
 * An interface to handle unregistering of subscriptions. An implementor of this
 * class should remove any <code>Subscription</code> that matches the
 * <em>StopPersChParams</em> in the
 * {@link org.cip4.elk.impl.subscriptions.SubscriptionContainer}.
 * 
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id$
 */
public interface Unsubscriber {

    /**
     * Handles unregistering of a <em>Subscription</em> (or Persistent
     * channel) according to the given <em>StopPeristentChParams</em>.
     * 
     * @param subscriptions The container of current subscriptions.
     * @param stopParams The <em>StopPersChParams</em> to apply to the
     *            unregistering
     * 
     * @return 0 on success, preferably an appropriate error code otherwise
     */
    public int unsubscribe(SubscriptionContainer subscriptions,
            JDFStopPersChParams stopParams);

}
