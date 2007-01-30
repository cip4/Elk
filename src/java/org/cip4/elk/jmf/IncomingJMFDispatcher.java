/*
 * Created on Aug 25, 2004
 */
package org.cip4.elk.jmf;

import java.util.Map;
import java.util.Set;

import org.cip4.elk.Config;
import org.cip4.jdflib.jmf.JDFJMF;

/**
 * An interface for dispatching incoming JMF messages to {@link JMFProcessor}s, 
 * that process the JMF messages synchronously.
 * <p>
 * When {@link #dispatchJMF(JDFJMF) dispatchJMF} is called the dispatcher splits up the JMF node's
 * child messages, and based on the type of each individual message, routes 
 * it to the corresponding <code>JMFProcessor</code>. The responses from all 
 * processors are combined by the dispatcher to a single JMF response which is 
 * returned. 
 * </p><p>
 * Processors are registered to handle one or more message types, see 
 * {@link  #registerProcessor(String,JMFProcessor)}. Examples of JMF message types 
 * are <em>KnownMessages</em>, <em>SubmitQueueEntry</em> and <em>Status</em>. For a 
 * complete list of all standard JMF message types, see the JDF Specification. 
 * Processors may be registered for custom message types.
 * </p><p>
 * An implementation of this interface may chose to dispatch a JMF node's child 
 * messages sequentially, one after another, or in parallel. 
 * </p>
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @see <a href="http://www.cip4.org/documents/jdf_specifications/JDF1.2.pdf">JDF Specification Release 1.2, Chapter 5: JDF Messaging with the Job Messaging Format</a>
 */
public interface IncomingJMFDispatcher {

    /**
     * Sets this dispatcher's configuration
     * @param config
     */
    public void setConfig(Config config);
	
    /**
     * Registers a <code>JMFProcessor</code> for handling a <em>JMF message type</em>.
     * When a JMF message of the specified message type is received by an implementation
     * of this interface the message should be routed to the corresponding 
     * JMFProcessor.
     * <p>
     * If a JMFProcessor already is registered for the message type the JMFProcessor
     * will automatically be unregistered and returned. 
     * </p><p>
     * Examples of JMF message types are <em>KnownMessages</em>, <em>SubmitQueueEntry</em> and <em>Status</em>. For a 
     * complete list of all standard JMF message types, see the JDF Specification. Custom message types may
     * also be registered. 
     * </p>
     * 
     * @param jmfType   the name of the JMF message type to register, for example <em>KnownMessages</em>
     * @param processor the JMFProcessor that all messages of the message type will be routed to
     * @return the JMFProcessor that was previously registered to handle the message type; or <code>null</code> if no JMFProcessor was previously registered
     */
    public JMFProcessor registerProcessor(String jmfType, JMFProcessor processor);
  
    /**
     * Unregisters and returns the JMFProcessor for the specified message type. 
     * 
     * @see <a href="http://www.cip4.org">JDF Specification Release 1.2, Chapter 5: JDF Messaging with the Job Messaging Format</a> 
     * @param jmfType   the name of the JMF message type to unregister
     * @return  the JMFProcessor that was registered to handle the message type; or <code>null</code> if no JMFProcessor was registered
     */
    public JMFProcessor unregisterProcessor(String jmfType);
    
    /**
     * Returns a set of <code>String</code>s representing the names of the messages types 
     * that are handled by this <code>IncomingJMFDispatcher</code>. If no messages types are registered 
     * then the set will be of size <code>0</code>.
     * 
     * @return a set of <code>String</code>s representing the names of the messages types
     */
    public Set getMessageTypes();
    
    /**
     * Returns the JMFProcessor processor for the specified message type.
     * 
     * @param jmfType
     * @return the JMFProcessor for the specified message type; or <code>null</code> if the message type is not registered and there is no default processor
     */
    public JMFProcessor getProcessor(String jmfType);
    
    /**
     * Registers a default JMFProcessor to handle all message types that do not have a specifically registered JMFProcessor.
     * All JMF messages that do not have a specifically registered JMFProcessor will be dispatched to the default JMFProcessor.
     * <p>
     * If a JMFProcessor already is registered for the message type the JMFProcessor will automatically 
     * be unregistered and returned. 
     * </p>
     * 
     * @param defaultProcessor the default JMFProcessor 
     * @return the previously registered default JMFProcessor; <code>null</code> if no default JMFProcessor was previously registered
     */
    public JMFProcessor registerDefaultProcessor(JMFProcessor defaultProcessor);    
    
    /**
     * Unregisters the default JMFProcessor that handles all message types that do not have a specifically 
     * registered JMFProcessor.
     * 
     * @param defaultProcessor the default JMFProcessor 
     * @return the previously registered default JMFProcessor; <code>null</code> if no default JMFProcessor was previously registered
     */  
    public JMFProcessor unregisterDefaultProcessor();
    
    /**
     * Returns the default JMFProcessor.
     * 
     * @return the default JMFProcessor; or null if no default processor is registered
     */
    public JMFProcessor getDefaultProcessor();
    

    /**
     * Registers all message processors in the map. Each map key is the
     * the name of the JMF message type to register. Each key's corresponding value is the
     * JMFProcessor that all messages of the message type will be routed to. If a key
     * has value <code>default</code> the processor should be registered as the default
     * JMF message processor. 
     * <p>
     * All new message type processors will be added to the current configuration. Any 
     * already registered message type processors will be replaced by the ones in the map.
     * </p>
     * @param processors    a <code>Map</code> of <code>JMProcessors</code>   
     */
    public void setProcessors(Map processors);
    
    /**
     * Dispatches a <em>JMF node</em>'s child messages to one or more 
     * <code>JMFProcessor</code>s based on each message's type. A JMF node may 
     * contain one or more child messages, possibly of a different message families 
     * and types. Each message is dispatched to a JMFProcessor based on its 
     * message <em>type</em>. Messages may be dispatched sequentially, one after 
     * another, or in parallel. The responses from all processors are combined 
     * by the dispatcher to a single JMF response which is returned when this 
     * method returns.
     * @param jmf the JMF node containing child messages to dispatch
     * @return a JMF node contain the responses of the processed child messages
     */
    public JDFJMF dispatchJMF(JDFJMF jmf);
    
}
