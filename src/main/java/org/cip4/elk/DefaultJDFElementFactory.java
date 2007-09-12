/*
 * Created on Sep 2, 2004
 */
package org.cip4.elk;

import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;

/**
 * A default implementation of <code>JDFElementFactory</code>. This
 * implementation creates JDF elements using the constructor of
 * <code>com.heidelberg.JDFLib.core.JDFDoc</code>:
 * 
 * <pre>
 * new JDFDoc(elementName).getRoot()
 * </pre>
 * 
 * @see org.cip4.jdflib.core.JDFDoc
 * @author Claes Buckwalter (clabu@itn.liu.se)
 * @version $Id: DefaultJDFElementFactory.java,v 1.6 2006/08/24 11:55:30 buckwalter Exp $
 */
public class DefaultJDFElementFactory extends JDFElementFactory {
    protected DefaultJDFElementFactory() {
        super();
    }

    /**
     * Creates a JDF element of the specified type.
     * 
     * @see org.cip4.elk.JDFElementFactory#createJDFElement(java.lang.String)
     */
    public JDFElement createJDFElement(String elementName) {
        return (JDFElement) new JDFDoc(elementName).getRoot();
    }

    /**
     * Convenience method for creating JDF nodes.
     * 
     * @return a JDF node
     */
    public JDFNode createJDF() {
        return (JDFNode) createJDFElement(ElementName.JDF);
        //return new JDFDoc(ElementName.JDF).getJDFRoot();
    }

    /**
     * Convenience method for creating JMF nodes.
     * This method sets the attributes:
     * <ul>
     * <li>JMF/@xmlns="http://www.CIP4.org/JDFSchema_1_1"</li>
     * <li>JMF/@xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"</li>
     * <li>JMF/@Version="1.2"</li>
     * <li>JMF/@TimeStamp</li>
     * </ul>
     * 
     * @return a JMF node
     */
    public JDFJMF createJMF() {
    	JDFJMF jmf = (JDFJMF) createJDFElement(ElementName.JMF);
    	jmf.setVersion(JDFElement.EnumVersion.Version_1_2);
    	jmf.addNameSpace("xsi","http://www.w3.org/2001/XMLSchema-instance");
    	
        return jmf;
        
        //return new JDFDoc(ElementName.JMF).getJMFRoot();
    }

}
