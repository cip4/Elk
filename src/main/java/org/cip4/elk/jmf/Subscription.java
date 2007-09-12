/*
 * Created on Nov 8, 2005
 */
package org.cip4.elk.jmf;

import org.cip4.jdflib.jmf.JDFQuery;

/**
 * This class represents a subscription. It consists of:
 * <ul>
 * <li>The initiating message that contained the <em>Subscription</em>
 * element</li>
 * <li>An URL to which <em>Signal</em>s shoul be sent. This is the URL specified by
 * the initiating messages <em>Subscription/@URL</em> attribute.</li>
 * </ul>
 * NOTE: The initiating messages <em>/@ID</em> is the same as the 
 * {@link Subscription#getId()}, which is also the same as the subscriptions
 * channel ID.
 * 
 * @todo Modify to support Command subscritpions.
 * @version $Id$
 * @author Claes Buckwalter, clabu@itn.liu.se
 * @author Ola Stering, olst6875@student.uu.se
 */
public interface Subscription {

    public static final String TIME_TYPE = "TIME_TYPE";
    public static final String EVENT_TYPE = "EVENT_TYPE";

    /**
     * Returns the message type of this <code>Subscription</code> which is
     * the same as this <code>Subscription</code>'s <em>Query/@Type</em>
     * (i.e QueueStatus or KnownDevices).
     * 
     * @return the message type of this <code>Subscription</code> which is the
     *         same as this <code>Subscription</code>'s <em>Query/@Type</em>
     *         (i.e QueueStatus or KnownDevices).
     */
    public abstract String getMessageType();

    /**
     * Returns the type of the <code>Subscription</code>, it is either
     * TIME_TYPE or EVENT_TYPE. A <code>Subscription</code> that is time-based
     * has the attribute TIME_TYPE, all other have the type EVENT_TYPE.
     * 
     * @return the type of the <code>Subscription</code>, it is either
     *         {@link #TIME_TYPE} or {@link #EVENT_TYPE}.
     */
    public abstract String getType();

    /**
     * Returns this <code>Subscriptions</code>'s <em>Query</em>.
     * 
     * @return this <code>Subscriptions</code>'s <em>Query</em>.
     */
    public abstract JDFQuery getQuery();

    /**
     * Returns the id of this <code>Subscription</code> (which is equivalent
     * to the ChannelID and the original <em>Query</em>'s ID)
     * 
     * @return the id of this <code>Subscription</code> (which is equivalent
     *         to the ChannelID and the original <em>Query</em>'s ID)
     */
    public abstract String getId();

    /**
     * Returns the URL to which this <code>Subscription</code> is registered.
     * 
     * @return the URL to which this <code>Subscription</code> is registered.
     */
    public abstract String getUrl();

    /**
     * @return <code>true</code> if this <code>Subscription</code> has a
     *         query where its Subscription/@RepeatStep attribute is set (and
     *         not 0), <code>false</code> otherwise
     */
    public abstract boolean isRepeatStepSubscription();

    /**
     * Returns the <em>RepeatStep</em> attribute of this
     * <code>Subscription</code>.
     * 
     * @return the <em>RepeatStep</em> attribute of this
     *         <code>Subscription</code>.
     */
    public abstract int getRepeatStep();

}
