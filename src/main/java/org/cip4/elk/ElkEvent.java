/*
 * Created on Sep 30, 2004
 */
package org.cip4.elk;

import java.util.EventObject;

import org.cip4.jdflib.resource.JDFNotification;

/**
 * This is a base class for an event that can be generated by any
 * component in an Elk system. Each event has a class used by the sender of the 
 * event to categorize the event. An event's class maps to the classes used by 
 * JDF's <em>NotificationFilter</em> element. 
 * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Table 5-18 Contents of the NotificationFilter element</a>
 * @see org.cip4.jdflib.resource.process.JDFNotification.EnumClass
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: ElkEvent.java,v 1.3 2005/05/13 13:12:25 ola.stering Exp $
 */
public class ElkEvent extends EventObject {
    public static final JDFNotification.EnumClass EVENT = JDFNotification.EnumClass.Event;
    public static final JDFNotification.EnumClass INFORMATION = JDFNotification.EnumClass.Information;
    public static final JDFNotification.EnumClass WARNING = JDFNotification.EnumClass.Warning;
    public static final JDFNotification.EnumClass ERROR = JDFNotification.EnumClass.Error;
    public static final JDFNotification.EnumClass FATAL = JDFNotification.EnumClass.Fatal;
    
    protected long _timestamp;
    protected JDFNotification.EnumClass _eventClass;
    protected String _description;
   
    /**
     * Creates a new device event.
     * 
     * @param eventClass    the events class
     * @param source        the device component that generated this event
     * @param description   a textual description of the event and what caused it
     */
    public ElkEvent(JDFNotification.EnumClass eventClass, 
            Object source, String description) {
        super(source);
        _eventClass = eventClass;
        _description = description;
        _timestamp = System.currentTimeMillis();
    }
    
    public JDFNotification.EnumClass getEventClass() {
        return _eventClass;
    }
    
    /**
     * The time when this event was generated
     * @return the time in milliseconds when this event was generated
     */    
    public long getTimestamp() {
        return _timestamp;
    }
    
    public String getDescription() {
        return _description;
    }

    public String toString() {
        return "ElkEvent[Class: " + _eventClass.getName() + ";  Source: " + getSource() + ";  Description: " + getDescription() + ";  Time stamp: " + getTimestamp() + "]";
    }
}