/*
 * Created on Sep 2, 2004
 */
package org.cip4.elk;

import java.io.InputStream;
import java.util.Properties;

import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;

/**
 * Defines a factory API that enables applications to obtain a factory for
 * creating JDF elements.
 * <p>
 * Usage examples:
 * 
 * <pre>
 * JDFElementFactory factory = JDFElementFactory.getInstance();
 * 
 * // Create a JDF element
 * JDFDoc jdf = factory.createJDF();
 *  
 *      // Create a JMF element
 *      JDFJMF jmf = factory.createJMF();
 *  
 *      // Create an element using an element name defined in com.heidelberg.JDFLib.core.ElementName
 *      JDFNotification notification1 = (JDFNotification) factory.createJDFElement(ElementName.NOTIFICATION);
 *  
 *      // Create an element using a string
 *      JDFNotification notification2 = (JDFNotification) factory.createJDFElement(&quot;Notification&quot;);
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * <em>This class is thread save.</em>
 * </p>
 * 
 * @todo Reimplement without using synchronization, but still guaranteeing
 *       thread safety.
 * @see org.cip4.jdflib.core.ElementName
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: JDFElementFactory.java,v 1.5 2006/08/24 11:55:31 buckwalter Exp $
 */
public abstract class JDFElementFactory {

    private static final String JDFELEMENTFACTORY_KEY = "org.cip4.elk.JDFElementFactory";

    private static final String CONFIG_FILE = "org/cip4/elk/JDFElementFactory.properties";

    private static JDFElementFactory instance;

    /**
     * Private constructor so that this class cannot be instantiated except from
     * its factory method.
     */
    protected JDFElementFactory() {
    }

    /**
     * Obtain a new instance of a <code>JDFElementFactory</code>. This method
     * uses the following ordered lookup to determine the
     * <code>JDFElementFactory</code> class to load:
     * <ul>
     * <li>Use the <code>org.cip4.elk.JDFElementFactory</code> system
     * property. The value of the system property is the fully qualified name of
     * the implemenation class.</li>
     * <li>Uses the first properties file
     * <code>org/cip4/elk/JDFElementFactory.properties</code> found in the
     * classpath. This configuration file is in standard
     * <code>java.util.Properties</code> format and uses the same key as the
     * system property defined above. Again, the property's value is the fully
     * qualified name of the implemenation class.</li>
     * </ul>
     * Once an applciation has obtained a reference to a
     * <code>JDFElementFactory</code> implementation it can use the factory to
     * generate <code>JDFElement</code>s.
     * 
     * @return a reference to a JDFElementFactory
     * @throws JDFElementFactoryLoaderException
     *             if the factory cannot be loaded
     */
    public static synchronized JDFElementFactory getInstance() {
        // Creates an instance if it has not been instantiated yet
        if (instance == null) {
            loadInstance();
        }
        return instance;
    }

    /**
     * Loads the factory instance based on the system property or configuration
     * file.
     * 
     * @see #getInstance()
     */
    protected static void loadInstance() {
        try {
            // Checks for the system property
            String className = System.getProperty(JDFELEMENTFACTORY_KEY);
            if (className == null) {
                // Checks for the properties file in the classpath
                InputStream stream = JDFElementFactory.class.getClassLoader()
                        .getResourceAsStream(CONFIG_FILE);
                // Loads the properties file
                Properties props = new Properties();
                props.load(stream);
                className = props.getProperty(JDFELEMENTFACTORY_KEY);
            }
            // Loads the class
            Class factoryClass = Class.forName(className);
            instance = (JDFElementFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw new JDFElementFactoryLoaderException(
                    "Could not load the JDFElementFactory implementation. Verify that the system property 'org.cip4.elk.JDFElementFactory' exists or a property file 'org/cip4/elk/JDFElementFactory.properties' exists in the classpath. The property file must contain the same key as the system property. The value of the property should be the fully qualified name of the JDFElementFactory implementation to be loaded.",
                    e);
        }
    }

    /**
     * Creates JDF elements of the specified type.
     * 
     * @see org.cip4.jdflib.core.ElementName
     * @param elementName
     *            the name of JDF element to create
     * @return a JDF element
     */
    public abstract JDFElement createJDFElement(String elementName);

    /**
     * Convenience method for creating JDF nodes.
     * 
     * @return a JDF node
     */
    public abstract JDFNode createJDF();

    /**
     * Convenience method for creating JMDF nodes.
     * 
     * @return a JMF node
     */
    public abstract JDFJMF createJMF();

}
